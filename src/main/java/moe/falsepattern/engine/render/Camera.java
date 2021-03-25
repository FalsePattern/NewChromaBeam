package moe.falsepattern.engine.render;

import org.joml.Vector2f;

public class Camera {
    public Vector2f pos = new Vector2f();
    public Vector2f aspect = new Vector2f();
    public float zoom = 1;
    private float baseZoom = 2;

    public void setFromScreenResolution(float width, float height) {
        aspect.x = 1 / width;
        aspect.y = -1 / height;
    }

    public float getRenderZoom() {
        return baseZoom * zoom;
    }
}
