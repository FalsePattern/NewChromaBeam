package xyz.chromabeam.engine.render.buffer;

import org.lwjgl.opengl.GL11C;
import xyz.chromabeam.Global;
import xyz.chromabeam.engine.bind.BindManager;
import xyz.chromabeam.engine.Bindable;
import xyz.chromabeam.util.Destroyable;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33C.*;

public class VertexArray implements Bindable, Destroyable {

    private final int pointer;
    private final GpuBuffer vbo;
    private boolean vboChanged = false;

    public final int drawMethod;
    public final int floatsPerVertex;
    public final int vertexCount;
    public final int floatsInBuffer;

    public enum DrawMethod {
        TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, LINES, LINE_STRIP;

        private int toGL() {
            return switch (this) {
                case TRIANGLES -> GL_TRIANGLES;
                case TRIANGLE_FAN ->  GL_TRIANGLE_FAN;
                case TRIANGLE_STRIP -> GL_TRIANGLE_STRIP;
                case LINES -> GL_LINES;
                case LINE_STRIP -> GL_LINE_STRIP;
            };
        }
    }

    public VertexArray(DrawMethod drawMethod, int vertices, int... attributes) {
        int attributeCount = attributes.length;
        int floatsPerVertex = 0;
        for (var attrib: attributes) {
            floatsPerVertex += attrib;
        }
        this.floatsPerVertex = floatsPerVertex;
        vertexCount = vertices;
        floatsInBuffer = vertices * floatsPerVertex;
        this.drawMethod = drawMethod.toGL();
        pointer = BindManager.genVertexArrays();
        BindManager.bindVertexArray(pointer);
        vbo = new GpuBuffer(vertices * floatsPerVertex * 4, GL_ARRAY_BUFFER);
        int stride = floatsPerVertex * 4;
        int offset = 0;
        for (int i = 0; i < attributeCount; i++) {
            glVertexAttribPointer(i, attributes[i], GL_FLOAT, false, stride, offset);
            offset += attributes[i] * 4;
            glEnableVertexAttribArray(i);
        }
        vbo.unbind();
    }

    public FloatBuffer getVertexBuffer() {
        vboChanged = true;
        return vbo.getWriteBuffer().asFloatBuffer();
    }

    public long getVertexBufferPointer() {
        vboChanged = true;
        return vbo.getWriteBufferPointer();
    }

    private void syncVBO() {
        if (vboChanged) {
            vbo.bind();
            vbo.sync();
            vbo.unbind();
            vboChanged = false;
        }
    }

    public void sync() {
        if (Global.DEBUG) {
            int bound = BindManager.DEBUG_boundVAO();
            if (bound != pointer) throw new IllegalStateException("Tried to sync VAO while it wasn't bound!");
        }
        syncVBO();
    }

    public void draw() {
        if (Global.DEBUG) {
            int bound = BindManager.DEBUG_boundVAO();
            if (bound != pointer) throw new IllegalStateException("Tried to draw vertex buffer while a different one was bound!");
        }
        drawImpl();
    }

    protected void drawImpl() {
        GL11C.glDrawArrays(drawMethod, 0, vertexCount);
    }

    @Override
    public void bind() {
        BindManager.bindVertexArray(pointer);
    }

    @Override
    public void unbind() {
        BindManager.unbindVertexArray(pointer);
    }

    @Override
    public void destroy() {
        vbo.destroy();
        BindManager.deleteVertexArrays(pointer);
    }
}
