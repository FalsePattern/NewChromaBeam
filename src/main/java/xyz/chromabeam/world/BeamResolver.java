package xyz.chromabeam.world;

import xyz.chromabeam.beam.Direction;

public interface BeamResolver {
    void scheduleBeam(int x, int y, Direction direction, float red, float green, float blue);
}
