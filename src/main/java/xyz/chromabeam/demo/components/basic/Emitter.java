package xyz.chromabeam.demo.components.basic;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.BeamProducer;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import xyz.chromabeam.world.BeamEmitter;

public class Emitter extends Component implements BeamProducer {

    @Override
    public void initialize(TextureAtlas atlas) {
        initialize("Emitter", atlas, "emitter");
    }

    @Override
    public void emitBeams(BeamEmitter beamEmitter) {
        beamEmitter.emit(Direction.RIGHT, 1, 1, 1);
    }

    @Override
    public boolean wantEmit() {
        return false;
    }

    @Override
    public void tick() {
    }
}
