package moe.falsepattern.chromabeam.world;

import moe.falsepattern.chromabeam.beam.BeamColor;
import moe.falsepattern.chromabeam.beam.Direction;
import moe.falsepattern.chromabeam.component.ComponentI;
import moe.falsepattern.engine.render.texture.TextureRegionI;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class BeamTest {
    @Test
    public void testBeamInteraction() {
        var world = new World2D(null);
        var prod1 = new BeamProducer(Direction.UP, BeamColor.RED);
        var prod2 = new BeamProducer(Direction.RIGHT, BeamColor.GREEN);
        var prod3 = new BeamProducer(Direction.DOWN, BeamColor.BLUE);
        var prod4 = new BeamProducer(Direction.LEFT, BeamColor.WHITE);
        var cons1 = new BeamConsumer(Direction.UP, BeamColor.RED);
        var cons2 = new BeamConsumer(Direction.RIGHT, BeamColor.GREEN);
        var cons3 = new BeamConsumer(Direction.DOWN, BeamColor.BLUE);
        var cons4 = new BeamConsumer(Direction.LEFT, BeamColor.WHITE);
        world.set(0, -1, prod1);
        world.set(1, 0, prod2);
        world.set(0, 1, prod3);
        world.set(-1, 0, prod4);
        world.set(0, -100, cons1);
        world.set(100, 0, cons2);
        world.set(0, 100, cons3);
        world.set(-100, 0, cons4);
        world.tick();
        assertTrue(cons1.match);
        assertTrue(cons2.match);
        assertTrue(cons3.match);
        assertTrue(cons4.match);
    }

    private static class BeamConsumer implements ComponentI{

        private final Direction desDir;
        private final BeamColor desCol;

        boolean match = false;
        BeamConsumer(Direction desiredDirection, BeamColor desiredColor) {
            this.desDir = desiredDirection;
            this.desCol = desiredColor;
        }
        @Override
        public void incomingBeam(Direction direction, BeamColor color) {
            match = direction == desDir && color == desCol;
        }

        @Override
        public void tick(BiConsumer<Direction, BeamColor> beamEmitter) {

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
        private final BeamColor outCol;

        BeamProducer(Direction outputDirection, BeamColor outputColor) {
            this.outDir = outputDirection;
            this.outCol = outputColor;
        }
        @Override
        public void incomingBeam(Direction direction, BeamColor color) {
            throw new IllegalArgumentException("Beam producer hit by beam?");
        }

        @Override
        public void tick(BiConsumer<Direction, BeamColor> beamEmitter) {
            beamEmitter.accept(outDir, outCol);
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
}