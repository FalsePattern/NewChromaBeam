package xyz.chromabeam.engine;

import xyz.chromabeam.engine.window.Keyboard;
import xyz.chromabeam.engine.window.Mouse;


public interface InputHandler {
    boolean keyboardEvent(Keyboard.Event event);
    boolean mouseEvent(Mouse.Event event);

    int z();
}
