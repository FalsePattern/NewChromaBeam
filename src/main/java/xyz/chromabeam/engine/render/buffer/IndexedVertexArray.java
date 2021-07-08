package xyz.chromabeam.engine.render.buffer;

import org.lwjgl.opengl.GL11C;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;

public class IndexedVertexArray extends VertexArray {
    private final GpuBuffer ebo;
    private boolean eboChanged = false;
    public final int elements;

    public IndexedVertexArray(DrawMethod drawMethod, int vertices, int elements, int... attributes) {
        super(drawMethod, vertices, attributes);
        this.elements = elements;
        ebo = new GpuBuffer(elements * 4, GL_ELEMENT_ARRAY_BUFFER);
    }

    public IntBuffer getElementArrayBuffer() {
        eboChanged = true;
        return ebo.getWriteBuffer().asIntBuffer();
    }

    public long getElementArrayBufferPointer() {
        eboChanged = true;
        return ebo.getWriteBufferPointer();
    }

    private void syncEBO() {
        if (eboChanged) {
            ebo.sync();
            eboChanged = false;
        }
    }

    @Override
    public void sync() {
        super.sync();
        syncEBO();
    }

    @Override
    protected void drawImpl() {
        GL11C.glDrawElements(drawMethod, elements, GL_UNSIGNED_INT, 0);
    }

    @Override
    public void destroy() {
        super.destroy();
        ebo.destroy();
    }
}
