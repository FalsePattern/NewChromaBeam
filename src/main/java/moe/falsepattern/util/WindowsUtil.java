package moe.falsepattern.util;

import org.lwjgl.system.APIUtil;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.User32;

public final class WindowsUtil {
    public static final boolean IS_WINDOWS = System.getProperty("os.name").contains("win");
    private static final long
            SetCapture = APIUtil.apiGetFunctionAddress(User32.getLibrary(), "SetCapture"),
            ReleaseCapture = APIUtil.apiGetFunctionAddress(User32.getLibrary(), "ReleaseCapture");

    public static long SetCapture(long hWnd) {
        return JNI.callPP(hWnd, SetCapture);
    }
    public static long ReleaseCapture(long hWnd) {
        return JNI.callPP(hWnd, ReleaseCapture);
    }
}