package xyz.chromabeam.engine.render;

import xyz.chromabeam.engine.Bindable;
import xyz.chromabeam.util.Destroyable;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33C.*;

public class VertexBuffer implements Bindable, Destroyable {

    private final int vao;
    private final int vbo;
    private final FloatBuffer buffer;
    private boolean changed = false;

    public VertexBuffer(int vertices, int... attributes) {
        int attributeCount = attributes.length;
        int floatsPerVertex = 0;
        for (var attrib: attributes) {
            floatsPerVertex += attrib;
        }
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices * floatsPerVertex * 4, GL_DYNAMIC_DRAW);
        int stride = floatsPerVertex * 4;
        int offset = 0;
        for (int i = 0; i < attributeCount; i++) {
            glVertexAttribPointer(i, attributes[i], GL_FLOAT, false, stride, offset);
            offset += attributes[i] * 4;
            glEnableVertexAttribArray(i);
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        buffer = MemoryUtil.memCallocFloat(vertices * floatsPerVertex);
    }

    public FloatBuffer getBufferForWriting() {
        changed = true;
        return buffer;
    }

    public void sync() {
        if (changed) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            changed = false;
        }
    }

    @Override
    public void bind() {
        sync();
        glBindVertexArray(vao);
    }

    @Override
    public void unbind() {
        glBindVertexArray(0);
    }

    @Override
    public void destroy() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        MemoryUtil.memFree(buffer);
    }
}
