package xyz.chromabeam;

import org.joml.Vector2f;
import org.joml.Vector2i;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.component.UserInteractive;
import xyz.chromabeam.engine.render.Camera;
import xyz.chromabeam.engine.window.Keyboard;
import xyz.chromabeam.engine.window.Mouse;
import xyz.chromabeam.world.World2D;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

public class InteractionManager {
    private static final Direction[] dirs = Direction.values();
    private final Keyboard keyboard;
    private final Mouse mouse;
    private final World2D world;
    private final Camera camera;

    private Direction placeDir = Direction.RIGHT;
    private boolean placeFlip = false;
    private final Component[] components; //TODO remove demo code
    private Component selectedComponent;
    public InteractionManager(Keyboard keyboard, Mouse mouse, World2D world, Camera camera, Component[] components) {
        this.keyboard = keyboard;
        this.mouse = mouse;
        this.world = world;
        this.camera = camera;
        this.components = Arrays.copyOf(components, components.length);
        System.out.println("Components:");
        for (int i = 0; i < components.length; i++) {
            System.out.println(i + " -- " + components[i].getName());
        }
        this.selectedComponent = components[0];
        System.out.println("Selected component: " + selectedComponent.getName());
    }

    private final Vector2f cursorProjectionBuffer = new Vector2f();
    private final Vector2i mouseBuffer = new Vector2i();
    //TODO remove demo code and add proper input handling
    public void handleInput() {
        while (true) {
            var opt = keyboard.readKey();
            if (opt.isEmpty()) break;
            var event = opt.get();
            if (event.type() == Keyboard.Event.Type.Press) {
                switch (event.keyCode()) {
                    case GLFW_KEY_0, GLFW_KEY_1, GLFW_KEY_2, GLFW_KEY_3, GLFW_KEY_4, GLFW_KEY_5, GLFW_KEY_6, GLFW_KEY_7, GLFW_KEY_8, GLFW_KEY_9 -> {
                        selectedComponent = components[(event.keyCode() - GLFW_KEY_0) % components.length];
                        System.out.println("Selected component: " + selectedComponent.getName());
                    }
                    case GLFW_KEY_R -> {
                        placeDir = dirs[(placeDir.ordinal() + (event.shift() ? dirs.length - 1 : 1)) % dirs.length];
                        System.out.println("Placement direction: " + placeDir.name());
                    }
                    case GLFW_KEY_F -> {
                        placeFlip = !placeFlip;
                        System.out.println("Placement flip: " + placeFlip);
                    }
                }
            }
        }
        if (!mouse.isEmpty()) {
            camera.screenToWorldSpace(cursorProjectionBuffer.set(mouse.getPos(mouseBuffer)), cursorProjectionBuffer);
            getMouseDelta(mouseBuffer);
        }
        while (true) {
            var opt = mouse.read();
            if (opt.isEmpty()) break;
            var event = opt.get();
            var x = (int)Math.floor(cursorProjectionBuffer.x);
            var y = (int)Math.floor(cursorProjectionBuffer.y);

            if (event.type == Mouse.Event.Type.LPress) {
                var existing = world.get(x, y);
                if (existing != null) {
                    if (existing.isInteractive()) {
                        ((UserInteractive)existing).mouseInteraction();
                    }
                } else {
                    System.out.println("Placed " + selectedComponent.getName() + " at: (" + Math.floor(cursorProjectionBuffer.x) + ", " + Math.floor(cursorProjectionBuffer.y)
                            + "), facing: " + placeDir.name()
                            + ", flipped: " + placeFlip);
                    var newComponent = selectedComponent.newInstance();
                    selectedComponent.copy(newComponent);
                    world.set(x, y, placeDir, placeFlip, newComponent);
                }
            }
            if (event.type == Mouse.Event.Type.RPress) {
                world.remove((int)Math.floor(cursorProjectionBuffer.x), (int)Math.floor(cursorProjectionBuffer.y));
            }
        }
        markMouse();
    }

    private final Vector2i lastMousePos = new Vector2i();
    private void markMouse() {
        mouse.getPos(lastMousePos);
    }

    private void getMouseDelta(Vector2i buffer) {
        mouse.getPos(buffer).sub(lastMousePos);
    }
}
