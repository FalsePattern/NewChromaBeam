package xyz.chromabeam.engine.beam;

import xyz.chromabeam.beam.Direction;

public class Beam {
    public int x;
    public int y;
    public int sourceX;
    public int sourceY;
    public Direction direction;
    public float red;
    public float green;
    public float blue;
    public boolean infinite;

    public Beam copyTo(Beam other) {
        other.x = x;
        other.y = y;
        other.sourceX = sourceX;
        other.sourceY = sourceY;
        other.direction = direction;
        other.red = red;
        other.green = green;
        other.blue = blue;
        other.infinite = infinite;
        return other;
    }
}
