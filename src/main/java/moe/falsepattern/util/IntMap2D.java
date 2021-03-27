package moe.falsepattern.util;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.function.Supplier;

public class IntMap2D<T> implements Iterable<T>{
    private final TreeMap<Integer, TreeMap<Integer, T>> map = new TreeMap<>();
    private int size = 0;
    private int minX = 0;
    private int maxX = 0;
    private int minY = 0;
    private int maxY = 0;

    public T get(Vector2i pos) {
        return get(pos.x(), pos.y());
    }

    public T get(int x, int y) {
        var row = map[y];
        if (row != null) {
            return row[x];
        }
        return null;
    }

    public T getOrDefault(int x, int y, T defaultValue) {
        var ret = get(x, y);
        return ret == null ? defaultValue : ret;
    }

    public T getOrCompute(int x, int y, Supplier<T> supplier) {
        var ret = get(x, y);
        if (ret == null) {
            ret = supplier.get();
            set(x, y, ret);
        }
        return ret;
    }

    public boolean isEmptyAbove(int x, int y) {
        return map.entrySet().stream().filter(entry -> entry.getKey() < y).anyMatch(entry -> entry.getValue().containsKey(x));
    }

    public boolean isEmptyBelow(int x, int y) {
        return map.entrySet().stream().filter(entry -> entry.getKey() > y).anyMatch(entry -> entry.getValue().containsKey(x));
    }

    public boolean isEmptyLeft(int x, int y) {
        var row = map[y];
        return row == null || row.keySet().stream().noneMatch((key) -> key < x);
    }

    public boolean isEmptyRight(int x, int y) {
        var row = map[y];
        return row == null || row.keySet().stream().noneMatch((key) -> key > x);
    }



    public void set(Vector2ic pos, T value) {
        set(pos.x(), pos.y(), value);
    }

    public void set(int x, int y, T value) {
        if (value == null) delete(x, y);
        var row = map.computeIfAbsent(y, (ignored) -> new TreeMap<>());
        if (row.put(x, value) == null) {
            size++;
            minX = Math.min(x, minX);
            maxX = Math.max(x, maxX);
            minY = Math.min(y, minY);
            maxY = Math.max(y, maxY);
        }
    }

    public void delete(Vector2ic pos) {
        delete(pos.x(), pos.y());
    }

    public void delete(int x, int y) {
        var row = map[y];
        if (row != null) {
            if (row.remove(x) != null) {
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
        }
    }

    public void clear() {
        map.clear();
    }

    @Override
    public Iterator<T> iterator() {
        var result = new ArrayList<T>(size);
        for (TreeMap<Integer, T> row : map.values()) {
            result.addAll(row.values());
        }
        return result.iterator();
    }
}
