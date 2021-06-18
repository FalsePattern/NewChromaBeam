package xyz.chromabeam.engine;

import xyz.chromabeam.engine.window.Keyboard;
import xyz.chromabeam.engine.window.Mouse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InputDispatcher {

    private final Keyboard keyboard;
    private final Mouse mouse;
    private final List<InputHandler> handlers = new ArrayList<>();
    public InputDispatcher(Keyboard keyboard, Mouse mouse) {
        this.keyboard = keyboard;
        this.mouse = mouse;
    }

    public void registerInputHandler(InputHandler inputHandler) {
        handlers.add(inputHandler);
        handlers.sort(Comparator.comparingInt(InputHandler::z).reversed());
    }

    public void unregisterInputHandler(InputHandler inputHandler) {
        handlers.remove(inputHandler);
    }

    public void processInput() {
        while (true) {
            var opt = keyboard.readKey();
            if (opt.isEmpty()) break;
            var event = opt.get();
            for (var handler: handlers) {
                if (handler.keyboardEvent(event)) break;
            }
        }
        while (true) {
            var opt = mouse.read();
            if (opt.isEmpty()) break;
            var event = opt.get();
            for (var handler: handlers) {
                if (handler.mouseEvent(event)) break;
            }
        }
    }
}
