package xyz.chromabeam.engine.render.chunk;

import org.joml.Vector4f;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.engine.render.RenderUtil;
import xyz.chromabeam.engine.render.buffer.IndexedVertexArray;
import xyz.chromabeam.engine.render.buffer.VertexArray;
import xyz.chromabeam.engine.render.texture.TextureRegionI;
import xyz.chromabeam.util.Destroyable;

import java.util.ArrayList;
import java.util.List;

/**
 * The basic building block of the new render engine. Renders a 128x128 square grid of components (16384) with a single
 * draw call. This will be changed later once the game is actually implemented, so that the used can freely choose the
 * desired scale based on their circuits/GPU/CPU. If components have static graphics, haven't updated their texture,
 * or new components haven't been added since the previous draw call, then the renderer can avoid the costly re-send
 * operation to the GPU.
 *
 * The smaller the chunk, the more draw calls are needed to draw large circuits, but it's changed less per tick because
 * of the lower amount of potential changed components per tick.
 * The larger the chunk, the more wasted data is stored on the GPU (empty components are just zero-sized rectangles).
 *
 * Also, components with animated textures need to re-send the entire chunk data to the gpu if the texture changes.
 * (i now know why modded minecraft was so laggy on older versions)
 */
public class RenderChunk implements Destroyable {

    public static final int FLOATS_PER_VERTEX = RenderUtil.POSITION_FLOATS + RenderUtil.UV_FLOATS + RenderUtil.COLOR_FLOATS;
    public static final int VERTICES_PER_QUAD = 4;
    public static final int INDICES_PER_QUAD = 6;

    public static final int FLOATS_PER_QUAD = FLOATS_PER_VERTEX * VERTICES_PER_QUAD;

    public static final int CHUNK_SIDE_LENGTH = 128;

    private static final int COLOR_OFFSET = RenderUtil.POSITION_FLOATS + RenderUtil.UV_FLOATS;

    public int x = 0;
    public int y = 0;

    private int layerCount = 0;

    private final List<IndexedVertexArray> layers = new ArrayList<>();

    private final ChunkRenderer parent;

    private final int edgeLength;

    public void set(int x, int y, Direction rotation, boolean flipped, int layer, TextureRegionI texture, Vector4f color) {
        if (x >= edgeLength || x < 0 || y >= edgeLength || y < 0) {
            throw new IllegalArgumentException("Chunk position out of bounds: " + x + ", " + y);
        } else {
            while (layer >= layerCount) createLayer();
            var buf = layers.get(layer).getVertexBuffer();
            buf.position((y * edgeLength + x) * FLOATS_PER_QUAD);
            var otherRotation = rotation == Direction.LEFT || rotation == Direction.UP;
            var horizontal = rotation == Direction.RIGHT || rotation == Direction.LEFT;
            if (texture == null) {
                for (int i = 0; i < FLOATS_PER_QUAD; i++) {
                    buf.put(0);
                }
            } else {
                float u0; float v0; float u1; float v1; float vA; float vB; float uA; float uB;
                if (otherRotation) {
                    u0 = texture.u1(); u1 = texture.u0();
                } else {
                    u0 = texture.u0(); u1 = texture.u1();
                }
                if (flipped ^ otherRotation) {
                    v0 = texture.v1(); v1 = texture.v0();
                } else {
                    v0 = texture.v0(); v1 = texture.v1();
                }
                if (horizontal) {
                    vA = v0; vB = v1; uA = u0; uB = u1;
                } else {
                    vA = v1; vB = v0; uA = u1; uB = u0;
                }
                buf
                        .put(x)    .put(y)    .put(u0).put(vA).put(color.x).put(color.y).put(color.z).put(color.w)
                        .put(x)    .put(y + 1).put(uA).put(v1).put(color.x).put(color.y).put(color.z).put(color.w)
                        .put(x + 1).put(y + 1).put(u1).put(vB).put(color.x).put(color.y).put(color.z).put(color.w)
                        .put(x + 1).put(y)    .put(uB).put(v0).put(color.x).put(color.y).put(color.z).put(color.w);
            }
        }
    }

