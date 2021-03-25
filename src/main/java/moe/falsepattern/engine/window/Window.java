package moe.falsepattern.engine.window;

import moe.falsepattern.engine.Constants;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glEnable;

public class Window implements AutoCloseable{
    private static Window singleton;
    private final long address;
    private final List<WindowResizeCallback> resizeCallbacks = new ArrayList<>();
    private final WindowCloseCallback closeCallback;
    public Window(int width, int height, String title, WindowCloseCallback closeCallback) {

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
        glfwSetWindowSizeCallback(address, Window::windowSizeCallback);
        glfwSetWindowCloseCallback(address, Window::windowCloseCallback);
        this.closeCallback = (closeCallback == null) ? () -> {} : closeCallback;
        singleton = this;
        glfwMakeContextCurrent(address);
        GL.createCapabilities();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

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

    public void vSync(boolean enabled) {
        glfwSwapInterval(enabled ? 1 : 0);
    }

    public void addResizeCallback(WindowResizeCallback callback) {
        resizeCallbacks.add(callback);
    }

    public static void pollEvents() {
        glfwPollEvents();
    }

    private static void windowCloseCallback(long address) {
        singleton.closeCallback.accept();
    }

    private static void windowSizeCallback(long address, int width, int height) {
        singleton.resizeCallbacks.forEach((consumer) -> consumer.accept(width, height));
    }

    @Override
    public void close() {
        destroy();
    }
}
