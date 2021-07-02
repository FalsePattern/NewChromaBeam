package xyz.chromabeam.util.storage;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class NativeContainer2D<T> implements Container2D<T>{
    private T[][][] quadrants;
    private int size;
    private final List<T> contents = new ArrayList<>();
    private final Function<Integer, T[]> rowCreator;
    private final Function<Integer, T[][]> quadrantCreator;
    private final Function<Integer, T[][][]> storageCreator;

    public NativeContainer2D(Function<Integer, T[]> rowCreator, Function<Integer, T[][]> quadrantCreator, Function<Integer, T[][][]> storageCreator) {
        quadrants = storageCreator.apply(4);
        for (int i = 0; i < 4; i++) {
            quadrants[i] = quadrantCreator.apply(64);
        }
        this.rowCreator = rowCreator;
        this.quadrantCreator = quadrantCreator;
        this.storageCreator = storageCreator;
    }

    @Override
    public long getElementCount() {
        return size;
    }

    public T get(int x, int y) {
        var quadrant = quadrants[((y >>> 31) << 1) | (x >>> 31)];
        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (y < quadrant.length) {
            var row = quadrant[y];
            if (row != null && x < row.length) {
                return row[x];
            }
        }
        return null;
    }

    public T set(int x, int y, T data) {
        if (data == null) remove(x, y);
        var id = ((y >>> 31) << 1) | (x >>> 31);
        var quadrant = quadrants[id];
        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (quadrant.length <= y) {
            var oldQ = quadrant;
            quadrants[id] = quadrant = quadrantCreator.apply(y + 256);
            System.arraycopy(oldQ, 0, quadrant, 0, oldQ.length);
        }
        var row = quadrant[y];
        if (row == null) {
            quadrant[y] = row = rowCreator.apply(x + 256);
        } else if (row.length <= x) {
            var oldRow = row;
            quadrant[y] = row = rowCreator.apply(x + 256);
            System.arraycopy(oldRow, 0, row, 0, oldRow.length);
        }
        var old = row[x];
        row[x] = data;
        contents.add(data);
        if (old == null) {
            size++;
        } else {
            contents.remove(old);
        }
        return old;
    }

    public T remove(int x, int y) {
        var quadrant = quadrants[((y >>> 31) << 1) | (x >>> 31)];

        x = x < 0 ? ~x : x;
        y = y < 0 ? ~y : y;
        if (y < quadrant.length) {
            var row = quadrant[y];
            if (row != null && x < row.length) {
                var result = row[x];
                row[x] = null;
                if (result != null) {
                    contents.remove(result);
                    size--;
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty(int x, int y) {
        return get(x, y) == null;
    }

    public void clear() {
        quadrants = storageCreator.apply(4);
    }

    @Override
    public List<T> getNonNullUnordered() {
        return Collections.unmodifiableList(contents);
    }

    @Override
    public boolean isEmptyUp(int x, int y) {
        int xI = x >= 0 ? 0 : 1;
        x = x < 0 ? ~x : x;
        if (y > 0) {
            var quadrant = quadrants[xI];
            for (y = Math.min(quadrant.length - 1, y - 1); y >= 0; y--) {
                var line = quadrant[y];
                if (line != null && line.length > x && line[x] != null) return false;
            }
            y = 0;
        }
        var quadrant = quadrants[2 + xI];
        y = -y;
        for (; y < quadrant.length; y++) {
            var line = quadrant[y];
            if (line != null && line.length > x && line[x] != null) return false;
        }
        return true;
    }

    @Override
    public boolean isEmptyDown(int x, int y) {
        int xI = x >= 0 ? 0 : 1;
        x = x < 0 ? ~x : x;
        if (y < -1) {
            var quadrant = quadrants[2 + xI];
            for (y = Math.min(quadrant.length - 1, ~y - 1); y >= 0; y--) {
                var line = quadrant[y];
                if (line != null && line.length > x && line[x] != null) return false;
            }
            y = -1;
        }
        var quadrant = quadrants[xI];
        y++;
        for (; y < quadrant.length; y++) {
            var line = quadrant[y];
            if (line != null && line.length > x && line[x] != null) return false;
        }
        return true;
    }

    @Override
    public boolean isEmptyLeft(int x, int y) {
        int yI = y >= 0 ? 0 : 2;
        y = y < 0 ? ~y : y;
        if (x > 0) {
            var quadrant = quadrants[yI];
            if (y < quadrant.length) {
                var line = quadrant[y];
                if (line != null) {
                    for (x = Math.min(line.length - 1, x); x >= 0; x--) {
                        if (line[x] != null) return false;
                    }
                }
            }
            x = 0;
        }
        var quadrant = quadrants[yI + 1];
        if (y < quadrant.length) {
            var line = quadrant[y];
            if (line != null) {
                x = -x;
                for (; x < line.length; x++) {
                    if (line[x] != null) return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isEmptyRight(int x, int y) {
        int yI = y >= 0 ? 0 : 2;
        y = y < 0 ? ~y : y;
        if (x < -1) {
            var quadrant = quadrants[yI + 1];
            if (y < quadrant.length) {
                var line = quadrant[y];
                if (line != null) {
                    for (x = Math.min(line.length - 1, ~x - 1); x >= 0; x--) {
                        if (line[x] != null) return false;
                    }
                }
            }
            x = -1;
        }
        var quadrant = quadrants[yI];
        if (y < quadrant.length) {
            var line = quadrant[y];
            if (line != null) {
                x++;
                for (; x < line.length; x++) {
                    if (line[x] != null) return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean getUp(int x, int y, Vector2i buffer) {
        buffer.x = x;
        int xI = x >= 0 ? 0 : 1;
        x = x < 0 ? ~x : x;
        if (y > 0) {
            var quadrant = quadrants[xI];
            for (y = Math.min(quadrant.length - 1, y - 1); y >= 0; y--) {
                var line = quadrant[y];
                if (line != null && line.length > x && line[x] != null) {
                    buffer.y = y;
                    return true;
                }
            }
            y = 0;
        }
        var quadrant = quadrants[2 + xI];
        y = -y;
        for (; y < quadrant.length; y++) {
            var line = quadrant[y];
            if (line != null && line.length > x && line[x] != null) {
                buffer.y = ~y;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean getDown(int x, int y, Vector2i buffer) {
        buffer.x = x;
        int xI = x >= 0 ? 0 : 1;
        x = x < 0 ? ~x : x;
        if (y < -1) {
            var quadrant = quadrants[2 + xI];
            for (y = Math.min(quadrant.length - 1, ~y - 1); y >= 0; y--) {
                var line = quadrant[y];
                if (line != null && line.length > x && line[x] != null) {
                    buffer.y = ~y;
                    return true;
                }
            }
            y = -1;
        }
        var quadrant = quadrants[xI];
        y++;
        for (; y < quadrant.length; y++) {
            var line = quadrant[y];
            if (line != null && line.length > x && line[x] != null) {
                buffer.y = y;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean getLeft(int x, int y, Vector2i buffer) {
        buffer.y = y;
        int yI = y >= 0 ? 0 : 2;
        y = y < 0 ? ~y : y;
        if (x > 0) {
            var quadrant = quadrants[yI];
            if (y < quadrant.length) {
                var line = quadrant[y];
                if (line != null) {
                    for (x = Math.min(line.length - 1, x - 1); x >= 0; x--) {
                        if (line[x] != null) {
                            buffer.x = x;
                            return true;
                        }
                    }
                }
            }
            x = 0;
        }
        var quadrant = quadrants[yI + 1];
        if (y < quadrant.length) {
            var line = quadrant[y];
            if (line != null) {
                x = -x;
                for (; x < line.length; x++) {
                    if (line[x] != null) {
                        buffer.x = ~x;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean getRight(int x, int y, Vector2i buffer) {
        buffer.y = y;
        int yI = y >= 0 ? 0 : 2;
        y = y < 0 ? ~y : y;
        if (x < -1) {
            var quadrant = quadrants[yI + 1];
            if (y < quadrant.length) {
                var line = quadrant[y];
                if (line != null) {
                    for (x = Math.min(line.length - 1, ~x - 1); x >= 0; x--) {
                        if (line[x] != null) {
                            buffer.x = ~x;
                            return true;
                        }
                    }
                }
            }
            x = -1;
        }
        var quadrant = quadrants[yI];
        if (y < quadrant.length) {
            var line = quadrant[y];
            if (line != null) {
                x++;
                for (; x < line.length; x++) {
                    if (line[x] != null) {
                        buffer.x = x;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private final int size = contents.size();
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public T next() {
                return contents.get(i++);
            }
        };
    }
}
