package xyz.chromabeam.world;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.*;
import xyz.chromabeam.engine.render.chunk.RenderChunk;
import xyz.chromabeam.util.Cache;
import xyz.chromabeam.util.Destroyable;
import org.joml.Math;
import org.joml.Vector2i;

import java.util.*;

/**
 * The basic storage block of the ChromaBeam world. Chunks improve component access speeds by reducing the amount of
 * List accesses. Update order of a chunk is randomized, as components are updated independently anyways.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class WorldChunk implements Destroyable, WorldTickable {
    public static final int CHUNK_SIDE_LENGTH = 128;
    public static final int COMPONENTS_PER_CHUNK = CHUNK_SIDE_LENGTH * CHUNK_SIDE_LENGTH;



    private int baseX;
    private int baseY;
    private final RenderChunk renderChunk;
    private final Component[] components = new Component[COMPONENTS_PER_CHUNK];
    private final Direction[] directions = new Direction[COMPONENTS_PER_CHUNK];
    private final boolean[] flips = new boolean[COMPONENTS_PER_CHUNK];

    private final List<ComponentTransform<Tickable>> tickables = new ArrayList<>();
    private final List<ComponentTransform<BeamProducer>> producers = new ArrayList<>();
    private final List<ComponentTransform<?>> graphicsUpdateQueue = new ArrayList<>();

    //private final Map<ComponentI, Vector2i> reverseComponentMap = new HashMap<>();

    //Cache objects to reduce GC pressure
    //private final Cache<Vector2i> vectorCache = new Cache<>(Vector2i::new);
    private final Cache<ComponentTransform<ComponentI>> compCache = new Cache<>(ComponentTransform::new, ComponentTransform[]::new);

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
        for (var t: tickables) {
            t.component.tick();
            if (t.component.isGraphicsChanged()) {
                t.component.updateGraphics();
                graphicsUpdateQueue.add(t);
            }
        }
        for (var e: producers) {
            e.component.emitBeams((beamDir, red, green, blue) -> resolver.scheduleBeam(e.position.x + baseX, e.position.y + baseY, beamDir.applyFlip(e.flipped).add(e.direction), red, green, blue));
        }
    }

    public void updateGraphics() {
        for (var t:graphicsUpdateQueue) {
            updateGraphics(t.position.x, t.position.y, t.direction, t.flipped, t.component);
        }
        graphicsUpdateQueue.clear();
    }

    /**
     * Replaces the component at the specified location with the specified component.
     * @param x The x position inside the chunk
     * @param y The y position inside the chunk
     * @param component The new component to put there
     * @return The previous component at that location, if any, otherwise null.
     */
    public Component setComponent(int x, int y, Direction direction, boolean flipped, Component component) {
        if (component == null) return removeComponent(x, y);
        final int i = y * CHUNK_SIDE_LENGTH + x;
        var old = components[i];
        components[i] = component;
        directions[i] = direction;
        flips[i] = flipped;
        if (old != null) {
            removeComp(old);
        } else {
            size++;
        }
        if (component.isTickable() || component.isProducer()) {
            var transform = compCache.getOrCreate().with(component, x, y, direction, flipped);
            if (component.isTickable()) {
                //noinspection unchecked
                tickables.add((ComponentTransform)transform);
            }
            if (component.isProducer()) {
                //noinspection unchecked
                producers.add((ComponentTransform)transform);
            }
        }

        updateGraphics(x, y, direction, flipped, component);
        return old;
    }

    /**
     * Retrieves the component at the specified position.
     * @param x The X position inside the chunk
     * @param y The Y position inside the chunk
     * @return The component at the position, or null if empty.
     */
    public Component getComponent(int x, int y) {
        return components[y * CHUNK_SIDE_LENGTH + x];
    }

    public Direction getRotation(int x, int y) {
        return directions[y * CHUNK_SIDE_LENGTH + x];
    }

    public boolean getFlipped(int x, int y) {
        return flips[y * CHUNK_SIDE_LENGTH + x];
    }

    public ComponentTransform<Component> getTransform(int x, int y, ComponentTransform<Component> buffer) {
        int i = y * CHUNK_SIDE_LENGTH + x;
        return buffer.with(components[i], x, y, directions[i], flips[i]);
    }

    /**
     * Gets and removes a component at the specified position without the overhead of trying to add a new component.
     * @param x The X position inside the chunk
     * @param y The Y position inside the chunk
     * @return The component that was removed, or null if the position was empty.
     */
    public Component removeComponent(int x, int y) {
        int i = y * CHUNK_SIDE_LENGTH + x;
        var comp = components[i];
        if (comp == null) return null;
        components[i] = null;
        removeComp(comp);
        clearGraphics(x, y);
        size--;
        return comp;
    }

    private int size;
    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
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

    private void removeComp(Component component) {
        if (component.isProducer() || component.isTickable()) {
            ComponentTransform<?> transform = null;
            if (component.isTickable()) {
                var opt = tickables.stream().filter((p) -> p.component == component).findFirst();
                if (opt.isPresent()) {
                    tickables.remove(opt.get());
                    transform = opt.get();
                } else {
                    throw new IllegalStateException("World Chunk cache corruption Type A detected!");
                }
            }
            if (component.isProducer()) {
                var opt = producers.stream().filter((p) -> p.component == component).findFirst();
                if (opt.isPresent()) {
                    producers.remove(opt.get());
                    if (transform != null) {
                        if (transform != opt.get())
                            throw new IllegalStateException("World Chunk cache corruption Type C detected!");
                    } else {
                        transform = opt.get();
                    }
                } else {
                    throw new IllegalStateException("World Chunk cache corruption Type B detected!");
                }
            }
            //noinspection unchecked
            compCache.put((ComponentTransform<ComponentI>) transform.with(null, 0, 0, null, false));
        }
    }

    private void clearGraphics(int x, int y) {
        if (renderChunk == null)return;
        renderChunk.unset(x, y);
    }

    private void updateGraphics(int x, int y, Direction rotation, boolean flipped, ComponentI component) {
        if (renderChunk == null) return;
        if (component == null) {
            renderChunk.unset(x, y);
        } else {
            renderChunk.set(x, y, rotation, flipped, component.getTexture());
        }
    }
}
