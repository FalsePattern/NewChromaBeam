package xyz.chromabeam.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public class Cache<T> {
    private final Supplier<T> creator;
    private final Deque<T> buffer = new ArrayDeque<>();
    public Cache(Supplier<T> creator) {
        this.creator = creator;
    }
    public T getOrCreate() {
        return buffer.isEmpty() ? creator.get() : buffer.pop();
    }

    public void put(T thing) {
        buffer.push(thing);
    }
}
