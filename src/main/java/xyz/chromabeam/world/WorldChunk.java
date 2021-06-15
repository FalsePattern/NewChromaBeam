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
public class WorldChunk implements Destroyable, WorldTickable {
    public static final int CHUNK_SIDE_LENGTH = 128;
    public static final int COMPONENTS_PER_CHUNK = CHUNK_SIDE_LENGTH * CHUNK_SIDE_LENGTH;



    private int baseX;
    private int baseY;
    private final RenderChunk renderChunk;
    private final Component[] components = new Component[COMPONENTS_PER_CHUNK];
    private final Direction[] directions = new Direction[COMPONENTS_PER_CHUNK];
    private final boolean[] flips = new boolean[COMPONENTS_PER_CHUNK];

    private final List<Triplet<Tickable>> tickables = new ArrayList<>();
    private final List<Triplet<BeamProducer>> producers = new ArrayList<>();
    private final List<Triplet<?>> graphicsUpdateQueue = new ArrayList<>();

    private final Map<ComponentI, Vector2i> reverseComponentMap = new HashMap<>();

    //Cache objects to reduce GC pressure
    private final Cache<Vector2i> vectorCache = new Cache<>(Vector2i::new);
    private final Cache<Triplet<ComponentI>> compCache = new Cache<>(Triplet::new);

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
            e.component.emitBeams((beamDir, red, green, blue) -> {
                if (red <= 0 && green <= 0 && blue <= 0) return;
                var pos = reverseComponentMap.get(e.component);
                resolver.scheduleBeam(pos.x + baseX, pos.y + baseY, beamDir.applyFlip(e.flipped).add(e.direction), Math.max(0, red), Math.max(0, green), Math.max(0, blue));
            });
        }
    }

    public void updateGraphics() {
        for (var t:graphicsUpdateQueue) {
            var vec = reverseComponentMap.get(t.component);
            updateGraphics(vec.x, vec.y, getRotation(vec.x, vec.y), getFlipped(vec.x, vec.y), t.component);
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
            reverseComponentMap.put(component, reverseComponentMap.remove(old));
            removeComp(old);
        } else {
            reverseComponentMap.put(component, vectorCache.getOrCreate().set(x, y));
            size++;
        }
        if (component.isTickable() || component.isProducer()) {
            var triplet = compCache.getOrCreate().with(component, direction, flipped);
            if (component.isTickable()) {
                //noinspection unchecked
                tickables.add(triplet);
            }
            if (component.isProducer()) {
                //noinspection unchecked
                producers.add(triplet);
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

    public xyz.chromabeam.util.tuples.mutable.Triplet<Component, Direction, Boolean> getCompRotFlip(int x, int y, xyz.chromabeam.util.tuples.mutable.Triplet<Component, Direction, Boolean> buffer) {
        int i = y * CHUNK_SIDE_LENGTH + x;
        return buffer.with(components[i], directions[i], flips[i]);
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
        vectorCache.put(reverseComponentMap.remove(comp));
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
            Triplet<?> triplet = null;
            if (component.isTickable()) {
                var opt = tickables.stream().filter((p) -> p.component == component).findFirst();
                if (opt.isPresent()) {
                    tickables.remove(opt.get());
                    triplet = opt.get();
                } else {
                    throw new IllegalStateException("World Chunk cache corruption Type A detected!");
                }
            }
            if (component.isProducer()) {
                var opt = producers.stream().filter((p) -> p.component == component).findFirst();
                if (opt.isPresent()) {
                    producers.remove(opt.get());
                    if (triplet != null) {
                        if (triplet != opt.get())
                            throw new IllegalStateException("World Chunk cache corruption Type C detected!");
                    } else {
                        triplet = opt.get();
                    }
                } else {
                    throw new IllegalStateException("World Chunk cache corruption Type B detected!");
                }
            }
            //noinspection unchecked
            compCache.put(triplet.with(null, null, false));
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

    private static final class Triplet<T extends ComponentI> {
        T component;
        Direction direction;
        boolean flipped;

        @SuppressWarnings("rawtypes")
        Triplet with(T component, Direction direction, boolean flipped) {
            this.component = component;
            this.direction = direction;
            this.flipped = flipped;
            return this;
        }
    }
}
