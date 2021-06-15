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
            Enter, Leave
        }
        public final Type type;
        public final boolean leftIsPressed;
        public final boolean middleIsPressed;
        public final boolean rightIsPressed;
        public final int x;
        public final int y;

        public Vector2i getPos() {
            return new Vector2i(x, y);
        }

        private Event(Type type, Mouse parent) {
            this.type = type;
            leftIsPressed = parent.leftIsPressed;
            middleIsPressed = parent.middleIsPressed;
            rightIsPressed = parent.rightIsPressed;
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
        return isInWindow;
    }
    public boolean leftIsPressed() {
        return leftIsPressed;
    }
    public boolean middleIsPressed() {
        return middleIsPressed;
    }
    public boolean rightIsPressed() {
        return rightIsPressed;
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
        isInWindow = false;
        postEvent(Event.Type.Leave);
    }
    void onMouseEnter() {
        isInWindow = true;
        postEvent(Event.Type.Enter);
    }
    void onLeftPressed() {
        leftIsPressed = true;
        postEvent(Event.Type.LPress);
    }
    void onLeftReleased() {
        leftIsPressed = false;
        postEvent(Event.Type.LRelease);
    }
    void onRightPressed() {
        rightIsPressed = true;
        postEvent(Event.Type.RPress);
    }
    void onRightReleased() {
        rightIsPressed = false;
        postEvent(Event.Type.RRelease);
    }
    void onMiddlePressed() {
        middleIsPressed = true;
        postEvent(Event.Type.MPress);
    }
    void onMiddleReleased() {
        middleIsPressed = false;
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
        return leftIsPressed || middleIsPressed || rightIsPressed;
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


    private static final int bufferSize = 64;
    private int x = 0;
    private int y = 0;
    private boolean leftIsPressed = false;
    private boolean middleIsPressed = false;
    private boolean rightIsPressed = false;
    private boolean isInWindow = false;
    private final Queue<Event> buffer = new LinkedBlockingQueue<>();
    private double wheelDeltaCarryX = 0;
    private double wheelDeltaCarryY = 0;
}
