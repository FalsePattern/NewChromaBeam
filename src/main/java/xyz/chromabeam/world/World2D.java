package xyz.chromabeam.world;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.BeamConsumer;
import xyz.chromabeam.component.BeamInstantManipulator;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.engine.render.beam.BeamDrawer;
import xyz.chromabeam.engine.render.chunk.RenderChunk;
import xyz.chromabeam.util.Destroyable;
import xyz.chromabeam.util.IntMap2D;
import org.joml.Vector2i;
import org.joml.Vector4i;
import xyz.chromabeam.util.tuples.mutable.Triplet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

public class World2D implements Destroyable, BeamResolver {
    private final IntMap2D<WorldChunk> chunks = new IntMap2D<>();
    private final List<WorldChunk> emptyChunkCache = new ArrayList<>();
    private final Supplier<RenderChunk> allocator;
    private final BeamDrawer beamRenderer;
    public World2D(Supplier<RenderChunk> renderChunkAllocator, BeamDrawer beamRenderer) {
        this.allocator = renderChunkAllocator;
        this.beamRenderer = beamRenderer;
    }

    public Component set(int x, int y, Direction direction, boolean flipped, Component component) {
        if (component == null) return remove(x, y);
        int cX = Math.floorDiv(x, WorldChunk.CHUNK_SIDE_LENGTH);
        int cY = Math.floorDiv(y, WorldChunk.CHUNK_SIDE_LENGTH);
        var chunk = chunks.getOrCompute(cX, cY, () -> {
            if (emptyChunkCache.size() == 0) {
                return new WorldChunk(cX, cY, allocator == null ? null : allocator.get());
            } else {
                var c = emptyChunkCache.remove(emptyChunkCache.size() - 1);
                c.activate();
                c.setPos(cX, cY);
                return c;
            }
        });
        return chunk.setComponent(Math.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH), Math.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH), direction, flipped, component);
    }

    private WorldChunk getChunk(int x, int y) {
        return chunks.get(Math.floorDiv(x, WorldChunk.CHUNK_SIDE_LENGTH), Math.floorDiv(y, WorldChunk.CHUNK_SIDE_LENGTH));
    }

    private static <T> T callChunkFunction(int x, int y, ChunkFunction<T> fun) {
        return fun.call(Math.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH), Math.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH));
    }

    public Component get(int x, int y) {
        var chunk = getChunk(x, y);
        return chunk != null ? callChunkFunction(x, y, chunk::getComponent) : null;
    }

    public Triplet<Component, Direction, Boolean> getWithRotationAndFlip(int x, int y, Triplet<Component, Direction, Boolean> buffer) {
        var chunk = getChunk(x, y);
        return chunk != null ? callChunkFunction(x, y, (x1, y1) -> chunk.getCompRotFlip(x1, y1, buffer)) : null;
    }

    public Direction getRotation(int x, int y) {
        var chunk = getChunk(x, y);
        return chunk != null ? callChunkFunction(x, y, chunk::getRotation) : null;
    }

    public Component remove(int x, int y) {
        int cX = Math.floorDiv(x, WorldChunk.CHUNK_SIDE_LENGTH);
        int cY = Math.floorDiv(y, WorldChunk.CHUNK_SIDE_LENGTH);
        var chunk = chunks.get(cX, cY);
        var comp = chunk != null ? chunk.removeComponent(Math.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH), Math.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH)) : null;
        if (chunk != null && chunk.isEmpty()) {
            chunks.delete(cX, cY);
            emptyChunkCache.add(chunk);
            chunk.deactivate();
        }
        return comp;
    }

    private final Triplet<Component, Direction, Boolean> tickBuffer = new Triplet<>(null, null, null);
    public void update() {
        for (var chunk: chunks) {
            chunk.tick(this);
            chunk.updateGraphics();
        }
        while (!scheduledBeams.isEmpty()) {
            var beam = scheduledBeams.poll();
            var triplet = getWithRotationAndFlip(beam.x, beam.y, tickBuffer);
            if (triplet.a.isConsumer()) {
                ((BeamConsumer)triplet.a).incomingBeam(beam.direction.sub(triplet.b).applyFlip(triplet.c), beam.red, beam.green, beam.blue);
            }
            if (triplet.a.isInstantManipulator()) {
                ((BeamInstantManipulator)triplet.a)
                        .incomingBeam(beam.direction.sub(triplet.b).applyFlip(triplet.c),
                                beam.red, beam.green, beam.blue,
                                ((direction, red, green, blue) ->
                                        scheduleBeam(beam.x, beam.y, direction.applyFlip(triplet.c).add(triplet.b),
                                                Math.max(0, red), Math.max(0, green), Math.max(0, blue))));
            }
            scheduleCache.add(beam);
        }
    }

    private final Deque<ScheduledBeam> scheduledBeams = new ArrayDeque<>();
    private final Deque<ScheduledBeam> scheduleCache = new ArrayDeque<>();

    private final Vector2i schedulerBuffer = new Vector2i();
    @Override
    public void scheduleBeam(int x, int y, Direction direction, float red, float green, float blue) {
        if (rayCast(x, y, direction, schedulerBuffer)) {
            var beam = scheduleCache.isEmpty() ? new ScheduledBeam() : scheduleCache.pop();
            beam.x = schedulerBuffer.x;
            beam.y = schedulerBuffer.y;
            beam.sourceX = x;
            beam.sourceY = y;
            beam.direction = direction;
            beam.red = red;
            beam.green = green;
            beam.blue = blue;
            scheduledBeams.add(beam);
            renderBeam(x, y, beam.x, beam.y, red, green, blue);
        } else {
            renderBeam(x, y, switch (direction) {
                case LEFT -> Integer.MIN_VALUE;
                case RIGHT -> Integer.MAX_VALUE;
                default -> x;
            }, switch (direction) {
                case UP -> Integer.MIN_VALUE;
                case DOWN -> Integer.MAX_VALUE;
                default -> y;
            }, red, green, blue);
        }
    }

    private void renderBeam(int x1, int y1, int x2, int y2, float r, float g, float b) {
        if (beamRenderer != null) {
            beamRenderer.drawBeam(x1, y1, x2, y2, r, g, b);
        }
    }

    private static class ScheduledBeam {
        int x;
        int y;
        int sourceX;
        int sourceY;
        Direction direction;
        float red;
        float green;
        float blue;
    }

    @Override
    public void destroy() {
        for (var chunk: chunks) {
            chunk.destroy();
        }
    }


    private final Vector4i rayCastBuffer = new Vector4i();
    /**
     * Launches a virtual beam from the specified origin point along the specified cardinal axis, and puts the coordinates
     * of the first component it hits into the output buffer. Note that the output buffer will be updated even if the
     * ray doesn't detect a component, so make sure to use the return value as an indicator of successful detection,
     * and not the value of the output buffer.
     *
     * Note: Due to internal caching this method is not thread-safe.
     * @param x The origin point's X coordinate
     * @param y The origin point's Y coordinate
     * @param direction The axis to trace along
     * @param outputBuffer The buffer to put the results into
     * @return True if the output buffer contains a valid component location, and false if no component was hit, and the
     * output buffer contains undefined values.
     */
    public boolean rayCast(int x, int y, Direction direction, Vector2i outputBuffer) {
        var buf = rayCastBuffer;
        x = direction == Direction.LEFT ? x - 1 : direction == Direction.RIGHT ? x + 1 : x;
        y = direction == Direction.UP ? y - 1 : direction == Direction.DOWN ? y + 1 : y;
        buf.x = Math.floorDiv(x, WorldChunk.CHUNK_SIDE_LENGTH);
        buf.y = Math.floorDiv(y, WorldChunk.CHUNK_SIDE_LENGTH);

        buf.z = Math.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH);
        buf.w = Math.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH);
        boolean found = switch (direction) {
            case UP -> raytraceUp(buf);
            case DOWN -> raytraceDown(buf);
            case LEFT -> raytraceLeft(buf);
            case RIGHT -> raytraceRight(buf);
        };
        outputBuffer.x = buf.x * WorldChunk.CHUNK_SIDE_LENGTH + buf.z;
        outputBuffer.y = buf.y * WorldChunk.CHUNK_SIDE_LENGTH + buf.w;
        return found;
    }

    private boolean raytraceUp(Vector4i b) {
        do {
            var chunk = chunks.get(b.x, b.y);
            if (chunk != null)
                for (; b.w >= 0; b.w--)
                    if (chunk.getComponent(b.z, b.w) != null)
                        return true;
            b.w = WorldChunk.CHUNK_SIDE_LENGTH - 1;
        } while (!chunks.isEmptyAbove(b.x, b.y--));
        return false;
    }

    private boolean raytraceDown(Vector4i b) {
        do {
            var chunk = chunks.get(b.x, b.y);
            if (chunk != null)
                for (; b.w < WorldChunk.CHUNK_SIDE_LENGTH; b.w++)
                    if (chunk.getComponent(b.z, b.w) != null)
                        return true;
            b.w = 0;
        } while (!chunks.isEmptyBelow(b.x, b.y++));
        return false;
    }

    private boolean raytraceLeft(Vector4i b) {
        do {
            var chunk = chunks.get(b.x, b.y);
            if (chunk != null)
                for (; b.z >= 0; b.z--)
                    if (chunk.getComponent(b.z, b.w) != null)
                        return true;
            b.z = WorldChunk.CHUNK_SIDE_LENGTH - 1;
        } while (!chunks.isEmptyLeft(b.x--, b.y));
        return false;
    }

    private boolean raytraceRight(Vector4i b) {
        do {
            var chunk = chunks.get(b.x, b.y);
            if (chunk != null)
                for (; b.z < WorldChunk.CHUNK_SIDE_LENGTH; b.z++)
                    if (chunk.getComponent(b.z, b.w) != null)
                        return true;
            b.z = 0;
        } while (!chunks.isEmptyRight(b.x++, b.y));
        return false;
    }

}
