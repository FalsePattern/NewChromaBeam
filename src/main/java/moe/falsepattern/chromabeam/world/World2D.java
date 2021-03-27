package moe.falsepattern.chromabeam.world;

import moe.falsepattern.chromabeam.beam.BeamColor;
import moe.falsepattern.chromabeam.beam.Direction;
import moe.falsepattern.chromabeam.component.ComponentI;
import moe.falsepattern.engine.render.chunk.RenderChunk;
import moe.falsepattern.util.Destroyable;
import moe.falsepattern.util.IntMap2D;
import org.joml.Vector2i;

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

    private final Vector2i raycastBuffer = new Vector2i();
    @Override
    public void scheduleBeam(int x, int y, Direction direction, BeamColor color) {
        raycastBuffer.set(x, y);
        if (raycast(raycastBuffer, direction, raycastBuffer)) {
            var beam = scheduleCache.size() == 0 ? new ScheduledBeam() : scheduleCache.remove(scheduleCache.size() - 1);
            beam.x = raycastBuffer.x;
            beam.y = raycastBuffer.y;
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

    public boolean raycast(Vector2i source, Direction direction, Vector2i outputBuffer) {
        int cX, cY, iX, iY;
        {
            int x = direction == Direction.LEFT ? source.x - 1 : direction == Direction.RIGHT ? source.x + 1 : source.x;
            int y = direction == Direction.UP ? source.y - 1 : direction == Direction.DOWN ? source.y + 1 : source.y;
            cX = Math.floorDiv(x, WorldChunk.CHUNK_SIDE_LENGTH);
            cY = Math.floorDiv(y, WorldChunk.CHUNK_SIDE_LENGTH);

            iX = Math.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH);
            iY = Math.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH);
        }
        boolean found = false;
        outer:
        switch (direction) {
            case UP:
                do {
                    var chunk = chunks.get(cX, cY);
                    if (chunk != null)
                        for (; iY >= 0; iY--)
                            if (chunk.getComponent(iX, iY) != null) {
                                found = true;
                                break outer;
                            }
                    iY = WorldChunk.CHUNK_SIDE_LENGTH - 1;
                } while (!chunks.isEmptyAbove(cX, cY--));
                break;
            case DOWN:
                do {
                    var chunk = chunks.get(cX, cY);
                    if (chunk != null)
                        for (; iY < WorldChunk.CHUNK_SIDE_LENGTH; iY++)
                            if (chunk.getComponent(iX, iY) != null) {
                                found = true;
                                break outer;
                            }
                    iY = 0;
                } while (!chunks.isEmptyBelow(cX, cY++));
                break;

            case LEFT:
                do {
                    var chunk = chunks.get(cX, cY);
                    if (chunk != null)
                        for (; iX >= 0; iX--)
                            if (chunk.getComponent(iX, iY) != null) {
                                found = true;
                                break outer;
                            }
                    iX = WorldChunk.CHUNK_SIDE_LENGTH - 1;
                } while (!chunks.isEmptyLeft(cX--, cY));
                break;
            case RIGHT:
                do {
                    var chunk = chunks.get(cX, cY);
                    if (chunk != null)
                        for (; iX < WorldChunk.CHUNK_SIDE_LENGTH; iX++)
                            if (chunk.getComponent(iX, iY) != null) {
                                found = true;
                                break outer;
                            }
                    iX = WorldChunk.CHUNK_SIDE_LENGTH - 1;
                } while (!chunks.isEmptyRight(cX++, cY));
                break;
        }
        if (found) {
            outputBuffer.x = cX * WorldChunk.CHUNK_SIDE_LENGTH + iX;
            outputBuffer.y = cY * WorldChunk.CHUNK_SIDE_LENGTH + iY;
        }
        return found;
    }

}
