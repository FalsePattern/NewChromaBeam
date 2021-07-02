package xyz.chromabeam.ui;

import org.joml.Vector2i;
import xyz.chromabeam.util.Cache;

public class RectI2D {
    public static final RectI2D ZERO = new RectI2D(0, 0, 0, 0);

    public final Vector2i position = new Vector2i(0);
    public final Vector2i size = new Vector2i(0);

    private static final Cache<RectI2D> rectCache = new Cache<>(RectI2D::new, RectI2D[]::new);

    public static RectI2D getBuffer() {
        return rectCache.getOrCreate();
    }

    public static void releaseBuffer(RectI2D buffer) {
        rectCache.put(buffer.with(0, 0, 0, 0));
    }


    public RectI2D(){}
    public RectI2D(int x, int y, int w, int h) {
        position.set(x, y);
        size.set(w, h);
    }

    public RectI2D(RectI2D original) {
        position.set(original.position);
        size.set(original.size);
    }

    public RectI2D position(int x, int y) {
        position.set(x, y);
        return this;
    }

    public RectI2D position(Vector2i position) {
        this.position.set(position);
        return this;
    }

    public RectI2D size(int w, int h) {
        size.set(w, h);
        return this;
    }

    public RectI2D size(Vector2i size) {
        this.size.set(size);
        return this;
    }

    public RectI2D with(int x, int y, int w, int h) {
        position.set(x, y);
        size.set(w, h);
        return this;
    }

    public RectI2D with(RectI2D other) {
        position.set(other.position);
        size.set(other.size);
        return this;
    }

    public RectI2D addPosition(Vector2i position) {
        this.position.add(position);
        return this;
    }

    public RectI2D addSize(Vector2i size) {
        this.size.add(size);
        return this;
    }

    public boolean contains(int x, int y) {
        return x > position.x && x < position.x + size.x && y > position.y && y < position.y + size.y;
    }

}
