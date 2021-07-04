package xyz.chromabeam.util.system;

import org.lwjgl.system.APIUtil;
import org.lwjgl.system.JNI;
import org.lwjgl.system.windows.User32;

public final class WindowsUtil {
    private static long SetCapture, ReleaseCapture;
    public static void init() {
        SetCapture = APIUtil.apiGetFunctionAddress(User32.getLibrary(), "SetCapture");
        ReleaseCapture = APIUtil.apiGetFunctionAddress(User32.getLibrary(), "ReleaseCapture");
    }


    public static long SetCapture(long hWnd) {
        return JNI.callPP(hWnd, SetCapture);
    }
    public static long ReleaseCapture(long hWnd) {
        return JNI.callPP(hWnd, ReleaseCapture);
    }
}