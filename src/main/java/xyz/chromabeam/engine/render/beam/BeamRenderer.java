package xyz.chromabeam.engine.render.beam;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.engine.beam.Beam;
import xyz.chromabeam.engine.render.world.WorldRenderer;
import xyz.chromabeam.engine.render.buffer.VertexArray;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryUtil;
import xyz.chromabeam.util.Cache;
import xyz.chromabeam.util.storage.Container2D;
import xyz.chromabeam.util.storage.NativeContainer2D;

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
    private final Container2D<Beam[]> scheduledBeams = new NativeContainer2D<>(Beam[][]::new, Beam[][][]::new, Beam[][][][]::new);
    private final Cache<Beam> scheduledBeamCache = new Cache<>(Beam::new, Beam[]::new);
    private final Cache<Beam[]> groupCache = new Cache<>(() -> new Beam[4], Beam[][]::new);

    private FloatBuffer growableBuffer;
    public BeamRenderer() {
        this(BEAMS_PER_DRAW_CALL);
    }

    public BeamRenderer(int beamsPerDrawCall) {
        super("beam", "color4");
        this.capacity = this.beamsPerDraw = beamsPerDrawCall;
        vb = new VertexArray(VertexArray.DrawMethod.LINES, beamsPerDrawCall * VERTICES_PER_BEAM, 2, 3);
        growableBuffer = MemoryUtil.memCallocFloat(beamsPerDrawCall * FLOATS_PER_BEAM);
        vb.unbind();
    }

    @Override
    public void clear() {
        growableBuffer.clear();
    }

    public void drawBeam(Beam beam) {
        var cell = scheduledBeams.getOrCompute(beam.sourceX, beam.sourceY, groupCache::getOrCreate);
        int i = beam.direction.ordinal();
        if (cell[i] == null) {
            cell[i] = beam.copyTo(scheduledBeamCache.getOrCreate());
        } else {
            beam.copyTo(cell[i]);
        }
    }

    @Override
    public void removeBeam(int x, int y, Direction direction) {
        var cell = scheduledBeams.remove(x, y);
        if (cell != null) {
            int i = direction.ordinal();
            if (cell[i] != null) {
                scheduledBeamCache.put(cell[i]);
            }
            cell[i] = null;
        }
        groupCache.put(cell);
    }

    @Override
    public void removeAll(int x, int y) {
        var cell = scheduledBeams.remove(x, y);
        if (cell != null) {
            for (int i = 0; i < 4; i++) {
                if (cell[i] != null) {
                    scheduledBeamCache.put(cell[i]);
                }
                cell[i] = null;
            }
        }
        groupCache.put(cell);
    }


    private float camTop;
    private float camBottom;
    private float camLeft;
    private float camRight;
    private int camLastUpdated = -1;
    @Override
    protected void renderContent() {
        if (camera.lastUpdated() != camLastUpdated) {
            camTop = camera.top() - 0.5f;
            camBottom = camera.bottom() - 0.5f;
            camLeft = camera.left() - 0.5f;
            camRight = camera.right() - 0.5f;
            camLastUpdated = camera.lastUpdated();
        }
        for (var group: scheduledBeams) {
            for (int i = 0; i < 4; i++) {
                var beam = group[i];
                if (beam != null && !((beam.sourceX < camLeft && beam.x < camLeft)
                        || (beam.sourceX > camRight && beam.x > camRight)
                        || (beam.sourceY > camTop && beam.y > camTop)
                        || (beam.sourceY < camBottom && beam.y < camBottom)) && (beam.red > 0 || beam.green > 0 || beam.blue > 0)) {

                    if (beams >= capacity) {
                        capacity *= 2;
                        growableBuffer = MemoryUtil.memRealloc(growableBuffer, capacity * FLOATS_PER_BEAM);
                    }
                    beams++;
                    growableBuffer.put(beam.sourceX + 0.5f);
                    growableBuffer.put(beam.sourceY + 0.5f);
                    growableBuffer.put(beam.red);
                    growableBuffer.put(beam.green);
                    growableBuffer.put(beam.blue);
                    growableBuffer.put(beam.x + 0.5f);
                    growableBuffer.put(beam.y + 0.5f);
                    growableBuffer.put(beam.red);
                    growableBuffer.put(beam.green);
                    growableBuffer.put(beam.blue);
                }
            }
        }
        growableBuffer.flip();
        GL33C.glLineWidth(2);
        int beamsDrawn = 0;
        vb.bind();
        while (beams > 0) {
            var buf = vb.getVertexBuffer();
            int inCurrentDraw = Math.min(beams, beamsPerDraw);
            buf.clear();
            buf.put(0, growableBuffer, beamsDrawn * FLOATS_PER_BEAM, inCurrentDraw * FLOATS_PER_BEAM);
            if (inCurrentDraw < beamsPerDraw) {
                int c = inCurrentDraw;
                buf.position(c * FLOATS_PER_BEAM);
                while (c < beamsPerDraw) {
                    for (int i = 0; i < FLOATS_PER_BEAM; i++) {
                        buf.put(0);
                    }
                    c++;
                }
                buf.flip();
            }
            vb.sync();
            vb.draw();
            beams -= inCurrentDraw;
            beamsDrawn += inCurrentDraw;
        }
        vb.unbind();
        clear();
    }

    @Override
    public void destroy() {
        super.destroy();
        vb.destroy();
    }
}
