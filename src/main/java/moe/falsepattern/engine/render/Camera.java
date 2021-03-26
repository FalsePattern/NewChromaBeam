package moe.falsepattern.engine.render;

import org.joml.Vector2f;

/**
 * A simple 2D camera with a position and zoom level. Used by the renderer.
 */
public class Camera {
    private static final float baseZoom = 2;

    public final Vector2f pos = new Vector2f();
    public final Vector2f aspect = new Vector2f();
    public float zoom = 1;

    /**
     * If the aspect ratio of the window changed, it should be entered here to keep the aspect ratio of the world 1:1.
     * @param width The width of the framebuffer
     * @param height The height of the framebuffer
     */
    public void setFromScreenResolution(float width, float height) {
        aspect.x = 1 / width;
        aspect.y = -1 / height;
    }

    public float getRenderZoom() {
        return baseZoom * zoom;
    }
}
