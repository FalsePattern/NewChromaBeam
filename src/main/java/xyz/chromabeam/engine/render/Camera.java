package xyz.chromabeam.engine.render;

import org.joml.*;

/**
 * A simple 2D camera with a position and zoom level. Used by the renderer.
 */
public class Camera {
    public final Vector2f pos = new Vector2f(0, 0);
    private final Vector2f aspect = new Vector2f(0, 0);
    private final Vector2f resolution = new Vector2f(0, 0);
    private final Matrix3x2f projectionMatrix = new Matrix3x2f();
    private final Matrix3x2f unProjectionMatrix = new Matrix3x2f();
    private float zoom = 1;
    private boolean dirty = true;

    public void setViewport(int x, int y, int w, int h) {
        resolution.x = w;
        resolution.y = h;
        aspect.x = 1f / w;
        aspect.y = -1f / h;
        viewport[0] = x;
        viewport[1] = y;
        viewport[2] = w;
        viewport[3] = h;
        dirty = true;
    }

    public void setPosition(Vector2fc position) {
        pos.set(position);
    }

    public Vector2fc getPosition() {
        return pos;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getZoom() {
        return zoom;
    }

    private final Vector2f calcBuffer = new Vector2f();
    public Matrix3x2f getProjectionMatrix() {
        if (dirty) {
            dirty = false;
            return projectionMatrix.identity().scale(zoom * 2).scale(aspect).translate(pos.negate(calcBuffer));
        } else {
            return projectionMatrix;
        }
    }

    public Vector2f worldPosToScreen(Vector2fc position, Vector2f destination) {
        return getProjectionMatrix().transformPosition(position, destination);
    }

    public Vector2f worldDirToScreen(Vector2fc direction, Vector2f destination) {
        return getProjectionMatrix().transformDirection(direction, destination);
    }

    private final int[] viewport = new int[4];
    public Vector2f screenToWorldSpace(Vector2fc position, Vector2f destination) {
        return getProjectionMatrix().unproject(position.x(), position.y(), viewport, destination).mul(1, -1);
    }
}
