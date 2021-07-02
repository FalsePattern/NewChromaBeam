package xyz.chromabeam.engine.render.beam;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.engine.beam.Beam;

public interface BeamDrawer {
    void clear();
    void drawBeam(Beam beam);
    void removeBeam(int x, int y, Direction direction);
    void removeAll(int x, int y);
}
