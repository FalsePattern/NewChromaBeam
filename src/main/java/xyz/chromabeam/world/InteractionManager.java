package xyz.chromabeam.world;

import org.joml.Vector2f;
import org.joml.Vector2i;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.component.UserInteractive;
import xyz.chromabeam.engine.InputHandler;
import xyz.chromabeam.engine.render.Camera;
import xyz.chromabeam.engine.window.Keyboard;
import xyz.chromabeam.engine.window.Mouse;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

public class InteractionManager implements InputHandler {
    private static final Direction[] dirs = Direction.values();
    private final World2D world;
    private final Camera camera;

    private Direction placeDir = Direction.RIGHT;
    private boolean placeFlip = false;
    private final Component[] components; //TODO remove demo code
    private Component selectedComponent;
    private final int z;
    public InteractionManager(World2D world, Camera camera, Component[] components, int z) {
        this.world = world;
        this.camera = camera;
        this.components = Arrays.copyOf(components, components.length);
        System.out.println("Components:");
        for (int i = 0; i < components.length; i++) {
            System.out.println(i + " -- " + components[i].getName());
        }
        this.selectedComponent = components[0];
        System.out.println("Selected component: " + selectedComponent.getName());
        this.z = z;
    }

    private final Vector2f cursorProjectionBuffer = new Vector2f();
    private final Vector2i mouseBuffer = new Vector2i();

    @Override
    public boolean keyboardEvent(Keyboard.Event event) {
        if (event.type() == Keyboard.Event.Type.Press)
            return switch (event.keyCode()) {
                case GLFW_KEY_0, GLFW_KEY_1, GLFW_KEY_2, GLFW_KEY_3, GLFW_KEY_4, GLFW_KEY_5, GLFW_KEY_6, GLFW_KEY_7, GLFW_KEY_8, GLFW_KEY_9 -> {
                    selectedComponent = components[(event.keyCode() - GLFW_KEY_0) % components.length];
                    System.out.println("Selected component: " + selectedComponent.getName());
                    yield true;
                }
                case GLFW_KEY_R -> {
                    placeDir = dirs[(placeDir.ordinal() + (event.shift() ? dirs.length - 1 : 1)) % dirs.length];
                    System.out.println("Placement direction: " + placeDir.name());
                    yield true;
                }
                case GLFW_KEY_F -> {
                    placeFlip = !placeFlip;
                    System.out.println("Placement flip: " + placeFlip);
                    yield true;
                }
                default -> false;
            };
        else return false;
    }

    @Override
    public boolean mouseEvent(Mouse.Event event) {
        if (event.type() == Mouse.Event.Type.Move) {
            camera.screenToWorldSpace(cursorProjectionBuffer.set(event.getPos(mouseBuffer)), cursorProjectionBuffer);
            getMouseDelta(event, mouseBuffer);
        }
        var x = (int)Math.floor(cursorProjectionBuffer.x);
        var y = (int)Math.floor(cursorProjectionBuffer.y);

        return switch (event.type()) {
            case LPress -> {
                var existing = world.get(x, y);
                if (existing != null) {
                    if (existing.isInteractive()) {
                        ((UserInteractive)existing).mouseInteraction();
                        world.forceTick(x, y);
                    }
                } else {
                    System.out.println("Placed " + selectedComponent.getName() + " at: (" + Math.floor(cursorProjectionBuffer.x) + ", " + Math.floor(cursorProjectionBuffer.y)
                            + "), facing: " + placeDir.name()
                            + ", flipped: " + placeFlip);
                    var newComponent = selectedComponent.newInstance();
                    selectedComponent.copy(newComponent);
                    world.set(x, y, placeDir, placeFlip, newComponent);
                }
                yield true;
            }
            case RPress -> {
                world.remove((int)Math.floor(cursorProjectionBuffer.x), (int)Math.floor(cursorProjectionBuffer.y));
                yield true;
            }
            case Move -> {
                markMouse(event);
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public int z() {
        return z;
    }

    private final Vector2i lastMousePos = new Vector2i();

    private void markMouse(Mouse.Event event) {
        event.getPos(lastMousePos);
    }

    private Vector2i getMouseDelta(Mouse.Event event, Vector2i buffer) {
        return event.getPos(buffer).sub(lastMousePos);
    }
}