    public void setColor(int x, int y, int layer, Vector4f color) {
        if (x >= edgeLength || x < 0 || y >= edgeLength || y < 0 || layer >= layerCount) {
            throw new IllegalArgumentException("Chunk position out of bounds: " + x + ", " + y + ", layer " + layer);
        } else {
            var buf = layers.get(layer).getVertexBuffer();
            int base = (y * edgeLength + x) * FLOATS_PER_QUAD;
            buf
                    .position(base + COLOR_OFFSET).put(color.x).put(color.y).put(color.z).put(color.w)
                    .position(base + FLOATS_PER_VERTEX + COLOR_OFFSET).put(color.x).put(color.y).put(color.z).put(color.w)
                    .position(base + 2 * FLOATS_PER_VERTEX + COLOR_OFFSET).put(color.x).put(color.y).put(color.z).put(color.w)
                    .position(base + 3 * FLOATS_PER_VERTEX + COLOR_OFFSET).put(color.x).put(color.y).put(color.z).put(color.w);
        }
    }

    public void unset(int x, int y) {
        if (x >= edgeLength || x < 0 || y >= edgeLength || y < 0) {
            throw new IllegalArgumentException("Chunk position out of bounds: " + x + ", " + y);
        }
        int pos = (y * edgeLength + x) * FLOATS_PER_QUAD;
        for (var layer: layers) {
            var buf = layer.getVertexBuffer();
            buf.position(pos);
            for (int i = 0; i < FLOATS_PER_QUAD; i++) {
                buf.put(0);
            }
        }
    }

    public void unset(int x, int y, int layer) {
        if (x >= edgeLength || x < 0 || y >= edgeLength || y < 0) {
            throw new IllegalArgumentException("Chunk position out of bounds: " + x + ", " + y);
        }
        if (layer >= layerCount) return;
        var buf = layers.get(layer).getVertexBuffer();
        buf.position((y * edgeLength + x) * FLOATS_PER_QUAD);
        for (int i = 0; i < FLOATS_PER_QUAD; i++) {
            buf.put(0);
        }
    }

    private void createLayer() {
        var vertexArray = new IndexedVertexArray(VertexArray.DrawMethod.TRIANGLES,
                edgeLength * edgeLength * VERTICES_PER_QUAD,
                edgeLength * edgeLength * INDICES_PER_QUAD,
                RenderUtil.POSITION_FLOATS, RenderUtil.UV_FLOATS, RenderUtil.COLOR_FLOATS);
        var indexBuf = vertexArray.getElementArrayBuffer();
        indexBuf.clear();
        for (int i = 0; i < edgeLength * edgeLength; i++) {
            indexBuf.put(i * 4);
            indexBuf.put(i * 4 + 1);
            indexBuf.put(i * 4 + 2);
            indexBuf.put(i * 4);
            indexBuf.put(i * 4 + 2);
            indexBuf.put(i * 4 + 3);
        }
        indexBuf.flip();
        vertexArray.sync();
        vertexArray.unbind();
        layers.add(vertexArray);
        layerCount++;
    }

    RenderChunk(ChunkRenderer parent, int edgeLength) {
        this.parent = parent;
        this.edgeLength = edgeLength;
    }

    void draw() {
        for (var layer: layers) {
            layer.bind();
            layer.sync();
            layer.draw();
            layer.unbind();
        }
    }

    @Override
    public void destroy() {
        parent.removeChunk(this);
    }

    public void activate() {
        parent.activateChunk(this);
    }

    public void deactivate() {
        parent.deactivateChunk(this);
    }

    void destroyInternal() {
        for (var layer: layers) {
            layer.destroy();
        }
        layers.clear();
    }
}
