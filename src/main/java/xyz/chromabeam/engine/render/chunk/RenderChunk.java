package xyz.chromabeam.engine.render.chunk;

import org.lwjgl.system.MemoryUtil;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.engine.render.VertexBuffer;
import xyz.chromabeam.engine.render.texture.TextureRegionI;
import xyz.chromabeam.util.Destroyable;

import java.nio.FloatBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL33C.*;

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

    public static final int VERTICES_PER_TRIANGLE = 3;
    public static final int TRIANGLES_PER_QUAD = 2;
    public static final int FLOATS_PER_VERTEX = 4;
    public static final int VERTICES_PER_QUAD = TRIANGLES_PER_QUAD * VERTICES_PER_TRIANGLE;

    public static final int FLOATS_PER_QUAD = FLOATS_PER_VERTEX * VERTICES_PER_QUAD;
    private static final long BYTES_PER_QUAD = FLOATS_PER_QUAD * 4;

    private static final long P_ZERO_BUF = MemoryUtil.nmemCalloc(BYTES_PER_QUAD, 1);


    public int x = 0;
    public int y = 0;

    private final VertexBuffer vertexBuffer;

    private final float[] BUF = new float[FLOATS_PER_QUAD];
    public void set(int x, int y, Direction rotation, boolean flipped, TextureRegionI texture) {
        if (x >= edgeLength || x < 0 || y >= edgeLength || y < 0) {
            throw new IllegalArgumentException("Chunk position out of bounds: " + x + ", " + y);
        } else if (texture == null) {
            MemoryUtil.memCopy(P_ZERO_BUF, vertexBuffer.getWriteBufferPointer() + (y * (long)edgeLength + x) * BYTES_PER_QUAD, BYTES_PER_QUAD);
        } else {
            float u0 = texture.u0();
            float v0 = flipped ? texture.v1() : texture.v0();
            float u1 = texture.u1();
            float v1 = flipped ? texture.v0() : texture.v1();
            BUF[ 0] = x    ; BUF[ 1] = y    ;
            BUF[ 4] = x    ; BUF[ 5] = y + 1;
            BUF[ 8] = x + 1; BUF[ 9] = y + 1;
            BUF[12] = x    ; BUF[13] = y    ;
            BUF[16] = x + 1; BUF[17] = y + 1;
            BUF[20] = x + 1; BUF[21] = y    ;
            switch(rotation) {
                case RIGHT -> {
                    BUF[ 2] = u0   ; BUF[ 3] = v0   ;
                    BUF[14] = u0   ; BUF[15] = v0   ;
                    BUF[ 6] = u0   ; BUF[ 7] = v1   ;
                    BUF[22] = u1   ; BUF[23] = v0   ;
                    BUF[10] = u1   ; BUF[11] = v1   ;
                    BUF[18] = u1   ; BUF[19] = v1   ;
                }
                case DOWN -> {
                    BUF[ 2] = u0   ; BUF[ 3] = v1   ;
                    BUF[14] = u0   ; BUF[15] = v1   ;
                    BUF[ 6] = u1   ; BUF[ 7] = v1   ;
                    BUF[22] = u0   ; BUF[23] = v0   ;
                    BUF[10] = u1   ; BUF[11] = v0   ;
                    BUF[18] = u1   ; BUF[19] = v0   ;
                }
                case LEFT -> {
                    BUF[ 2] = u1   ; BUF[ 3] = v1   ;
                    BUF[14] = u1   ; BUF[15] = v1   ;
                    BUF[ 6] = u1   ; BUF[ 7] = v0   ;
                    BUF[22] = u0   ; BUF[23] = v1   ;
                    BUF[10] = u0   ; BUF[11] = v0   ;
                    BUF[18] = u0   ; BUF[19] = v0   ;
                }
                case UP -> {
                    BUF[ 2] = u1   ; BUF[ 3] = v0   ;
                    BUF[14] = u1   ; BUF[15] = v0   ;
                    BUF[ 6] = u0   ; BUF[ 7] = v0   ;
                    BUF[22] = u1   ; BUF[23] = v1   ;
                    BUF[10] = u0   ; BUF[11] = v1   ;
                    BUF[18] = u0   ; BUF[19] = v1   ;
                }
            }
        }
        vertexBuffer.getBufferForWriting().put((y * edgeLength + x) * FLOATS_PER_QUAD, BUF);
    }

    public void unset(int x, int y) {
        if (x >= edgeLength || x < 0 || y >= edgeLength || y < 0) {
            throw new IllegalArgumentException("Chunk position out of bounds: " + x + ", " + y);
        }
        MemoryUtil.memCopy(P_ZERO_BUF, vertexBuffer.getWriteBufferPointer() + (y * (long)edgeLength + x) * BYTES_PER_QUAD, BYTES_PER_QUAD);
    }

    private final ChunkRenderer parent;

    private final int edgeLength;
    private final int vertices;
    RenderChunk(ChunkRenderer parent, int edgeLength) {
        this.parent = parent;
        this.edgeLength = edgeLength;
        vertices = edgeLength * edgeLength * VERTICES_PER_QUAD;
        vertexBuffer = new VertexBuffer(vertices, 2, 2);
    }

    void draw() {
        vertexBuffer.bind();
        glDrawArrays(GL_TRIANGLES, 0, vertices);
        vertexBuffer.unbind();
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
        vertexBuffer.destroy();
    }
}
