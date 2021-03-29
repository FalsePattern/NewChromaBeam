
package moe.falsepattern.engine.render.chunk;

import moe.falsepattern.engine.render.world.WorldRenderer;

import static org.lwjgl.opengl.GL33C.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Allocates, destroys, and draws the chunks.
 */
public class ChunkRenderer extends WorldRenderer implements Supplier<RenderChunk> {
    private final int chunkUniform;

    private final List<RenderChunk> renderChunks = new ArrayList<>();
    private final List<RenderChunk> inactiveRenderChunks = new ArrayList<>();
    private final int edgeSize;
    public ChunkRenderer(int edgeSize) {
        super("tile", "chunk");
        this.edgeSize = edgeSize;
        chunkUniform = childUniforms[0];
    }

    @Override
    protected void renderContent() {
        for (RenderChunk renderChunk : renderChunks) {
            glUniform2f(chunkUniform,
                    renderChunk.x * edgeSize,
                    renderChunk.y * edgeSize);
            renderChunk.draw();
        }
    }

    public RenderChunk allocateChunk() {
        var chunk = new RenderChunk(this, edgeSize);
        renderChunks.add(chunk);
        return chunk;
    }

    public void activateChunk(RenderChunk renderChunk) {
        if (inactiveRenderChunks.remove(renderChunk)) {
            renderChunks.add(renderChunk);
        }
    }

    public void deactivateChunk(RenderChunk renderChunk) {
        if (renderChunks.remove(renderChunk)) {
            inactiveRenderChunks.add(renderChunk);
        }
    }

    public void removeChunk(RenderChunk renderChunk) {
        if (renderChunks.remove(renderChunk) || inactiveRenderChunks.remove(renderChunk)) {
            renderChunk.destroyInternal();
        }
    }

    @Override
    public void destroy() {
        for (var chunk: renderChunks) {
            chunk.destroyInternal();
        }
        for (var chunk: inactiveRenderChunks) {
            chunk.destroyInternal();
        }
        renderChunks.clear();
        super.destroy();
    }


    @Override
    public RenderChunk get() {
        return allocateChunk();
    }
}
