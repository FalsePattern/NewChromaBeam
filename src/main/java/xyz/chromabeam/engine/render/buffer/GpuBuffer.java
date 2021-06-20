package xyz.chromabeam.engine.render.buffer;

import org.lwjgl.system.MemoryUtil;
import xyz.chromabeam.Global;
import xyz.chromabeam.engine.BindManager;
import xyz.chromabeam.engine.Bindable;
import xyz.chromabeam.util.Destroyable;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33C.*;

public class GpuBuffer implements Bindable, Destroyable {
    private final int pointer;
    private final int bindPoint;

    private final ByteBuffer buffer;
    private final long pBuffer;

    public GpuBuffer(int size, int bindPoint) {
        this.bindPoint = bindPoint;
        pointer = glGenBuffers();
        bind();
        glBufferData(bindPoint, size, GL_DYNAMIC_DRAW);
        buffer = MemoryUtil.memAlloc(size);
        pBuffer = MemoryUtil.memAddress(buffer);
    }

    public ByteBuffer getWriteBuffer() {
        return buffer;
    }

    public long getWriteBufferPointer() {
        return pBuffer;
    }

    public void sync() {
        if (Global.DEBUG) {
            int bound = BindManager.DEBUG_boundBuffer(bindPoint);
            if (bound != pointer) throw new IllegalStateException("Tried to sync buffer while it wasn't bound!");
        }
        glBufferSubData(bindPoint, 0, buffer);
    }

    @Override
    public void bind() {
        BindManager.bindBuffer(bindPoint, pointer);
    }

    @Override
    public void unbind() {
        BindManager.bindBuffer(bindPoint, 0);
    }

    private boolean destroyed = false;
    @Override
    public void destroy() {
        if (destroyed) throw new IllegalStateException("Tried to destroy buffer twice!");
        destroyed = true;
        glDeleteBuffers(pointer);
        MemoryUtil.memFree(buffer);
        if (Global.DEBUG) {
            int bound = BindManager.DEBUG_boundBuffer(bindPoint);
            if (bound == pointer) throw new IllegalStateException("Destroyed bound buffer!");
        }
    }
}
