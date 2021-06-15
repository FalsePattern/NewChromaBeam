package xyz.chromabeam.demo.components.basic;

import org.joml.Vector3f;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.BeamConsumer;
import xyz.chromabeam.component.BeamProducer;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.component.Tickable;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import xyz.chromabeam.world.BeamEmitter;

public class Delayer extends Component implements BeamConsumer, BeamProducer, Tickable {
    private final Vector3f input = new Vector3f();
    private final Vector3f output = new Vector3f();
    private final Vector3f tmp = new Vector3f();

    @Override
    public void initialize(TextureAtlas atlas) {
        initialize("Delayer", atlas, "delayer");
    }

    @Override
    public void emitBeams(BeamEmitter beamEmitter) {
        beamEmitter.emit(Direction.RIGHT, output.x, output.y, output.z);
    }

    @Override
    public void incomingBeam(Direction direction, float red, float green, float blue) {
        if (direction == Direction.RIGHT) {
            input.max(tmp.set(red, green, blue));
        }
    }

    @Override
    public void tick() {
        output.set(input);
        input.set(0);
    }

    @Override
    public void copy(Component other) {
        super.copy(other);
        var o = (Delayer) other;
        o.input.set(input);
        o.output.set(output);
    }
}
