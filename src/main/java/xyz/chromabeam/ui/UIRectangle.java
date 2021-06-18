package xyz.chromabeam.ui;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UIRectangle {
    private final RectI2D relativeRect;
    private UIRectangle parent = null;
    private final List<UIRectangle> children = new ArrayList<>();
    private final boolean mouseTransparent;

    public UIRectangle(int x, int y, int w, int h, boolean mouseTransparent) {
        this.relativeRect = new RectI2D(x, y, w, h);
        this.mouseTransparent = mouseTransparent;
    }

    public void addChild(UIRectangle child) {
        if (child.parent != null) throw new RuntimeException("Cannot reparent UI object!");
        child.parent = this;
        children.add(child);
    }

    public void removeChild(UIRectangle child) {
        if (child.parent != this) throw new RuntimeException("Cannot remove UI child with different parent!");
        child.parent = null;
        children.remove(child);
    }

    public List<UIRectangle> children() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Gets the relative position of the gui element.
     * @param buffer The buffer to fill with the hitbox information
     * @return The passed-in buffer for chaining
     */
    public Vector2i getRelativePosition(Vector2i buffer) {
        return buffer.set(relativeRect.position);
    }

    /**
     * Sets the relative position of the gui element.
     * @param position The position to set the gui element to
     */
    public void setRelativePosition(Vector2i position) {
        this.relativeRect.position.set(position);
    }

    /**
     * Sets the relative position of the gui element.
     * @param x The x position to set the gui element to
     * @param y The y position to set the gui element to
     */
    public void setRelativePosition(int x, int y) {
        relativeRect.position.set(x, y);
    }

    /**
     * Gets the screen-space position of the gui element. (relative position + parent absolute position)
     * @param buffer The buffer to fill with the hitbox information
     * @return The passed-in buffer for chaining
     */
    public Vector2i getAbsolutePosition(Vector2i buffer) {
        return parent == null ? getRelativePosition(buffer) : parent.getAbsolutePosition(buffer).add(relativeRect.position);
    }

    /**
     * Gets the size of the gui element.
     * @param buffer The buffer to fill with the hitbox information
     * @return The passed-in buffer for chaining
     */
    public Vector2i getSize(Vector2i buffer) {
        return buffer.set(relativeRect.size);
    }

    /**
     * Sets the size of the gui element.
     * @param size The size to set the gui element to
     */
    public void setSize(Vector2i size) {
        relativeRect.size.set(size);
    }

    /**
     * Sets the size of the gui element.
     * @param w The width to set the gui element to
     * @param h The height to set the gui element to
     */
    public void setSize(int w, int h) {
        relativeRect.size.set(w, h);
    }

    /**
     * Gets the screen-space rectangle of the gui element. (relative rectangle + parent absolute position)
     * @param buffer The buffer to fill with the hitbox information
     * @return The passed-in buffer for chaining
     */
    public RectI2D getAbsoluteRect(RectI2D buffer) {
        getAbsolutePosition(buffer.position);
        getSize(buffer.size);
        return buffer;
    }

    /**
     * Gets the relative rectangle of the gui element.
     * @param buffer The buffer to fill with the hitbox information
     * @return The passed-in buffer for chaining
     */
    public RectI2D getRelativeRect(RectI2D buffer) {
        getRelativePosition(buffer.position);
        getSize(buffer.size);
        return buffer;
    }

    /**
     * Sets the relative rectangle of the gui element.
     * @param value The value to set the dimensions of the gui element to.
     */
    public void setRelativeRect(RectI2D value) {
        relativeRect.with(value);
    }

    private boolean mouseOver = false;
    private boolean mousePressed = false;

    /**
     * Called when the mouse hovers over the ui component. The default implementation does nothing.
     * @param x The relative x position of the mouse compared to the hitBox of the component.
     * @param y The relative y position of the mouse compared to the hitBox of the component.
     */
    public void mouseEnter(int x, int y) {}

    /**
     * Called when the mouse stops hovering over the ui component. The default implementation does nothing.
     * @param x The relative x position of the mouse compared to the hitBox of the component.
     * @param y The relative y position of the mouse compared to the hitBox of the component.
     */
    public void mouseLeave(int x, int y) {}

    /**
     * Called when the mouse changes position over the ui component. The default implementation does nothing.
     * @param x The relative x position of the mouse compared to the hitBox of the component.
     * @param y The relative y position of the mouse compared to the hitBox of the component.
     */
    public void mousePos(int x, int y) {}

    /**
     * Called when the mouse starts clicking the ui component. The default implementation does nothing.
     * @param x The relative x position of the mouse compared to the hitBox of the component.
     * @param y The relative y position of the mouse compared to the hitBox of the component.
     */
    public void mousePress(int x, int y) {}

    /**
     * Called when the mouse finishes clicking the ui component. The default implementation does nothing.
     * @param x The relative x position of the mouse compared to the hitBox of the component.
     * @param y The relative y position of the mouse compared to the hitBox of the component.
     */
    public void mouseRelease(int x, int y){}

    public final boolean processMouse(int x, int y, boolean press) {
        final var contains = relativeRect.contains(x, y);
        final var rX = x - relativeRect.position.x;
        final var rY = y - relativeRect.position.y;
        if (contains) {
            mousePos(rX, rY);
            if (!mouseOver) {
                mouseOver = true;
                mouseEnter(rX, rY);
            }
            if (!mousePressed && press) {
                mousePressed = true;
                mousePress(rX, rY);
            } else if (mousePressed && !press) {
                mousePressed = false;
                mouseRelease(rX, rY);
            }
            boolean val = false;
            for (var child: children) {
                val |= child.processMouse(rX, rY, press);
            }
            return !mouseTransparent || val;
        } else if (mouseOver) {
            children.forEach((child) -> child.processMouse(rX, rY, press));
            if (mousePressed) {
                mouseRelease(rX, rY);
                mousePressed = false;
            }
            mouseLeave(rX, rY);
            mouseOver = false;
        }
        return false;
    }

    /**
     * Called when drawing the UI. The default implementation calls the drawing method of all children. Always call the
     * parent method as the last one to maintain overlap order!
     * @param drawer The UI drawing library. You have to use this to draw to the screen.
     */
    public void draw(UIDrawer drawer) {
        children.forEach((child) -> child.draw(drawer));
    }
}
