package moe.falsepattern.chromabeam.world;

import moe.falsepattern.chromabeam.beam.Direction;

public interface BeamEmitter {
    void emit(Direction direction, float red, float green, float blue);
}
