package xyz.chromabeam.world;

import org.joml.Vector4f;
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

    private static final Vector4f WHITE = new Vector4f(1);
    private static final Vector4f colorBuffer = new Vector4f(1);
    public void set(ComponentTransform<Component> transform) {
        int cX = Math.floorDiv(transform.position.x, RenderChunk.CHUNK_SIDE_LENGTH);
        int cY = Math.floorDiv(transform.position.y, RenderChunk.CHUNK_SIDE_LENGTH);
        var chunk = chunks.getOrCompute(cX, cY, () -> renderChunkSupplier.apply(cX, cY));
        var comp = transform.component;
        int masks = comp.getColorMaskCount();
        int CX = FastMath.floorMod(transform.position.x, RenderChunk.CHUNK_SIDE_LENGTH);
        int CY = FastMath.floorMod(transform.position.y, RenderChunk.CHUNK_SIDE_LENGTH);
        chunk.set(CX, CY, transform.direction, transform.flipped, 0, transform.component.getTexture(), WHITE);
        for (int i = 0; i < masks; i++) {
            chunk.set(
                    CX, CY,
                    transform.direction, transform.flipped,
                    i + 1, comp.getColorMaskTexture(i), comp.getColorMaskColor(i, colorBuffer)
            );

        }
    }

    public void updateMaskColors(ComponentTransform<Component> transform) {
        int cX = Math.floorDiv(transform.position.x, RenderChunk.CHUNK_SIDE_LENGTH);
        int cY = Math.floorDiv(transform.position.y, RenderChunk.CHUNK_SIDE_LENGTH);
        var chunk = chunks.get(cX, cY);
        var comp = transform.component;
        int masks = comp.getColorMaskCount();
        int CX = FastMath.floorMod(transform.position.x, RenderChunk.CHUNK_SIDE_LENGTH);
        int CY = FastMath.floorMod(transform.position.y, RenderChunk.CHUNK_SIDE_LENGTH);
        for (int i = 0; i < masks; i++) {
            chunk.setColor(CX, CY, i + 1, comp.getColorMaskColor(i, colorBuffer));
        }
    }

    public void remove(int x, int y) {
        int cX = Math.floorDiv(x, RenderChunk.CHUNK_SIDE_LENGTH);
        int cY = Math.floorDiv(y, RenderChunk.CHUNK_SIDE_LENGTH);
        var chunk = chunks.get(cX, cY);
        if (chunk != null) chunk.unset(FastMath.floorMod(x, RenderChunk.CHUNK_SIDE_LENGTH), FastMath.floorMod(y, RenderChunk.CHUNK_SIDE_LENGTH));
    }
}
