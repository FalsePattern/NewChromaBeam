package moe.falsepattern.engine.render.chunk;

import moe.falsepattern.engine.render.texture.TextureRegionI;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;

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
public class Chunk {
    public static final int FLOATS_PER_VERTEX = 4;
    public static final int CHUNK_SIDE_LENGTH = 128;

    public static final int VERTICES_PER_TRIANGLE = 3;
    public static final int TRIANGLES_PER_QUAD = 2;
    public static final int VERTICES_PER_QUAD = TRIANGLES_PER_QUAD * VERTICES_PER_TRIANGLE;

    public static final int FLOATS_PER_QUAD = FLOATS_PER_VERTEX * VERTICES_PER_QUAD;
    public static final int QUADS_PER_CHUNK = CHUNK_SIDE_LENGTH * CHUNK_SIDE_LENGTH;

    public static final int FLOATS_PER_CHUNK = QUADS_PER_CHUNK * FLOATS_PER_QUAD;
    public static final int VERTICES_PER_CHUNK = QUADS_PER_CHUNK * VERTICES_PER_QUAD;

    public final Vector2i position = new Vector2i();

    private final int vao;
    private final int vbo;
    private final FloatBuffer buffer;
    private boolean changed = false;


    private final float[] BUF = new float[FLOATS_PER_QUAD];
    public void set(int x, int y, TextureRegionI texture) {
        changed = true;
        if (x >= CHUNK_SIDE_LENGTH || x < 0 || y >= CHUNK_SIDE_LENGTH || y < 0) {
            throw new IllegalArgumentException("Chunk position out of bounds: " + x + ", " + y);
        }
        if (texture != null) {
            float u0 = texture.u0();
            float v0 = texture.v0();
            float u1 = texture.u1();
            float v1 = texture.v1();
            BUF[ 0] = x    ; BUF[ 1] = y    ; BUF[ 2] = u0; BUF[ 3] = v0;
            BUF[ 4] = x    ; BUF[ 5] = y + 1; BUF[ 6] = u0; BUF[ 7] = v1;
            BUF[ 8] = x + 1; BUF[ 9] = y + 1; BUF[10] = u1; BUF[11] = v1;
            BUF[12] = x    ; BUF[13] = y    ; BUF[14] = u0; BUF[15] = v0;
            BUF[16] = x + 1; BUF[17] = y + 1; BUF[18] = u1; BUF[19] = v1;
            BUF[20] = x + 1; BUF[21] = y    ; BUF[22] = u1; BUF[23] = v0;
        } else {
            Arrays.fill(BUF, 0);
        }
        buffer.put((y * CHUNK_SIDE_LENGTH + x) * FLOATS_PER_QUAD, BUF);
    }

    Chunk() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, FLOATS_PER_CHUNK * 4, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * 4, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * 4, 8);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        buffer = MemoryUtil.memCallocFloat(FLOATS_PER_CHUNK);
    }

    void draw() {
        glBindVertexArray(vao);
        if (changed) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            changed = false;
        }
        glDrawArrays(GL_TRIANGLES, 0, VERTICES_PER_CHUNK);
        glBindVertexArray(0);
    }

    void destroy() {
        glBindVertexArray(0);
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        MemoryUtil.memFree(buffer);
    }
}
