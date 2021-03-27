package moe.falsepattern.chromabeam.world;

import moe.falsepattern.chromabeam.component.ComponentI;
import moe.falsepattern.engine.render.chunk.RenderChunk;
import moe.falsepattern.util.Destroyable;
import moe.falsepattern.util.FPMath;
import moe.falsepattern.util.IntMap2D;

import java.util.function.Supplier;

public class World2D implements Destroyable {
    private final IntMap2D<WorldChunk> chunks = new IntMap2D<>();
    private final Supplier<RenderChunk> allocator;
    public World2D(Supplier<RenderChunk> renderChunkAllocator) {
        this.allocator = renderChunkAllocator;
    }

    public ComponentI set(int x, int y, ComponentI component) {
        var chunk = chunks.getOrCompute(x / WorldChunk.CHUNK_SIDE_LENGTH, y / WorldChunk.CHUNK_SIDE_LENGTH, () -> new WorldChunk(allocator.get()));
        return chunk.setComponent(FPMath.modulo(x, WorldChunk.CHUNK_SIDE_LENGTH), FPMath.modulo(y, WorldChunk.CHUNK_SIDE_LENGTH), component);
    }

    public ComponentI get(int x, int y) {
        var chunk = chunks.getOrDefault(x / WorldChunk.CHUNK_SIDE_LENGTH, y / WorldChunk.CHUNK_SIDE_LENGTH, null);
        return chunk != null ? chunk.getComponent(FPMath.modulo(x, WorldChunk.CHUNK_SIDE_LENGTH), FPMath.modulo(y, WorldChunk.CHUNK_SIDE_LENGTH)) : null;
    }

    public void tick() {
        for (var chunk: chunks) {
            chunk.tick();
        }
    }

    @Override
    public void destroy() {
        for (var chunk: chunks) {
            chunk.destroy();
        }
    }
}
