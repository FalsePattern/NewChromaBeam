package xyz.chromabeam.world;

import xyz.chromabeam.component.Component;
import xyz.chromabeam.engine.render.chunk.RenderChunk;
import xyz.chromabeam.util.FastMath;
import xyz.chromabeam.util.storage.Container2D;
import xyz.chromabeam.util.storage.NativeContainer2D;

import java.util.function.BiFunction;

public class WorldRenderer {
    private final Container2D<RenderChunk> chunks = new NativeContainer2D<>(RenderChunk[]::new, RenderChunk[][]::new, RenderChunk[][][]::new);
    private final BiFunction<Integer, Integer, RenderChunk> renderChunkSupplier;
    public WorldRenderer(BiFunction<Integer, Integer, RenderChunk> renderChunkSupplier) {
        this.renderChunkSupplier = renderChunkSupplier;
    }

    public void set(ComponentTransform<Component> transform) {
        int cX = Math.floorDiv(transform.position.x, RenderChunk.CHUNK_SIDE_LENGTH);
        int cY = Math.floorDiv(transform.position.y, RenderChunk.CHUNK_SIDE_LENGTH);
        var chunk = chunks.getOrCompute(cX, cY, () -> renderChunkSupplier.apply(cX, cY));
        chunk.set(
                FastMath.floorMod(transform.position.x, RenderChunk.CHUNK_SIDE_LENGTH),
                FastMath.floorMod(transform.position.y, RenderChunk.CHUNK_SIDE_LENGTH),
                transform.direction, transform.flipped, transform.component.getTexture());
    }

    public void remove(int x, int y) {
        int cX = Math.floorDiv(x, RenderChunk.CHUNK_SIDE_LENGTH);
        int cY = Math.floorDiv(y, RenderChunk.CHUNK_SIDE_LENGTH);
        var chunk = chunks.get(cX, cY);
        if (chunk != null) chunk.unset(FastMath.floorMod(x, RenderChunk.CHUNK_SIDE_LENGTH), FastMath.floorMod(y, RenderChunk.CHUNK_SIDE_LENGTH));
    }
}
