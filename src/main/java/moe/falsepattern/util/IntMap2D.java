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

    public T get(Vector2i pos) {
        return get(pos.x(), pos.y());
    }

    public T get(int x, int y) {
        var row = map.getOrDefault(y, null);
        if (row != null) {
            return row.getOrDefault(x, null);
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

    public void set(Vector2ic pos, T value) {
        set(pos.x(), pos.y(), value);
    }

    public void set(int x, int y, T value) {
        if (value == null) delete(x, y);
        var row = map.computeIfAbsent(y, (ignored) -> new TreeMap<>());
        if (row.put(x, value) == null) {
            size++;
        }
    }

    public void delete(Vector2ic pos) {
        delete(pos.x(), pos.y());
    }

    public void delete(int x, int y) {
        var row = map.getOrDefault(y, null);
        if (row != null) {
            if (row.remove(x) != null) {
                size--;
            }
            if (row.isEmpty()) {
                map.remove(y);
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
