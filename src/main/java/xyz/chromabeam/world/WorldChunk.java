package xyz.chromabeam.world;

import xyz.chromabeam.component.ComponentI;
import xyz.chromabeam.engine.render.chunk.RenderChunk;
import xyz.chromabeam.util.Destroyable;
import org.joml.Math;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * The basic storage block of the ChromaBeam world. Chunks improve component access speeds by reducing the amount of
 * List accesses. Update order of a chunk is randomized, as components are updated independently anyways.
 */
public class WorldChunk implements Destroyable, Tickable {
    public static final int CHUNK_SIDE_LENGTH = 128;
    public static final int COMPONENTS_PER_CHUNK = CHUNK_SIDE_LENGTH * CHUNK_SIDE_LENGTH;

    private int baseX;
    private int baseY;
    private final RenderChunk renderChunk;
    private final ComponentI[] components = new ComponentI[COMPONENTS_PER_CHUNK];
    private final List<ComponentI> updateQueue = new ArrayList<>();

    private final Map<ComponentI, Vector2i> reverseComponentMap = new HashMap<>();

    //Cache objects to reduce GC pressure
    private final Stack<Vector2i> vectorCache = new Stack<>();

    private int size = 0;

    public WorldChunk(int cX, int cY, RenderChunk assignedRenderChunk) {
        this.renderChunk = assignedRenderChunk;
        setPos(cX, cY);
    }

    public void setPos(int cX, int cY) {
        this.baseX = cX * CHUNK_SIDE_LENGTH;
        this.baseY = cY * CHUNK_SIDE_LENGTH;
        if (this.renderChunk != null) {
            this.renderChunk.x = cX;
            this.renderChunk.y = cY;
        }
    }

    public void tick(BeamResolver resolver) {
        for (var component: updateQueue) {
            component.tick((dir, red, green, blue) -> {
                if (red <= 0 && green <= 0 && blue <= 0) return;
                var pos = reverseComponentMap.get(component);
                resolver.scheduleBeam(pos.x + baseX, pos.y + baseY, dir, Math.max(0, red), Math.max(0, green), Math.max(0, blue));
            });
            if (component.graphicsChanged()) {
                var vec = reverseComponentMap.get(component);
                updateGraphics(vec.x, vec.y, component);
            }
        }
    }

    /**
     * Replaces the component at the specified location with the specified component.
     * @param x The x position inside the chunk
     * @param y The y position inside the chunk
     * @param component The new component to put there
     * @return The previous component at that location, if any, otherwise null.
     */
    public ComponentI setComponent(int x, int y, ComponentI component) {
        if (component == null) return removeComponent(x, y);
        var old = components[y * CHUNK_SIDE_LENGTH + x];
        components[y * CHUNK_SIDE_LENGTH + x] = component;
        if (old != null) {
            reverseComponentMap.put(component, reverseComponentMap.remove(old));
            updateQueue.set(updateQueue.indexOf(old), component);
        } else {
            reverseComponentMap.put(component, vectorCache.empty() ? new Vector2i(x, y) : vectorCache.pop());
            updateQueue.add(component);
            size++;
        }
        updateGraphics(x, y, component);
        return old;
    }

    /**
     * Retrieves the component at the specified position.
     * @param x The X position inside the chunk
     * @param y The Y position inside the chunk
     * @return The component at the position, or null if empty.
     */
    public ComponentI getComponent(int x, int y) {
        return components[y * CHUNK_SIDE_LENGTH + x];
    }

    /**
     * Gets and removes a component at the specified position without the overhead of trying to add a new component.
     * @param x The X position inside the chunk
     * @param y The Y position inside the chunk
     * @return The component that was removed, or null if the position was empty.
     */
    public ComponentI removeComponent(int x, int y) {
        var comp = components[y * CHUNK_SIDE_LENGTH + x];
        if (comp == null) return null;
        components[y * CHUNK_SIDE_LENGTH + x] = null;
        updateQueue.remove(comp);
        updateGraphics(x, y, null);
        vectorCache.push(reverseComponentMap.remove(comp));
        size--;
        return comp;
    }

    private void updateGraphics(int x, int y, ComponentI component) {
        if (renderChunk == null) return;
        if (component == null) {
            renderChunk.set(x, y, null);
        } else {
            renderChunk.set(x, y, component.getTexture());
        }
    }

    public int size() {
        return size;
    }

    public boolean empty() {
        return size == 0;
    }

    void activate() {
        renderChunk.activate();
    }

    void deactivate() {
        renderChunk.deactivate();
    }

    @Override
    public void destroy() {
        renderChunk.destroy();
    }
}
