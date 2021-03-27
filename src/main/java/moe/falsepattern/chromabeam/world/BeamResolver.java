package moe.falsepattern.chromabeam.world;

import moe.falsepattern.chromabeam.beam.BeamColor;
import moe.falsepattern.chromabeam.beam.Direction;

public interface BeamResolver {
    void scheduleBeam(int x, int y, Direction direction, BeamColor color);
}
