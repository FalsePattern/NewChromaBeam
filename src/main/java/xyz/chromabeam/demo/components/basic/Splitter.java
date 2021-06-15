package xyz.chromabeam.demo.components.basic;

import org.joml.Vector3f;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.component.BeamInstantManipulator;
import xyz.chromabeam.component.Tickable;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import xyz.chromabeam.world.BeamEmitter;

public class Splitter extends Component implements BeamInstantManipulator, Tickable {
    private final Vector3f[] cache = new Vector3f[] {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
    private static final Direction[] reflectionMap = new Direction[4];
    static {
        reflectionMap[Direction.RIGHT.ordinal()] = Direction.UP;
        reflectionMap[Direction.DOWN.ordinal()] = Direction.LEFT;
        reflectionMap[Direction.LEFT.ordinal()] = Direction.DOWN;
        reflectionMap[Direction.UP.ordinal()] = Direction.RIGHT;
    }

    private final Vector3f tmp = new Vector3f();
    @Override
    public void initialize(TextureAtlas atlas) {
        initialize("Splitter", atlas, "splitter");
    }

    @Override
    public void copy(Component other) {
        super.copy(other);
        var o = (Splitter) other;
        for (int i = 0; i < 4; i++) {
            o.cache[i].set(cache[i]);
        }
    }

    @Override
    public void incomingBeam(Direction direction, float red, float green, float blue, BeamEmitter instantOutput) {
        var c = cache[direction.ordinal()];
        if (!tmp.set(red, green, blue).max(c).equals(c, 0)) {
            c.set(tmp);
            instantOutput.emit(direction, c.x, c.y, c.z);
            instantOutput.emit(reflectionMap[direction.ordinal()], c.x, c.y, c.z);
        }
    }

    @Override
    public void tick() {
        for (int i = 0; i < 4; i++) {
            cache[i].set(0);
        }
    }
}
