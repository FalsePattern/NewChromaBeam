package xyz.chromabeam.demo.components.basic;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.component.BeamInstantManipulator;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import xyz.chromabeam.world.BeamEmitter;

public class Mirror extends Component implements BeamInstantManipulator {

    public Mirror() {
        super("Mirror", "mirror", 0);
    }

    @Override
    public void incomingBeam(Direction direction, float red, float green, float blue, BeamEmitter instantOutput) {
        switch (direction) {
            case RIGHT -> instantOutput.emit(Direction.DOWN, red, green, blue);
            case UP -> instantOutput.emit(Direction.LEFT, red, green, blue);
        }
    }
}
