package xyz.chromabeam.demo.components.basic;

import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.BeamProducer;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import xyz.chromabeam.world.BeamEmitter;

public class Emitter extends Component implements BeamProducer {

    public Emitter() {
        super("Emitter", "emitter", 1);
    }

    @Override
    public void emitBeams(BeamEmitter beamEmitter) {
        beamEmitter.emit(Direction.RIGHT, 1, 1, 1);
    }

    @Override
    public Vector4f getColorMaskColor(int mask, Vector4f buffer) {
        return buffer.set(1);
    }

    @Override
    public boolean wantEmit() {
        return false;
    }

    @Override
    public void tick() {
    }
}
