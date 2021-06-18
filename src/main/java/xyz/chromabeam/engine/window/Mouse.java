package xyz.chromabeam.engine.window;

import org.joml.Vector2i;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Mouse {
    public static class Event {
        public enum Type {
            LPress, LRelease,
            RPress, RRelease,
            MPress, MRelease,
            WheelUp, WheelDown,
            WheelLeft, WheelRight,
            Move,
            Enter, Leave;

            private static final Type[] values = Type.values();
            private byte toMask() {
                return (byte) (this.ordinal() << 0x04);
            }

            private static Type fromMask(byte mask) {
                return values[(mask & 0x00f0) >>> 4];
            }
        }

        private final byte mask;
        private final int x;
        private final int y;

        public int x() {
            return x;
        }
        public int y() {
            return y;
        }

        public boolean isLeftPressed() {
            return (mask & MASK_LEFT_BUTTON) == MASK_LEFT_BUTTON;
        }
        public boolean isRightPressed() {
            return (mask & MASK_RIGHT_BUTTON) == MASK_RIGHT_BUTTON;
        }
        public boolean isMiddlePressed() {
            return (mask & MASK_MIDDLE_BUTTON) == MASK_MIDDLE_BUTTON;
        }
        public boolean isInWindow() {
            return (mask & MASK_IN_WINDOW) == MASK_IN_WINDOW;
        }
        public Type type() {
            return Type.fromMask(mask);
        }

        public Vector2i getPos(Vector2i buffer) {
            return buffer.set(x, y);
        }

        private Event(Type type, Mouse parent) {
            mask = (byte) (type.toMask() | parent.mask);
            x = parent.x;
            y = parent.y;
        }
    }

    Mouse(){}

    public static final int MOUSE_WHEEL_DELTA = 120;

    public Vector2i getPos() {
        return new Vector2i(x, y);
    }
    public Vector2i getPos(Vector2i buffer) {
        return buffer.set(x, y);
    }
    public int getPosX() {
        return x;
    }
    public int getPosY() {
        return y;
    }
    public boolean isInWindow() {
        return (mask & MASK_IN_WINDOW) == MASK_IN_WINDOW;
    }
    public boolean leftIsPressed() {
        return (mask & MASK_LEFT_BUTTON) == MASK_LEFT_BUTTON;
    }
    public boolean middleIsPressed() {
        return (mask & MASK_MIDDLE_BUTTON) == MASK_MIDDLE_BUTTON;
    }
    public boolean rightIsPressed() {
        return (mask & MASK_RIGHT_BUTTON) == MASK_RIGHT_BUTTON;
    }
    public Optional<Event> read() {
        return Optional.ofNullable(buffer.poll());
    }
    public boolean isEmpty() {
        return buffer.isEmpty();
    }
    public void flush() {
        buffer.clear();
    }

    void onMouseMove(int x, int y) {
        this.x = x;
        this.y = y;
        postEvent(Event.Type.Move);
    }
    void onMouseLeave() {
        mask &= ~MASK_IN_WINDOW;
        postEvent(Event.Type.Leave);
    }
    void onMouseEnter() {
        mask |= MASK_IN_WINDOW;
        postEvent(Event.Type.Enter);
    }
    void onLeftPressed() {
        mask |= MASK_LEFT_BUTTON;
        postEvent(Event.Type.LPress);
    }
    void onLeftReleased() {
        mask &= ~MASK_LEFT_BUTTON;
        postEvent(Event.Type.LRelease);
    }
    void onRightPressed() {
        mask |= MASK_RIGHT_BUTTON;
        postEvent(Event.Type.RPress);
    }
    void onRightReleased() {
        mask &= ~MASK_RIGHT_BUTTON;
        postEvent(Event.Type.RRelease);
    }
    void onMiddlePressed() {
        mask |= MASK_MIDDLE_BUTTON;
        postEvent(Event.Type.MPress);
    }
    void onMiddleReleased() {
        mask &= ~MASK_MIDDLE_BUTTON;
        postEvent(Event.Type.MRelease);
    }
    void onWheelDelta(double dx, double dy) {
        wheelDeltaCarryX += dx;
        wheelDeltaCarryY += dy;
        while (wheelDeltaCarryX <= -MOUSE_WHEEL_DELTA) {
            wheelDeltaCarryX += MOUSE_WHEEL_DELTA;
            postEvent(Event.Type.WheelLeft);
        }
        while (wheelDeltaCarryX >= MOUSE_WHEEL_DELTA) {
            wheelDeltaCarryX -= MOUSE_WHEEL_DELTA;
            postEvent(Event.Type.WheelRight);
        }
        while (wheelDeltaCarryY <= -MOUSE_WHEEL_DELTA) {
            wheelDeltaCarryY += MOUSE_WHEEL_DELTA;
            postEvent(Event.Type.WheelDown);
        }
        while (wheelDeltaCarryY >= MOUSE_WHEEL_DELTA) {
            wheelDeltaCarryY -= MOUSE_WHEEL_DELTA;
            postEvent(Event.Type.WheelUp);
        }
    }

    boolean anyPressed() {
        return mask != 0;
    }

    private void trimBuffer() {
        while (buffer.size() > bufferSize) {
            buffer.poll();
        }
    }

    private void postEvent(Event.Type type) {
        buffer.add(new Event(type, this));
        trimBuffer();
    }

    private static final byte MASK_LEFT_BUTTON = 0x01;
    private static final byte MASK_RIGHT_BUTTON = 0x02;
    private static final byte MASK_MIDDLE_BUTTON = 0x04;
    private static final byte MASK_IN_WINDOW = 0x08;

    private static final int bufferSize = 64;
    private int x = 0;
    private int y = 0;
    private byte mask = 0x00;
    private final Queue<Event> buffer = new LinkedBlockingQueue<>();
    private double wheelDeltaCarryX = 0;
    private double wheelDeltaCarryY = 0;
}
