package xyz.chromabeam.util;

import org.joml.Vector2f;
import org.lwjgl.opengl.GL20C;

public class GLHelpers {
    public static void glUniform2f(int location, Vector2f vec) {
        GL20C.glUniform2f(location, vec.x, vec.y);
    }
}
