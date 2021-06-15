package xyz.chromabeam.engine.window;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {
    public static class Event {
        public enum Type {
            Press, Release
        }

        public int keyCode() {
            return code;
        }

        public Type type() {
            return (mask & MASK_BIT_TYPE) == MASK_BIT_TYPE ? Type.Press : Type.Release;
        }

        public boolean shift() {
            return (mask & MASK_BIT_SHIFT) == MASK_BIT_SHIFT;
        }

        public boolean ctrl() {
            return (mask & MASK_BIT_CONTROL) == MASK_BIT_CONTROL;
        }

        public boolean alt() {
            return (mask & MASK_BIT_ALT) == MASK_BIT_ALT;
        }


        private final byte mask;
        private final int code;
        private Event(int keyCode, byte mask) {
            this.code = keyCode;
            this.mask = mask;
        }
    }

    Keyboard(){}

    public boolean keyIsPressed(char keyCode) {
        return keyStates.get(keyCode);
    }

    public Optional<Event> readKey() {
        return Optional.ofNullable(keyBuffer.poll());
    }

    public boolean keyIsEmpty() {
        return keyBuffer.isEmpty();
    }

    public void flushKey() {
        keyBuffer.clear();
    }

    public char readChar() {
        return Objects.requireNonNullElse(charBuffer.poll(), (char)0);
    }

    public boolean charIsEmpty() {
        return charBuffer.isEmpty();
    }

    public void flushChar() {
        charBuffer.clear();
    }

    public void flush() {
        flushKey();
        flushChar();
    }

    void setAutorepeat(boolean enableAutoRepeat) {
        autoRepeat = enableAutoRepeat;
    }

    boolean getAutorepeat() {
        return autoRepeat;
    }

    void onKeyPressed(int keyCode) {
        keyStates.set(keyCode);
        mask |= MASK_BIT_TYPE;
        switch (keyCode) {
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> mask |= MASK_BIT_SHIFT;
            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> mask |= MASK_BIT_CONTROL;
            case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> mask |= MASK_BIT_ALT;
        }
        keyBuffer.add(new Event(keyCode, mask));
        trimBuffer(keyBuffer);
    }

    void onKeyReleased(int keyCode) {
        keyStates.clear(keyCode);
        mask &= ~MASK_BIT_TYPE;
        switch (keyCode) {
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> mask &= ~MASK_BIT_SHIFT;
            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> mask &= ~MASK_BIT_CONTROL;
            case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> mask &= ~MASK_BIT_ALT;
        }
        keyBuffer.add(new Event(keyCode, mask));
        trimBuffer(keyBuffer);
    }

    void onChar(char character) {
        charBuffer.add(character);
        trimBuffer(charBuffer);
    }

    void clearState() {
        keyStates.clear();
    }

    private static <T> void trimBuffer(Queue<T> buffer) {
        while (buffer.size() > bufferSize) {
            buffer.poll();
        }
    }



    private static final byte MASK_BIT_TYPE = 0x01;
    private static final byte MASK_BIT_SHIFT = 0x02;
    private static final byte MASK_BIT_CONTROL = 0x04;
    private static final byte MASK_BIT_ALT = 0x08;
    private static final int nKeys = GLFW_KEY_LAST;
    private static final int bufferSize = 64;

    private byte mask = 0x00;
    private boolean autoRepeat = false;
    private final BitSet keyStates = new BitSet(nKeys);
    private final Queue<Event> keyBuffer = new LinkedBlockingQueue<>();
    private final Queue<Character> charBuffer = new LinkedBlockingQueue<>();
}
