package xyz.chromabeam.util.storage;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import xyz.chromabeam.util.tuples.mutable.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Supplier;

public class IntMap2D<T> implements Container2D<T> {
    private final Map<Integer, Map<Integer, T>> map = new HashMap<>();
    private int size = 0;
    private int minX = 0;
    private int maxX = 0;
    private int minY = 0;
    private int maxY = 0;

    @Override
    public long getElementCount() {
        return size;
    }

    @Override
    public T get(int x, int y) {
        var row = map.get(y);
        if (row != null) {
            return row.get(x);
        }
        return null;
    }

    @Override
    public T getOrDefault(int x, int y, T defaultValue) {
        var ret = get(x, y);
        return ret == null ? defaultValue : ret;
    }

    @Override
    public T getOrCompute(int x, int y, Supplier<T> supplier) {
        var ret = get(x, y);
        if (ret == null) {
            ret = supplier.get();
            set(x, y, ret);
        }
        return ret;
    }

    @Override
    public boolean isEmptyUp(int x, int y) {
        return map.entrySet().stream().filter(entry -> entry.getKey() < y).noneMatch(entry -> entry.getValue().containsKey(x));
    }

    @Override
    public boolean isEmptyDown(int x, int y) {
        return map.entrySet().stream().filter(entry -> entry.getKey() > y).noneMatch(entry -> entry.getValue().containsKey(x));
    }

    @Override
    public boolean isEmptyLeft(int x, int y) {
        var row = map.get(y);
        return row == null || row.keySet().stream().noneMatch((key) -> key < x);
    }

    @Override
    public boolean isEmptyRight(int x, int y) {
        var row = map.get(y);
        return row == null || row.keySet().stream().noneMatch((key) -> key > x);
    }

    @Override
    public boolean getUp(int x, int y, Vector2i buffer) {
        var optY = map.entrySet().stream().filter(entry -> entry.getKey() < y && entry.getValue().containsKey(x)).mapToInt(Map.Entry::getKey).min();
        if (optY.isPresent()) {
            buffer.set(x, optY.getAsInt());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean getDown(int x, int y, Vector2i buffer) {
        var optY = map.entrySet().stream().filter(entry -> entry.getKey() > y && entry.getValue().containsKey(x)).mapToInt(Map.Entry::getKey).max();
        if (optY.isPresent()) {
            buffer.set(x, optY.getAsInt());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean getLeft(int x, int y, Vector2i buffer) {
        var row = map.get(y);
        if (row == null) return false;
        var optX = row.keySet().stream().mapToInt(Integer::intValue).filter((i) -> i < y).min();
        if (optX.isPresent()) {
            buffer.set(optX.getAsInt(), y);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean getRight(int x, int y, Vector2i buffer) {
        var row = map.get(y);
        if (row == null) return false;
        var optX = row.keySet().stream().mapToInt(Integer::intValue).filter((i) -> i > y).max();
        if (optX.isPresent()) {
            buffer.set(optX.getAsInt(), y);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public T set(int x, int y, T value) {
        if (value == null) remove(x, y);
        var row = map.computeIfAbsent(y, (ignored) -> new HashMap<>());
        var old = row.put(x, value);
        if (old == null) {
            size++;
            minX = Math.min(x, minX);
            maxX = Math.max(x, maxX);
            minY = Math.min(y, minY);
            maxY = Math.max(y, maxY);
        }
        return old;
    }

    @Override
    public T remove(int x, int y) {
        var row = map.get(y);
        if (row != null) {
            var old = row.remove(x);
            if (old != null) {
                size--;
                if (x == maxX) {
                    maxX = map.values()
                            .stream()
                            .mapMultiToInt((line, replacer) ->
                                    line.keySet().forEach(replacer::accept))
                            .max()
                            .orElse(0);
                }
                if (x == minX) {
                    maxX = map.values()
                            .stream()
                            .mapMultiToInt((line, replacer) ->
                                    line.keySet().forEach(replacer::accept))
                            .min()
                            .orElse(0);
                }
            }
            if (row.isEmpty()) {
                map.remove(y);
                if (y == minY) {
                    minY = map.keySet()
                            .stream()
                            .min(Integer::compareTo)
                            .orElse(0);
                }
                if (y == maxY) {
                    maxY = map.keySet().stream().max(Integer::compareTo).orElse(0);
                }
            }
            return old;
        }
        return null;
    }

    @Override
    public boolean isEmpty(int x, int y) {
        return get(x, y) == null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public List<T> getNonNullUnordered() {
        var result = new ArrayList<T>(size);
        for (Map<Integer, T> row : map.values()) {
            for (T t : row.values()) {
                if (t != null) {
                    result.add(t);
                }
            }
        }
        return result;
    }

    @Override
    public Iterator<T> iterator() {
        return getNonNullUnordered().iterator();
    }
}
