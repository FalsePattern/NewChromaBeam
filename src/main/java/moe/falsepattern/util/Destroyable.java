package moe.falsepattern.util;

public interface Destroyable extends AutoCloseable {
    void destroy() throws Exception;

    default void close() throws Exception {
        destroy();
    }
}
