package moe.falsepattern.chromabeam.world;

import moe.falsepattern.chromabeam.beam.BeamColor;
import moe.falsepattern.chromabeam.beam.Direction;
import moe.falsepattern.chromabeam.component.ComponentI;
import moe.falsepattern.engine.render.chunk.RenderChunk;
import moe.falsepattern.util.Destroyable;
import moe.falsepattern.util.IntMap2D;
import org.joml.Vector2i;
import org.joml.Vector4i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class World2D implements Destroyable, BeamResolver{
    private final IntMap2D<WorldChunk> chunks = new IntMap2D<>();
    private final Supplier<RenderChunk> allocator;
    public World2D(Supplier<RenderChunk> renderChunkAllocator) {
        this.allocator = renderChunkAllocator;
    }

    public ComponentI set(int x, int y, ComponentI component) {
        int cX = Math.floorDiv(x, WorldChunk.CHUNK_SIDE_LENGTH);
        int cY = Math.floorDiv(y, WorldChunk.CHUNK_SIDE_LENGTH);
        var chunk = chunks.getOrCompute(cX, cY, () -> new WorldChunk(cX, cY, allocator == null ? null : allocator.get()));
        return chunk.setComponent(Math.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH), Math.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH), component);
    }

    public ComponentI get(int x, int y) {
        var chunk = chunks.get(Math.floorDiv(x, WorldChunk.CHUNK_SIDE_LENGTH), Math.floorDiv(y, WorldChunk.CHUNK_SIDE_LENGTH));
        return chunk != null ? chunk.getComponent(Math.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH), Math.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH)) : null;
    }

    public void tick() {
        for (var chunk: chunks) {
            chunk.tick(this);
        }
        for (var beam:scheduledBeams) {
            get(beam.x, beam.y).incomingBeam(beam.direction, beam.color);
        }
        scheduleCache.addAll(scheduledBeams);
        scheduledBeams.clear();
    }

    private final List<ScheduledBeam> scheduledBeams = new ArrayList<>();
    private final List<ScheduledBeam> scheduleCache = new ArrayList<>();

    private final Vector2i schedulerBuffer = new Vector2i();
    @Override
    public void scheduleBeam(int x, int y, Direction direction, BeamColor color) {
        if (raycast(x, y, direction, schedulerBuffer)) {
            var beam = scheduleCache.size() == 0 ? new ScheduledBeam() : scheduleCache.remove(scheduleCache.size() - 1);
            beam.x = schedulerBuffer.x;
            beam.y = schedulerBuffer.y;
            beam.direction = direction;
            beam.color = color;
            scheduledBeams.add(beam);
        }
    }

    private static class ScheduledBeam {
        int x;
        int y;
        Direction direction;
        BeamColor color;
    }

    @Override
    public void destroy() {
        for (var chunk: chunks) {
            chunk.destroy();
        }
    }


    private final Vector4i raycastBuffer = new Vector4i();
    /**
     * Launches a virtual beam from the specified origin point along the specified cardinal axis, and puts the coordinates
     * of the first component it hits into the output buffer. Note that the output buffer will be updated even if the
     * raycast doesn't detect a component, so make sure to use the return value as an indicator of successful detection,
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
    public boolean raycast(int x, int y, Direction direction, Vector2i outputBuffer) {
        var buf = raycastBuffer;
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
            b.z = WorldChunk.CHUNK_SIDE_LENGTH - 1;
        } while (!chunks.isEmptyRight(b.x++, b.y));
        return false;
    }

}
