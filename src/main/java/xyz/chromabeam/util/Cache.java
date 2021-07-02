package xyz.chromabeam.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import java.util.function.Supplier;

public class Cache<T> {
    private static final int INITIAL_CACHE_SIZE = 256;
    private static final float GROWTH_MULTIPLIER = 1.5f;
    private int i = -1;
    private final Supplier<T> creator;
    private final Function<Integer, T[]> arrayCreator;
    private T[] buffer;
    public Cache(Supplier<T> creator, Function<Integer, T[]> arrayCreator) {
        this.creator = creator;
        this.arrayCreator = arrayCreator;
        buffer = arrayCreator.apply(256);
    }
    public T getOrCreate() {
        if (i == -1) return creator.get(); else {
            T result = buffer[i];
            buffer[i--] = null;
            return result;
        }
    }

    public T get() {
        T result = buffer[i];
        buffer[i--] = null;
        return result;
    }

    public void put(T thing) {
        i++;
        if (i >= buffer.length) {
            var old = buffer;
            buffer = arrayCreator.apply((int) (old.length * GROWTH_MULTIPLIER));
            System.arraycopy(old, 0, buffer, 0, old.length);
        }
        buffer[i] = thing;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean empty() {
        return i == -1;
    }

    public int size() {
        return i + 1;
    }
}
