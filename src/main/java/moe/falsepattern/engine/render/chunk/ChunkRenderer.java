package moe.falsepattern.engine.render.chunk;

import moe.falsepattern.engine.render.Camera;
import moe.falsepattern.engine.render.Shader;
import moe.falsepattern.util.ResourceUtil;
import org.lwjgl.opengl.GL11C;

import static org.lwjgl.opengl.GL33C.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Allocates, destroys, and draws the chunks.
 */
public class ChunkRenderer implements AutoCloseable {
    private static final String defaultVertexShaderPath = "/moe/falsepattern/chromabeam/shaders/tile.vert";
    private static final String defaultFragmentShaderPath = "/moe/falsepattern/chromabeam/shaders/tile.frag";
    private final Shader shader;
    private final int chunkOffsetUniform;
    private final int zoomUniform;
    private final int aspectUniform;

    private final List<Chunk> chunks = new ArrayList<>();

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
        for (Chunk chunk : chunks) {
            glUniform2f(chunkOffsetUniform,
                    camera.pos.x + chunk.position.x * Chunk.CHUNK_SIDE_LENGTH,
                    camera.pos.y + chunk.position.y * Chunk.CHUNK_SIDE_LENGTH);
            chunk.draw();
        }
        shader.unbind();
    }

    public Chunk allocateChunk() {
        var chunk = new Chunk();
        chunks.add(chunk);
        return chunk;
    }

    public void removeChunk(Chunk chunk) {
        if (chunks.contains(chunk)) {
            chunks.remove(chunk);
            chunk.destroy();
        }
    }

    public void destroy() {
        for (var chunk: chunks) {
            chunk.destroy();
        }
        chunks.clear();
        shader.destroy();
    }

    public void clear() {
        glClear(GL11C.GL_COLOR_BUFFER_BIT);
    }

    public void setClearColor(float r, float g, float b) {
        glClearColor(r, g, b, 1f);
    }


    @Override
    public void close() {
        destroy();
    }
}
