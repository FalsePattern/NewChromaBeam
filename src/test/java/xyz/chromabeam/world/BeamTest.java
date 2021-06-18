package xyz.chromabeam.world;

import xyz.chromabeam.TestUtil;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class BeamTest {
    @Test
    public void testBeamInteraction() {
        System.out.println("Testing basic beam collision detection");
        var world = new World2D(null, null);
        var prod1 = new BeamProducer(TestUtil.colorToVector(Color.RED));
        var prod2 = new BeamProducer(TestUtil.colorToVector(Color.GREEN));
        var prod3 = new BeamProducer(TestUtil.colorToVector(Color.BLUE));
        var prod4 = new BeamProducer(TestUtil.colorToVector(Color.WHITE));
        var cons1 = new BeamConsumer(TestUtil.colorToVector(Color.RED));
        var cons2 = new BeamConsumer(TestUtil.colorToVector(Color.GREEN));
        var cons3 = new BeamConsumer(TestUtil.colorToVector(Color.BLUE));
        var cons4 = new BeamConsumer(TestUtil.colorToVector(Color.WHITE));
        world.set(1, 0, Direction.RIGHT, false, prod1);
        world.set(0, 1, Direction.DOWN, false, prod2);
        world.set(-1, 0, Direction.LEFT, false, prod3);
        world.set(0, -1, Direction.UP, false, prod4);
        world.set(100, 0, Direction.RIGHT, false, cons1);
        world.set(0, 100, Direction.DOWN, false, cons2);
        world.set(-100, 0, Direction.LEFT, false, cons3);
        world.set(0, -100, Direction.UP, false, cons4);
        world.update();
        assertTrue(cons1.match, "Collision check 1 failed");
        assertTrue(cons2.match, "Collision check 2 failed");
        assertTrue(cons3.match, "Collision check 3 failed");
        assertTrue(cons4.match, "Collision check 4 failed");
    }

    @Test
    public void testBeamZeroRemove() {
        System.out.println("Testing zero-beam and negative beam removal");
        var world = new World2D(null, null);
        var prod0 = new BeamProducer(new Vector3f(0, 0, 0));
        var prod1 = new BeamProducer(new Vector3f(-1, 0, 0));
        var prod2 = new BeamProducer(new Vector3f(0, -1, 0));
        var prod3 = new BeamProducer(new Vector3f(-1, -1, 0));
        var prod4 = new BeamProducer(new Vector3f(0, 0, -1));
        var prod5 = new BeamProducer(new Vector3f(-1, 0, -1));
        var prod6 = new BeamProducer(new Vector3f(0, -1, -1));
        var prod7 = new BeamProducer(new Vector3f(-1, -1, -1));
        world.set(0, 0, Direction.LEFT, false, prod0);
        world.set(0, 1, Direction.LEFT, false, prod1);
        world.set(0, 2, Direction.LEFT, false, prod2);
        world.set(0, 3, Direction.LEFT, false, prod3);
        world.set(0, 4, Direction.LEFT, false, prod4);
        world.set(0, 5, Direction.LEFT, false, prod5);
        world.set(0, 6, Direction.LEFT, false, prod6);
        world.set(0, 7, Direction.LEFT, false, prod7);
        for (int i = 0; i < 8; i++) {
            var cons = new NullConsumer();
            world.set(-2, i, Direction.LEFT, false, cons);
        }
        assertDoesNotThrow(world::update);
        //Now, to make sure the beams actually hit the targets...
        prod0.outCol.set(1, 1, 1);
        assertThrows(IllegalStateException.class, world::update);
    }

    @Test
    public void testClamp() {
        System.out.println("Testing negative beam channel clamping");
        var world = new World2D(null, null);
        var prod = new BeamProducer(new Vector3f(-1, 0, 1));
        var cons = new BeamConsumer(new Vector3f(0, 0, 1));
        world.set(0, 0, Direction.RIGHT, false, prod);
        world.set(2, 0, Direction.RIGHT, false, cons);
        world.update();
        assertTrue(cons.match);
    }

    private static class BeamConsumer extends Component implements xyz.chromabeam.component.BeamConsumer {

        private final Vector3f desiredColor;

        boolean match = false;
        BeamConsumer(Vector3f desiredColor) {
            this.desiredColor = new Vector3f(desiredColor);
        }

        @Override
        public void incomingBeam(Direction direction, float red, float green, float blue) {
            match = direction == Direction.RIGHT && desiredColor.x == red && desiredColor.y == green && desiredColor.z == blue;
            if (!match) {
                System.out.println("Expected: (" + desiredColor.x + ", " + desiredColor.y + ", " + desiredColor.z + ")");
                System.out.println("Actual: (" + red + ", " + green + ", " + blue + ")");
            }
        }

        @Override
        public void initialize(TextureAtlas atlas) {
            super.initialize("Consumer", atlas, "consumer");
        }
    }

    private static class BeamProducer extends Component implements xyz.chromabeam.component.BeamProducer {

        private final Vector3f outCol;

        BeamProducer(Vector3f outputColor) {
            this.outCol = new Vector3f(outputColor);
        }

        @Override
        public void emitBeams(BeamEmitter beamEmitter) {
            beamEmitter.emit(Direction.RIGHT, outCol.x, outCol.y, outCol.z);
        }

        @Override
        public void initialize(TextureAtlas atlas) {
            super.initialize("Producer", atlas, "producer");
        }

    }

    private static class NullConsumer extends Component implements xyz.chromabeam.component.BeamConsumer {
        @Override
        public void incomingBeam(Direction direction, float red, float green, float blue) {

            throw new IllegalStateException("Beam received by null consumer component");
        }

        @Override
        public void initialize(TextureAtlas atlas) {
            super.initialize("Null", atlas, "null");
        }

    }
}