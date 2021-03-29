package moe.falsepattern.engine.render.texture;

/**
 * Canonical implementation of {@link TextureRegionI}.
 */
public class TextureRegion implements TextureRegionI {
    private final int x;
    private final int y;
    private final int w;
    private final int h;
    private final float u0;
    private final float v0;
    private final float u1;
    private final float v1;
    public TextureRegion(Texture texture, int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.u0 = x / (float)texture.width();
        this.v0 = y / (float)texture.height();
        this.u1 = (x + w) / (float)texture.width();
        this.v1 = (y + h) / (float)texture.height();
    }
    @Override
    public float u0() {
        return u0;
    }

    @Override
    public float v0() {
        return v0;
    }

    @Override
    public float u1() {
        return u1;
    }

    @Override
    public float v1() {
        return v1;
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int width() {
        return w;
    }

    @Override
    public int height() {
        return h;
    }

}
