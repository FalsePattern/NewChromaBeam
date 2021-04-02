package xyz.chromabeam.engine.window;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;

public class Keyboard {
    public static class Event {
        public enum Type {
            Press, Release
        }
        public final Type type;
        public final int code;
        private Event(Type type, int keyCode) {
            this.type = type;
            this.code = keyCode;
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
        keyBuffer.add(new Event(Event.Type.Press, keyCode));
        trimBuffer(keyBuffer);
    }

    void onKeyReleased(int keyCode) {
        keyStates.clear(keyCode);
        keyBuffer.add(new Event(Event.Type.Release, keyCode));
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

    private static final int nKeys = GLFW_KEY_LAST;
    private static final int bufferSize = 64;
    private boolean autoRepeat = false;
    private final BitSet keyStates = new BitSet(nKeys);
    private final Queue<Event> keyBuffer = new LinkedBlockingQueue<>();
    private final Queue<Character> charBuffer = new LinkedBlockingQueue<>();
}
