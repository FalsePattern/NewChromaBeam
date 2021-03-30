package moe.falsepattern.chromabeam.world;

import moe.falsepattern.chromabeam.beam.Direction;

public interface BeamResolver {
    void scheduleBeam(int x, int y, Direction direction, float red, float green, float blue);
}
