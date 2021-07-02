package xyz.chromabeam.world;

import org.joml.Vector2i;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.ComponentI;

public class ComponentTransform<T extends ComponentI> {
    T component;

    final Vector2i position = new Vector2i();
    Direction direction;
    boolean flipped;

    ComponentTransform<T> with(T component, int x, int y, Direction direction, boolean flipped) {
        this.component = component;
        this.position.set(x, y);
        this.direction = direction;
        this.flipped = flipped;
        return this;
    }

    ComponentTransform<T> with(ComponentTransform<T> other) {
        this.component = other.component;
        this.position.set(other.position);
        this.direction = other.direction;
        this.flipped = other.flipped;
        return this;
    }
}
