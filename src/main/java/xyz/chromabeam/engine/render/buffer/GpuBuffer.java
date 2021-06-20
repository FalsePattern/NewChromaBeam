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
    private final int target;

    private final ByteBuffer buffer;
    private final long pBuffer;

    public GpuBuffer(int size, int target) {
        this.target = target;
        pointer = BindManager.genBuffers(target);
        bind();
        glBufferData(target, size, GL_DYNAMIC_DRAW);
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
            int bound = BindManager.DEBUG_boundBuffer(target);
            if (bound != pointer) throw new IllegalStateException("Tried to sync buffer while it wasn't bound!");
        }
        glBufferSubData(target, 0, buffer);
    }

    @Override
    public void bind() {
        BindManager.bindBuffer(target, pointer);
    }

    @Override
    public void unbind() {
        BindManager.unbindBuffer(target, pointer);
    }

    private boolean destroyed = false;
    @Override
    public void destroy() {
        if (destroyed) throw new IllegalStateException("Tried to destroy buffer twice!");
        destroyed = true;
        BindManager.deleteBuffers(target, pointer);
        MemoryUtil.memFree(buffer);
    }
}
