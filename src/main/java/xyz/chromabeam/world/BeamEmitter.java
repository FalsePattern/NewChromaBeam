package xyz.chromabeam.world;

import xyz.chromabeam.beam.Direction;

public interface BeamEmitter {
    void emit(Direction direction, float red, float green, float blue);
}
