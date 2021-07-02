package xyz.chromabeam.world;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.Component;

public interface World2D extends BeamResolver {
    Component set(int x, int y, Direction direction, boolean flipped, Component component);
    Component get(int x, int y);
    Component remove(int x, int y);
    void forceTick(int x, int y);
    void update();
    ComponentTransform<Component> getTransform(int x, int y, ComponentTransform<Component> buffer);

}
