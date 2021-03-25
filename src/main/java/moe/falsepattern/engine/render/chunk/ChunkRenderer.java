package moe.falsepattern.engine.render.chunk;

import moe.falsepattern.engine.render.Camera;
import moe.falsepattern.engine.render.Shader;
import org.lwjgl.opengl.GL11C;

import static org.lwjgl.opengl.GL33C.*;

import java.util.ArrayList;
import java.util.List;

public class ChunkRenderer implements AutoCloseable {
    private final Shader shader;
    private final int chunkOffsetUniform;
    private final int zoomUniform;
    private final int textureUniform;
    private final int aspectUniform;

    private final List<Chunk> chunks = new ArrayList<>();
    public ChunkRenderer() {
        shader = new Shader(vertex, fragment, "chunkOffset", "zoom", "textureSampler", "aspect");
        var unis = shader.getUniforms();
        chunkOffsetUniform = unis[0];
        zoomUniform = unis[1];
        textureUniform = unis[2];
        aspectUniform = unis[3];
    }

    public void drawChunks(Camera camera) {
        shader.bind();
        glUniform1f(zoomUniform, camera.getRenderZoom());
        glUniform2f(aspectUniform, camera.aspect.x, camera.aspect.y);
        for (Chunk chunk : chunks) {
            glUniform2f(chunkOffsetUniform, camera.pos.x + chunk.position.x * Chunk.CHUNK_SIDE_LENGTH, camera.pos.y + chunk.position.y * Chunk.CHUNK_SIDE_LENGTH);
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

    private static final String vertex = """
            #version 330 core
            layout(location = 0) in vec2 position;
            layout(location = 1) in vec2 uvIN;
            
            out vec2 uv;
            uniform vec2 chunkOffset;
            uniform vec2 aspect;
            uniform float zoom;
            void main() {
                uv = uvIN;
                gl_Position = vec4((position - chunkOffset) * aspect * zoom, 0.0, 1.0);
                //gl_Position = vec4(position, 0.0, 1.0);
            }
            """;
    private static final String fragment = """
            #version 330 core
            in vec2 uv;
            out vec4 color;
            
            uniform sampler2D textureSampler;
            void main() {
                color = texture(textureSampler, uv);
                //color = vec4(0, 0, 0, 1);
            }
            """;
}
