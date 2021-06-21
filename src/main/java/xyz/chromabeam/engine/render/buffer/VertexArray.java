package xyz.chromabeam.engine.render.buffer;

import xyz.chromabeam.engine.bind.BindManager;
import xyz.chromabeam.engine.Bindable;
import xyz.chromabeam.util.Destroyable;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33C.*;

public class VertexArray implements Bindable, Destroyable {

    private final int vao;
    private final GpuBuffer vbo;
    private boolean changed = false;
    public final int floatsPerVertex;
    public final int vertexCount;
    public final int floatsInBuffer;

    public VertexArray(int vertices, int... attributes) {
        int attributeCount = attributes.length;
        int floatsPerVertex = 0;
        for (var attrib: attributes) {
            floatsPerVertex += attrib;
        }
        this.floatsPerVertex = floatsPerVertex;
        vertexCount = vertices;
        floatsInBuffer = vertices * floatsPerVertex;
        vao = BindManager.genVertexArrays();
        BindManager.bindVertexArray(vao);
        vbo = new GpuBuffer(vertices * floatsPerVertex * 4, GL_ARRAY_BUFFER);
        int stride = floatsPerVertex * 4;
        int offset = 0;
        for (int i = 0; i < attributeCount; i++) {
            glVertexAttribPointer(i, attributes[i], GL_FLOAT, false, stride, offset);
            offset += attributes[i] * 4;
            glEnableVertexAttribArray(i);
        }
        vbo.unbind();
        BindManager.unbindVertexArray(vao);
    }

    public FloatBuffer getWriteBuffer() {
        changed = true;
        return vbo.getWriteBuffer().asFloatBuffer();
    }

    public long getWriteBufferPointer() {
        changed = true;
        return vbo.getWriteBufferPointer();
    }

    public void sync() {
        if (changed) {
            vbo.bind();
            vbo.sync();
            vbo.unbind();
            changed = false;
        }
    }

    @Override
    public void bind() {
        sync();
        BindManager.bindVertexArray(vao);
    }

    @Override
    public void unbind() {
        BindManager.unbindVertexArray(vao);
    }

    @Override
    public void destroy() {
        vbo.destroy();
        BindManager.deleteVertexArrays(vao);
    }
}
