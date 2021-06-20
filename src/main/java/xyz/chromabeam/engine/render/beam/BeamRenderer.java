package xyz.chromabeam.engine.render.beam;

import org.joml.Vector2i;
import xyz.chromabeam.engine.render.world.WorldRenderer;
import xyz.chromabeam.engine.render.buffer.VertexArray;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class BeamRenderer extends WorldRenderer implements BeamDrawer{
    private static final int VERTICES_PER_BEAM = 2;
    private static final int FLOATS_PER_VERTEX = 5;
    private static final int FLOATS_PER_BEAM = VERTICES_PER_BEAM * FLOATS_PER_VERTEX;
    private static final int BEAMS_PER_DRAW_CALL = 2048;
    private final VertexArray vb;
    private final int beamsPerDraw;
    private int capacity;
    private int beams = 0;

    private final float[] BUF = new float[FLOATS_PER_BEAM];
    private FloatBuffer growableBuffer;
    public BeamRenderer() {
        this(BEAMS_PER_DRAW_CALL);
    }

    public BeamRenderer(int beamsPerDrawCall) {
        super("beam", "color4");
        this.capacity = this.beamsPerDraw = beamsPerDrawCall;
        vb = new VertexArray(beamsPerDrawCall * VERTICES_PER_BEAM, 2, 3);
        growableBuffer = MemoryUtil.memAllocFloat(beamsPerDrawCall);
    }

    public void drawBeam(Vector2i a, Vector2i b, Vector3f color) {
        drawBeam(a.x, a.y, b.x, b.y, color.x, color.y, color.z);
    }

    public void drawBeam(int x1, int y1, int x2, int y2, float r, float g, float b) {
        if (beams == capacity) {
            capacity *= 1.5f;
            growableBuffer = MemoryUtil.memRealloc(growableBuffer, capacity);
        }
        BUF[0] = x1 + 0.5f;
        BUF[1] = y1 + 0.5f;
        BUF[5] = x2 + 0.5f;
        BUF[6] = y2 + 0.5f;
        BUF[2] = BUF[7] = r;
        BUF[3] = BUF[8] = g;
        BUF[4] = BUF[9] = b;
        growableBuffer.put(beams++ * FLOATS_PER_BEAM, BUF);
    }

    @Override
    protected void renderContent() {
        GL33C.glLineWidth(2);
        int beamsDrawn = 0;
        vb.bind();
        while (beams > 0) {
            int inCurrentDraw = Math.min(beams, beamsPerDraw);
            vb.getWriteBuffer().put(0, growableBuffer, beamsDrawn * FLOATS_PER_BEAM, Math.min(beams, beamsPerDraw) * FLOATS_PER_BEAM);
            vb.sync();
            GL33C.glDrawArrays(GL11C.GL_LINES, 0, inCurrentDraw * VERTICES_PER_BEAM);
            beams -= inCurrentDraw;
            beamsDrawn += inCurrentDraw;
        }
        vb.unbind();
    }
}
