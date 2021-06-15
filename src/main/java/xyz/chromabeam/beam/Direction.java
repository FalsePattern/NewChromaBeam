package xyz.chromabeam.beam;

public enum Direction {
    RIGHT, DOWN, LEFT, UP;


    private static final Direction[] values = Direction.values();
    public Direction add(Direction other) {
        return values[(ordinal() + other.ordinal()) % 4];
    }

    public Direction sub(Direction other) {
        return values[((ordinal() - other.ordinal()) % 4 + 4) % 4];
    }

    public Direction applyFlip(boolean flipped) {
        if (flipped) {
            return switch (this) {
                case UP -> DOWN;
                case DOWN -> UP;
                default -> this;
            };
        } else {
            return this;
        }
    }
}
