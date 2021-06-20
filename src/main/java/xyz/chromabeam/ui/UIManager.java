package xyz.chromabeam.ui;

import xyz.chromabeam.engine.InputHandler;
import xyz.chromabeam.engine.window.Keyboard;
import xyz.chromabeam.engine.window.Mouse;
import xyz.chromabeam.engine.window.WindowResizeCallback;

public class UIManager extends UIRectangle implements InputHandler, WindowResizeCallback {

    private final int z;
    public UIManager(int width, int height, int z) {
        super(0, 0, width, height, true);
        this.z = z;
    }

    @Override
    public boolean keyboardEvent(Keyboard.Event event) {
        return false;
    }

    @Override
    public boolean mouseEvent(Mouse.Event event) {
        return processMouse(event.x(), event.y(), event.isLeftPressed());
    }

    @Override
    public int z() {
        return z;
    }

    @Override
    public void windowResize(int width, int height) {
        setSize(width, height);
    }
}
