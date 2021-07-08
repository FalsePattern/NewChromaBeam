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
        var world = new FlatWorld2D(null, null);
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


    private static class BeamConsumer extends Component implements xyz.chromabeam.component.BeamConsumer {

        private final Vector3f desiredColor;

        boolean match = false;
        BeamConsumer(Vector3f desiredColor) {
            super("Consumer", "consumer", 0);
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
        public void tick() {

        }
    }

    private static class BeamProducer extends Component implements xyz.chromabeam.component.BeamProducer {

        private final Vector3f outCol;

        BeamProducer(Vector3f outputColor) {
            super("Producer", "producer", 0);
            this.outCol = new Vector3f(outputColor);
        }

        @Override
        public void emitBeams(BeamEmitter beamEmitter) {
            beamEmitter.emit(Direction.RIGHT, outCol.x, outCol.y, outCol.z);
        }

        @Override
        public boolean wantEmit() {
            return true;
        }

        @Override
        public void tick() {

        }
    }
}