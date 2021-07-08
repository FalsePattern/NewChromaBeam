package xyz.chromabeam.demo.components.basic;

import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.BeamConsumer;
import xyz.chromabeam.component.BeamProducer;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.component.Tickable;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import xyz.chromabeam.world.BeamEmitter;

public class Delayer extends Component implements BeamConsumer, BeamProducer, Tickable {
    private final Vector3f value = new Vector3f();
    private boolean updated = false;

    public Delayer() {
        super("Delayer", "delayer", 1);
    }

    @Override
    public void emitBeams(BeamEmitter beamEmitter) {
        beamEmitter.emit(Direction.RIGHT, value.x, value.y, value.z);
        updated = false;
    }

    @Override
    public boolean wantEmit() {
        return updated;
    }

    @Override
    public void incomingBeam(Direction direction, float red, float green, float blue) {
        if (direction == Direction.RIGHT) {
            value.set(red, green, blue);
        }
    }

    @Override
    public Vector4f getColorMaskColor(int mask, Vector4f buffer) {
        return buffer.set(value, 1);
    }

    @Override
    public void tick() {
        updated = true;
    }

    @Override
    public void copy(Component other) {
        super.copy(other);
        var o = (Delayer) other;
        o.value.set(value);
    }
}
