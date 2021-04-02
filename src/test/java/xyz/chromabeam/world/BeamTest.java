package xyz.chromabeam.world;

import xyz.chromabeam.TestUtil;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.ComponentI;
import xyz.chromabeam.engine.render.texture.TextureRegionI;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class BeamTest {
    @Test
    public void testBeamInteraction() {
        System.out.println("Testing basic beam collision detection");
        var world = new World2D(null);
        var prod1 = new BeamProducer(Direction.UP, TestUtil.colorToVector(Color.RED));
        var prod2 = new BeamProducer(Direction.RIGHT, TestUtil.colorToVector(Color.GREEN));
        var prod3 = new BeamProducer(Direction.DOWN, TestUtil.colorToVector(Color.BLUE));
        var prod4 = new BeamProducer(Direction.LEFT, TestUtil.colorToVector(Color.WHITE));
        var cons1 = new BeamConsumer(Direction.UP, TestUtil.colorToVector(Color.RED));
        var cons2 = new BeamConsumer(Direction.RIGHT, TestUtil.colorToVector(Color.GREEN));
        var cons3 = new BeamConsumer(Direction.DOWN, TestUtil.colorToVector(Color.BLUE));
        var cons4 = new BeamConsumer(Direction.LEFT, TestUtil.colorToVector(Color.WHITE));
        world.set(0, -1, prod1);
        world.set(1, 0, prod2);
        world.set(0, 1, prod3);
        world.set(-1, 0, prod4);
        world.set(0, -100, cons1);
        world.set(100, 0, cons2);
        world.set(0, 100, cons3);
        world.set(-100, 0, cons4);
        world.tick();
        assertTrue(cons1.match, "Collision check 1 failed");
        assertTrue(cons2.match, "Collision check 2 failed");
        assertTrue(cons3.match, "Collision check 3 failed");
        assertTrue(cons4.match, "Collision check 4 failed");
    }

    @Test
    public void testBeamZeroRemove() {
        System.out.println("Testing zero-beam and negative beam removal");
        var world = new World2D(null);
        var prod0 = new BeamProducer(Direction.LEFT, new Vector3f(0, 0, 0));
        var prod1 = new BeamProducer(Direction.LEFT, new Vector3f(-1, 0, 0));
        var prod2 = new BeamProducer(Direction.LEFT, new Vector3f(0, -1, 0));
        var prod3 = new BeamProducer(Direction.LEFT, new Vector3f(-1, -1, 0));
        var prod4 = new BeamProducer(Direction.LEFT, new Vector3f(0, 0, -1));
        var prod5 = new BeamProducer(Direction.LEFT, new Vector3f(-1, 0, -1));
        var prod6 = new BeamProducer(Direction.LEFT, new Vector3f(0, -1, -1));
        var prod7 = new BeamProducer(Direction.LEFT, new Vector3f(-1, -1, -1));
        world.set(0, 0, prod0);
        world.set(0, 1, prod1);
        world.set(0, 2, prod2);
        world.set(0, 3, prod3);
        world.set(0, 4, prod4);
        world.set(0, 5, prod5);
        world.set(0, 6, prod6);
        world.set(0, 7, prod7);
        for (int i = 0; i < 8; i++) {
            var cons = new NullConsumer();
            world.set(-2, i, cons);
        }
        assertDoesNotThrow(world::tick);
        //Now, to make sure the beams actually hit the targets...
        prod0.outCol.set(1, 1, 1);
        assertThrows(IllegalStateException.class, world::tick);
    }

    @Test
    public void testClamp() {
        System.out.println("Testing negative beam channel clamping");
        var world = new World2D(null);
        var prod = new BeamProducer(Direction.RIGHT, new Vector3f(-1, 0, 1));
        var cons = new BeamConsumer(Direction.RIGHT, new Vector3f(0, 0, 1));
        world.set(0, 0, prod);
        world.set(2, 0, cons);
        world.tick();
        assertTrue(cons.match);
    }

    private static class BeamConsumer implements ComponentI {

        private final Direction desDir;
        private final Vector3f desiredColor;

        boolean match = false;
        BeamConsumer(Direction desiredDirection, Vector3f desiredColor) {
            this.desDir = desiredDirection;
            this.desiredColor = new Vector3f(desiredColor);
        }
        @Override
        public void incomingBeam(Direction direction, float red, float green, float blue) {
            match = direction == desDir && desiredColor.x == red && desiredColor.y == green && desiredColor.z == blue;
            if (!match) {
                System.out.println("Expected: (" + desiredColor.x + ", " + desiredColor.y + ", " + desiredColor.z + ")");
                System.out.println("Actual: (" + red + ", " + green + ", " + blue + ")");
            }
        }

        @Override
        public void tick(BeamEmitter beamEmitter) {

        }

        @Override
        public BeamConsumer copy() {
            return null;
        }

        @Override
        public boolean graphicsChanged() {
            return false;
        }

        @Override
        public TextureRegionI getTexture() {
            return null;
        }
    }

    private static class BeamProducer implements ComponentI{

        private final Direction outDir;
        private final Vector3f outCol;

        BeamProducer(Direction outputDirection, Vector3f outputColor) {
            this.outDir = outputDirection;
            this.outCol = new Vector3f(outputColor);
        }
        @Override
        public void incomingBeam(Direction direction, float red, float green, float blue) {
            throw new IllegalArgumentException("Beam producer hit by beam?");
        }

        @Override
        public void tick(BeamEmitter beamEmitter) {
            beamEmitter.emit(outDir, outCol.x, outCol.y, outCol.z);
        }

        @Override
        public BeamProducer copy() {
            return null;
        }

        @Override
        public boolean graphicsChanged() {
            return false;
        }

        @Override
        public TextureRegionI getTexture() {
            return null;
        }
    }

    private static class NullConsumer implements ComponentI{
        @Override
        public void incomingBeam(Direction direction, float red, float green, float blue) {
            throw new IllegalStateException("Beam received by null consumer component");
        }

        @Override
        public void tick(BeamEmitter beamEmitter) {

        }

        @Override
        public NullConsumer copy() {
            return null;
        }

        @Override
        public boolean graphicsChanged() {
            return false;
        }

        @Override
        public TextureRegionI getTexture() {
            return null;
        }
    }
}