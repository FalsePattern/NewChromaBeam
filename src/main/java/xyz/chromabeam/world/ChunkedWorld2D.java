package xyz.chromabeam.world;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.BeamConsumer;
import xyz.chromabeam.component.BeamInstantManipulator;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.engine.render.beam.BeamDrawer;
import xyz.chromabeam.engine.beam.Beam;
import xyz.chromabeam.engine.render.chunk.RenderChunk;
import xyz.chromabeam.util.Cache;
import xyz.chromabeam.util.Destroyable;
import xyz.chromabeam.util.FastMath;
import xyz.chromabeam.util.storage.Container2D;
import org.joml.Vector2i;
import org.joml.Vector4i;
import xyz.chromabeam.util.storage.NativeContainer2D;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ChunkedWorld2D implements Destroyable, BeamResolver, World2D {
    private final Container2D<WorldChunk> chunks = new NativeContainer2D<>(WorldChunk[]::new, WorldChunk[][]::new, WorldChunk[][][]::new);
    private final Container2D<Vector2i[]> neighbors = new NativeContainer2D<>(Vector2i[][]::new, Vector2i[][][]::new, Vector2i[][][][]::new);
    private final List<WorldChunk> emptyChunkCache = new ArrayList<>();
    private final Supplier<RenderChunk> allocator;
    private final BeamDrawer beamRenderer;
    public ChunkedWorld2D(Supplier<RenderChunk> renderChunkAllocator, BeamDrawer beamRenderer) {
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
        var oldComponent = chunk.setComponent(FastMath.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH), FastMath.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH), direction, flipped, component);
        if (oldComponent == null) {
            updateNeighbors(x, y, true);
        }
        return oldComponent;
    }
    private final Vector2i chunkBuffer = new Vector2i();
    public Component get(int x, int y) {
        var chunk = getChunk(x, y);
        if (chunk != null) {
            toChunkPos(x, y, chunkBuffer);
            return chunk.getComponent(chunkBuffer.x, chunkBuffer.y);
        } else return null;
    }

    public ComponentTransform<Component> getTransform(int x, int y, ComponentTransform<Component> buffer) {
        var chunk = getChunk(x, y);
        if (chunk != null) {
            toChunkPos(x, y, chunkBuffer);
            return chunk.getTransform(chunkBuffer.x, chunkBuffer.y, buffer);
        } else return null;
    }

    public Component remove(int x, int y) {
        int cX = Math.floorDiv(x, WorldChunk.CHUNK_SIDE_LENGTH);
        int cY = Math.floorDiv(y, WorldChunk.CHUNK_SIDE_LENGTH);
        var chunk = chunks.get(cX, cY);
        var comp = chunk != null ? chunk.removeComponent(FastMath.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH), FastMath.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH)) : null;
        if (chunk != null && chunk.isEmpty()) {
            chunks.remove(cX, cY);
            emptyChunkCache.add(chunk);
            chunk.deactivate();
        }
        if (comp != null) {
            updateNeighbors(x, y, false);
        }
        return comp;
    }

    @Override
    public void forceTick(int x, int y) {
    }

    private final ComponentTransform<Component> tickBuffer = new ComponentTransform<>();
    public void update() {
        if (beamRenderer != null) {
            beamRenderer.clear();
        }

        for (var chunk: chunks) {
            chunk.tick(this);
            chunk.updateGraphics();
        }

        while (!scheduledBeams.empty()) {
            processBeam();
        }
    }

    private void processBeam() {
        var beam = scheduledBeams.get();
        var transform = getTransform(beam.x, beam.y, tickBuffer);
        if (transform.component.isConsumer()) {
            ((BeamConsumer)transform.component).incomingBeam(beam.direction.sub(transform.direction).applyFlip(transform.flipped), beam.red, beam.green, beam.blue);
        }
        if (transform.component.isInstantManipulator()) {
            ((BeamInstantManipulator)transform.component)
                    .incomingBeam(beam.direction.sub(transform.direction).applyFlip(transform.flipped),
                            beam.red, beam.green, beam.blue,
                            ((direction, red, green, blue) ->
                                    scheduleBeam(beam.x, beam.y, direction.applyFlip(transform.flipped).add(transform.direction),
                                            red, green, blue)));
        }
        scheduleCache.put(beam);
    }

    private WorldChunk getChunk(int x, int y) {
        return chunks.get(java.lang.Math.floorDiv(x, WorldChunk.CHUNK_SIDE_LENGTH), java.lang.Math.floorDiv(y, WorldChunk.CHUNK_SIDE_LENGTH));
    }

    private static void toChunkPos(int x, int y, Vector2i buffer) {
        buffer.set(FastMath.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH), FastMath.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH));
    }


    private final Cache<Beam> scheduledBeams = new Cache<>(Beam::new, Beam[]::new);
    private final Cache<Beam> scheduleCache = new Cache<>(Beam::new, Beam[]::new);

    private final Vector2i schedulerBuffer = new Vector2i();
    private final Beam drawBeam = new Beam();
    @Override
    public void scheduleBeam(int x, int y, Direction direction, float red, float green, float blue) {

        drawBeam.x = schedulerBuffer.x;
        drawBeam.y = schedulerBuffer.y;
        drawBeam.sourceX = x;
        drawBeam.sourceY = y;
        drawBeam.direction = direction;
        drawBeam.red = red;
        drawBeam.green = green;
        drawBeam.blue = blue;
        if (getNeighbor(x, y, direction, schedulerBuffer)) {
            var c = get(schedulerBuffer.x, schedulerBuffer.y);
            if (c.isConsumer() || c.isInstantManipulator()) {
                var beam = scheduleCache.getOrCreate();
                drawBeam.copyTo(beam);
                scheduledBeams.put(beam);
            }
            renderBeam(drawBeam);
        } else {
            switch (direction) {
                case LEFT -> drawBeam.x = Integer.MIN_VALUE;
                case RIGHT -> drawBeam.x = Integer.MAX_VALUE;
                case UP -> drawBeam.y = Integer.MIN_VALUE;
                case DOWN -> drawBeam.y = Integer.MAX_VALUE;
            }
            renderBeam(drawBeam);
        }
    }

    private void renderBeam(Beam beam) {
        if (beamRenderer != null) {
            beamRenderer.drawBeam(beam);
        }
    }

    @Override
    public void destroy() {
        for (var chunk: chunks) {
            chunk.destroy();
        }
    }

    private boolean getNeighbor(int x, int y, Direction direction, Vector2i outputBuffer) {
        //if (true) return rayTrace(x, y, direction, outputBuffer);
        var cell = neighbors.get(x, y);
        var dir = cell[direction.ordinal()];
        if (dir != null) {
            outputBuffer.set(dir.x, dir.y);
            return true;
        } else {
            return false;
        }
    }

    private final Vector2i neighborBuf = new Vector2i();
    private void updateNeighbors(int x, int y, boolean addComponent) {
        var cell = neighbors.get(x, y);
        if (cell == null || cell.length == 0) {
            cell = new Vector2i[5];
            neighbors.set(x, y, cell);
        }
        if (addComponent) {
            for (int i = 0; i < 4; i++) cell[i] = null;
            Vector2i self = cell[4];
            if (self == null) {
                self = new Vector2i(x, y);
                cell[4] = self;
            }
            if (rayTrace(x, y, Direction.RIGHT, neighborBuf)) {
                var rightNeighbor = neighbors.get(neighborBuf.x, neighborBuf.y);
                cell[0] = rightNeighbor[4];
                var left = rightNeighbor[2];
                if (left != null) {
                    var leftNeighbor = neighbors.get(left.x, left.y);
                    leftNeighbor[0] = self;
                }
                cell[2] = left;
                rightNeighbor[2] = self;
            } else if (rayTrace(x, y, Direction.LEFT, neighborBuf)) {
                var leftNeighbor = neighbors.get(neighborBuf.x, neighborBuf.y);
                cell[2] = leftNeighbor[4];
                leftNeighbor[0] = self;
            }
            if (rayTrace(x, y, Direction.DOWN, neighborBuf)) {
                var downNeighbor = neighbors.get(neighborBuf.x, neighborBuf.y);
                cell[1] = downNeighbor[4];
                var up = downNeighbor[3];
                if (up != null) {
                    var upNeighbor = neighbors.get(up.x, up.y);
                    upNeighbor[1] = self;
                }
                cell[3] = up;
                downNeighbor[3] = self;
            } else if (rayTrace(x, y, Direction.UP, neighborBuf)) {
                var upNeighbor = neighbors.get(neighborBuf.x, neighborBuf.y);
                cell[3] = upNeighbor[4];
                upNeighbor[1] = self;
            }
        } else {
            var right = cell[0];
            var down = cell[1];
            var left = cell[2];
            var up = cell[3];
            if (right != null) {
                if (left != null) {
                    neighbors.get(right.x, right.y)[2] = left;
                    neighbors.get(left.x, left.y)[0] = right;
                } else {
                    neighbors.get(right.x, right.y)[2] = null;
                }
            } else {
                if (left != null) {
                    neighbors.get(left.x, left.y)[0] = null;
                }
            }
            if (down != null) {
                if (up != null) {
                    neighbors.get(down.x, down.y)[3] = up;
                    neighbors.get(up.x, up.y)[1] = down;
                } else {
                    neighbors.get(down.x, down.y)[3] = null;
                }
            } else {
                if (up != null) {
                    neighbors.get(up.x, up.y)[1] = null;
                }
            }
            for (int i = 0; i < 4; i++) cell[i] = null;
        }
    }

    //Raytracing code

    private final Vector4i raytraceBuffer = new Vector4i();

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
    private boolean rayTrace(int x, int y, Direction direction, Vector2i outputBuffer) {
        var buf = raytraceBuffer;
        x = direction == Direction.LEFT ? x - 1 : direction == Direction.RIGHT ? x + 1 : x;
        y = direction == Direction.UP ? y - 1 : direction == Direction.DOWN ? y + 1 : y;
        buf.x = Math.floorDiv(x, WorldChunk.CHUNK_SIDE_LENGTH);
        buf.y = Math.floorDiv(y, WorldChunk.CHUNK_SIDE_LENGTH);

        buf.z = FastMath.floorMod(x, WorldChunk.CHUNK_SIDE_LENGTH);
        buf.w = FastMath.floorMod(y, WorldChunk.CHUNK_SIDE_LENGTH);
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
        } while (!chunks.isEmptyUp(b.x, b.y--));
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
        } while (!chunks.isEmptyDown(b.x, b.y++));
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
