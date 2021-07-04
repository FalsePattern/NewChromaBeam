package xyz.chromabeam.util.system;

import org.lwjgl.glfw.GLFWNativeWin32;

public class NativeUtil {
    public static final boolean IS_WINDOWS = System.getProperty("os.name").contains("win");

    public static void captureMouse(long glfwWindow) {
        if (IS_WINDOWS) {
            WindowsUtil.SetCapture(GLFWNativeWin32.glfwGetWin32Window(glfwWindow));
        } else {
            throw new UnsupportedOperationException("Mouse capture is not supported on linux yet!");
        }
    }

    public static void releaseMouse(long glfwWindow) {
        if (IS_WINDOWS) {
            WindowsUtil.ReleaseCapture(GLFWNativeWin32.glfwGetWin32Window(glfwWindow));
        } else {
            throw new UnsupportedOperationException("Mouse capture is not supported on linux yet!");
        }
    }

    public static void initialize() {
        if (IS_WINDOWS) {
            WindowsUtil.init();
        }
    }

    public static boolean systemSupportsMouseCapture() {
        return IS_WINDOWS;
    }
}
