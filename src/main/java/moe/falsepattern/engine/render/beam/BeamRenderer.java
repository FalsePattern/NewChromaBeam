package moe.falsepattern.engine.render.beam;

import moe.falsepattern.engine.render.world.WorldRenderer;
import moe.falsepattern.engine.render.VertexBuffer;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class BeamRenderer extends WorldRenderer {
    private static final int VERTICES_PER_BEAM = 2;
    private static final int FLOATS_PER_VERTEX = 5;
    private static final int FLOATS_PER_BEAM = VERTICES_PER_BEAM * FLOATS_PER_VERTEX;
    private static final int BEAMS_PER_DRAW_CALL = 2048;
    private final VertexBuffer vb;
    private final int beamsPerDraw;
    private int capacity;
    private int beams = 0;

    private final float[] BUF = new float[FLOATS_PER_BEAM];
    private FloatBuffer growableBuffer;
    public BeamRenderer() {
        this(BEAMS_PER_DRAW_CALL);
    }

    public BeamRenderer(int beamsPerDrawCall) {
        super("beam");
        this.capacity = this.beamsPerDraw = beamsPerDrawCall;
        vb = new VertexBuffer(beamsPerDrawCall * VERTICES_PER_BEAM, 2, 3);
        growableBuffer = MemoryUtil.memAllocFloat(beamsPerDrawCall);
    }

    public void setBeam(Vector2f a, Vector2f b, Vector3f color) {
        if (beams == capacity) {
            capacity *= 1.5f;
            growableBuffer = MemoryUtil.memRealloc(growableBuffer, capacity);
        }
        BUF[0] = a.x + 0.5f;
        BUF[1] = a.y + 0.5f;
        BUF[5] = b.x + 0.5f;
        BUF[6] = b.y + 0.5f;
        BUF[2] = BUF[7] = color.x;
        BUF[3] = BUF[8] = color.y;
        BUF[4] = BUF[9] = color.z;
        growableBuffer.put(beams++ * FLOATS_PER_BEAM, BUF);
    }

    @Override
    protected void renderContent() {
        GL33C.glLineWidth(2);
        int beamsDrawn = 0;
        while (beams > 0) {
            int inCurrentDraw = Math.min(beams, beamsPerDraw);
            vb.getBufferForWriting().put(0, growableBuffer, beamsDrawn * FLOATS_PER_BEAM, Math.min(beams, beamsPerDraw) * FLOATS_PER_BEAM);
            vb.bind();
            GL33C.glDrawArrays(GL11C.GL_LINES, 0, inCurrentDraw * VERTICES_PER_BEAM);
            vb.unbind();
            beams -= inCurrentDraw;
            beamsDrawn += inCurrentDraw;
        }
    }
}
