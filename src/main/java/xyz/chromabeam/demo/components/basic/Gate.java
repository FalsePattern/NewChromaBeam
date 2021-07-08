package xyz.chromabeam.demo.components.basic;

import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.*;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import xyz.chromabeam.world.BeamEmitter;

public class Gate extends Component implements BeamConsumer, BeamProducer, UserInteractive {
    private final Vector3f input = new Vector3f();
    private final Vector3f output = new Vector3f();
    private boolean sw = false;
    private boolean inverted = false;
    private boolean changed = false;
    private boolean emit = false;

    public Gate() {
        super("Gate", "gate", 2);
    }

    @Override
    public void initialize(TextureAtlas atlas) {
        super.initialize(atlas);
        setActiveTexture(4);
    }

    @Override
    public void incomingBeam(Direction direction, float red, float green, float blue) {
        switch (direction) {
            case RIGHT -> {
                changed = true;
                input.set(red, green, blue);
            }
            case DOWN -> {
                changed = true;
                sw = Math.max(red, Math.max(green, blue)) >= 0.5f;
            }
        }
    }

    @Override
    public void emitBeams(BeamEmitter beamEmitter) {
        beamEmitter.emit(Direction.RIGHT, output.x, output.y, output.z);
        emit = false;
    }

    @Override
    public boolean wantEmit() {
        return emit;
    }

    @Override
    public void tick() {
        setActiveTexture((inverted ? 2 : 0) + (sw ? 1 : 0));
        if (sw != inverted) {
            output.set(input);
        } else {
            output.set(0);
        }
        emit = changed;
        changed = false;
    }

    @Override
    public Vector4f getColorMaskColor(int mask, Vector4f buffer) {
        return mask == 0 ? buffer.set(input, 1) : sw ? buffer.set(1) : buffer.set(0, 0, 0, 1);
    }

    @Override
    public void mouseInteraction() {
        inverted = !inverted;
        changed = true;
    }
}
