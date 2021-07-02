package xyz.chromabeam.util.storage;

import org.joml.Vector2i;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.engine.beam.Beam;
import xyz.chromabeam.util.Cache;

public class BeamMap {
    private final Container2D<Beam[]> neighborStorage = new NativeContainer2D<>(Beam[][]::new, Beam[][][]::new, Beam[][][][]::new);
    private final Cache<Beam[]> neighborCache = new Cache<>(() -> new Beam[4], Beam[][]::new);
    private final Cache<Beam> beamCache = new Cache<>(Beam::new, Beam[]::new);

    private final Vector2i neighborPosCache = new Vector2i();

    public Beam get(int x, int y, Direction direction) {
        return neighborStorage.get(x, y)[direction.ordinal()];
    }

    public Beam[] get(int x, int y) {
        return neighborStorage.get(x, y);
    }

    private void setupConnection(Beam[] a, Beam[] b, int aX, int aY, int bX, int bY, int aI, int bI) {
        var aToB = a[aI];
        var bToA = b[bI];
        aToB.sourceX = bToA.x = aX;
        aToB.sourceY = bToA.y = aY;
        aToB.x = bToA.sourceX = bX;
        aToB.y = bToA.sourceY = bY;
        aToB.direction = Direction.values()[aI];
        bToA.direction = Direction.values()[bI];
        aToB.infinite = bToA.infinite = false;
    }

    private void setupNeighbor(Beam[] cell, int x, int y, int selfI, int neighborI) {
        var neighbor = neighborStorage.get(neighborPosCache.x, neighborPosCache.y);
        setupConnection(cell, neighbor, x, y, neighborPosCache.x, neighborPosCache.y, selfI, neighborI);
    }

    private void setupInfiniteBeam(Beam[] cell, int x, int y, int selfI) {
        var beam = cell[selfI] == null ? cell[selfI] = beamCache.getOrCreate() : cell[selfI];
        beam.x = beam.sourceX = x;
        beam.y = beam.sourceY = y;
        switch (beam.direction = Direction.values()[selfI]) {
            case RIGHT -> beam.x = Integer.MAX_VALUE;
            case DOWN -> beam.y = Integer.MAX_VALUE;
            case LEFT -> beam.x = Integer.MIN_VALUE;
            case UP -> beam.y = Integer.MIN_VALUE;
        }
        beam.infinite = true;
    }

    public void setup(int x, int y) {
        if (!neighborStorage.isEmpty(x, y)) throw new IllegalArgumentException();
        var cell = neighborCache.getOrCreate();
        for (int i = 0; i < 4; i++) cell[i] = beamCache.getOrCreate();
        neighborStorage.set(x, y, cell);
        if (neighborStorage.getRight(x, y, neighborPosCache)) {
            setupNeighbor(cell, x, y, 0, 2);
        } else {
            setupInfiniteBeam(cell, x, y, 0);
        }
        if (neighborStorage.getDown(x, y, neighborPosCache)) {
            setupNeighbor(cell, x, y, 1, 3);
        } else {
            setupInfiniteBeam(cell, x, y, 1);
        }
        if (neighborStorage.getLeft(x, y, neighborPosCache)) {
            setupNeighbor(cell, x, y, 2, 0);
        } else {
            setupInfiniteBeam(cell, x, y, 2);
        }
        if (neighborStorage.getUp(x, y, neighborPosCache)) {
            setupNeighbor(cell, x, y, 3, 1);
        } else {
            setupInfiniteBeam(cell, x, y, 3);
        }
    }

    public void removeAll(int x, int y) {
        var cell = neighborStorage.remove(x, y);
        var right = cell[0].infinite ? null : neighborStorage.get(cell[0].x, cell[0].y);
        var down = cell[1].infinite ? null : neighborStorage.get(cell[1].x, cell[1].y);
        var left = cell[2].infinite ? null : neighborStorage.get(cell[2].x, cell[2].y);
        var up = cell[3].infinite ? null : neighborStorage.get(cell[3].x, cell[3].y);
        if (left != null && right != null) {
            setupConnection(left, right, cell[2].x, cell[2].y, cell[0].x, cell[0].y, 0, 2);
        } else if (left != null) {
            setupInfiniteBeam(left, cell[2].x, cell[2].y, 0);
        } else if (right != null) {
            setupInfiniteBeam(right, cell[0].x, cell[0].y, 2);
        }
        if (up != null && down != null) {
            setupConnection(up, down, cell[3].x, cell[3].y, cell[1].x, cell[1].y, 1, 3);
        } else if (up != null) {
            setupInfiniteBeam(up, cell[3].x, cell[3].y, 1);
        } else if (down != null) {
            setupInfiniteBeam(down, cell[1].x, cell[1].y, 3);
        }
        for (int i = 0; i < 4; i++) {
            cell[i].red = 0;
            cell[i].green = 0;
            cell[i].blue = 0;
            beamCache.put(cell[i]);
            cell[i] = null;
        }
        neighborCache.put(cell);
    }
}
