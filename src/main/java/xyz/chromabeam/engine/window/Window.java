package xyz.chromabeam.engine.window;

import xyz.chromabeam.engine.Constants;
import xyz.chromabeam.util.Destroyable;
import xyz.chromabeam.util.WindowsUtil;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glEnable;

public class Window implements Destroyable {
    private static Window singleton;
    private final long address;
    private final List<WindowResizeCallback> resizeCallbacks = new ArrayList<>();
    private final WindowCloseCallback closeCallback;
    private int width;
    private int height;
    public final Keyboard keyboard = new Keyboard();
    public final Mouse mouse = new Mouse();
    private final long hWnd;
    public Window(int width, int height, String title, WindowCloseCallback closeCallback) {
        this.width = width;
        this.height = height;
        if (singleton != null) {
            throw new IllegalStateException("Cannot have more than one opengl window active!");
        }

        if (!glfwInit()) {
            throw new ExceptionInInitializerError("Failed to initialize GLFW runtime!");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, Constants.OPENGL_VERSION_MAJOR);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, Constants.OPENGL_VERSION_MINOR);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        address = glfwCreateWindow(width, height, title, 0, 0);
        if (address == 0L) {
            throw new ExceptionInInitializerError("Failed to create GLFW window!");
        }
        hWnd = WindowsUtil.IS_WINDOWS ? GLFWNativeWin32.glfwGetWin32Window(address) : 0;
        glfwSetWindowSizeCallback(address, this::windowSizeCallback);
        glfwSetWindowCloseCallback(address, this::windowCloseCallback);
        glfwSetWindowFocusCallback(address, this::windowFocusCallback);

        glfwSetKeyCallback(address, this::keyCallback);
        glfwSetCharCallback(address, this::charCallback);

        glfwSetCursorPosCallback(address, this::cursorPosCallback);
        glfwSetMouseButtonCallback(address, this::mouseButtonCallback);
        glfwSetScrollCallback(address, this::scrollCallback);
        this.closeCallback = (closeCallback == null) ? () -> {} : closeCallback;
        singleton = this;
        glfwMakeContextCurrent(address);
        GL.createCapabilities();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }


    @Override
    public void destroy() {
        glfwDestroyWindow(address);
        glfwTerminate();
        singleton = null;
    }

    public void swap() {
        glfwSwapBuffers(address);
    }

    public void show() {
        glfwShowWindow(address);
    }

    public void hide() {
        glfwHideWindow(address);
    }

    public void vSync(int frames) {
        glfwSwapInterval(frames);
    }

    public void addResizeCallback(WindowResizeCallback callback) {
        resizeCallbacks.add(callback);
    }

    public static void pollEvents() {
        glfwPollEvents();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private void windowCloseCallback(long address) {
        closeCallback.accept();
    }

    private void windowSizeCallback(long address, int width, int height) {
        this.width = width;
        this.height = height;
        this.resizeCallbacks.forEach((consumer) -> consumer.windowResize(width, height));
    }

    private void windowFocusCallback(long window, boolean focused) {
        if (!focused) {
            keyboard.clearState();
        }
    }

    private void charCallback(long window, int codepoint) {
        keyboard.onChar((char)codepoint);
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        switch (action) {
            case GLFW_RELEASE:
                keyboard.onKeyReleased(key);
                break;
            case GLFW_PRESS:
                keyboard.onKeyPressed(key);
                break;
            case GLFW_REPEAT:
                if (keyboard.getAutoRepeat()) {
                    keyboard.onKeyPressed(key);
                }
        }
    }

    private void cursorPosCallback(long window, double x, double y) {
        if (WindowsUtil.IS_WINDOWS) {
            //Smart mouse auto-capturing for windows systems
            if (x >= 0 && x < width && y >= 0 && y < height) {
                mouse.onMouseMove((int) x, (int) y);
                if (!mouse.isInWindow()) {
                    WindowsUtil.SetCapture(hWnd);
                    mouse.onMouseEnter();
                }
            } else {
                if (mouse.anyPressed()) {
                    mouse.onMouseMove((int) x, (int) y);
                } else {
                    WindowsUtil.ReleaseCapture(hWnd);
                    mouse.onMouseLeave();
                }
            }
        } else {
            //Primitive input handling for linux, because
            mouse.onMouseMove((int)x, (int)y);
        }
    }

    private void mouseButtonCallback(long window, int button, int action, int mods) {
        switch (button) {
            case GLFW_MOUSE_BUTTON_LEFT:
                if (action == GLFW_PRESS) mouse.onLeftPressed(); else mouse.onLeftReleased();
                break;
            case GLFW_MOUSE_BUTTON_MIDDLE:
                if (action == GLFW_PRESS) mouse.onMiddlePressed(); else mouse.onMiddleReleased();
                break;
            case GLFW_MOUSE_BUTTON_RIGHT:
                if (action == GLFW_PRESS) mouse.onRightPressed(); else mouse.onRightReleased();
        }
    }

    private void scrollCallback(long window, double x, double y) {
        mouse.onWheelDelta(x, y);
    }


}
