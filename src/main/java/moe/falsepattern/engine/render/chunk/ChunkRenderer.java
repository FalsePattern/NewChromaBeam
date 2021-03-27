package moe.falsepattern.engine.render.chunk;

import moe.falsepattern.chromabeam.world.WorldChunk;
import moe.falsepattern.engine.render.Camera;
import moe.falsepattern.engine.render.Shader;
import moe.falsepattern.util.Destroyable;
import moe.falsepattern.util.ResourceUtil;

import static org.lwjgl.opengl.GL33C.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Allocates, destroys, and draws the chunks.
 */
public class ChunkRenderer implements Destroyable, Supplier<RenderChunk> {
    private static final String defaultVertexShaderPath = "/moe/falsepattern/chromabeam/shaders/tile.vert";
    private static final String defaultFragmentShaderPath = "/moe/falsepattern/chromabeam/shaders/tile.frag";
    private final Shader shader;
    private final int chunkOffsetUniform;
    private final int zoomUniform;
    private final int aspectUniform;

    private final List<RenderChunk> renderChunks = new ArrayList<>();

    public ChunkRenderer() {
        this(ResourceUtil.readStringFromResource(defaultVertexShaderPath),
                ResourceUtil.readStringFromResource(defaultFragmentShaderPath));
    }
    public ChunkRenderer(String vertexShaderSource, String fragmentShaderSource) {
        shader = new Shader(vertexShaderSource, fragmentShaderSource,
                "chunkOffset", "zoom", "aspect");
        var unis = shader.getUniforms();
        chunkOffsetUniform = unis[0];
        zoomUniform = unis[1];
        aspectUniform = unis[2];
    }

    public void drawChunks(Camera camera) {
        shader.bind();
        glUniform1f(zoomUniform, camera.getRenderZoom());
        glUniform2f(aspectUniform, camera.aspect.x, camera.aspect.y);
        for (RenderChunk renderChunk : renderChunks) {
            glUniform2f(chunkOffsetUniform,
                    camera.pos.x + renderChunk.position.x * WorldChunk.CHUNK_SIDE_LENGTH,
                    camera.pos.y + renderChunk.position.y * WorldChunk.CHUNK_SIDE_LENGTH);
            renderChunk.draw();
        }
        shader.unbind();
    }

    public RenderChunk allocateChunk() {
        var chunk = new RenderChunk(this);
        renderChunks.add(chunk);
        return chunk;
    }

    public void removeChunk(RenderChunk renderChunk) {
        if (renderChunks.contains(renderChunk)) {
            renderChunks.remove(renderChunk);
            renderChunk.destroyInternal();
        }
    }

    @Override
    public void destroy() {
        for (var chunk: renderChunks) {
            chunk.destroyInternal();
        }
        renderChunks.clear();
        shader.destroy();
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT);
    }

    public void setClearColor(float r, float g, float b) {
        glClearColor(r, g, b, 1f);
    }

    @Override
    public RenderChunk get() {
        return allocateChunk();
    }
}
