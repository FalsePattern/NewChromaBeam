package moe.falsepattern.chromabeam;

import org.joml.Vector3f;

import java.awt.*;

public final class TestUtil {
    public static Vector3f colorToVector(Color color) {
        return new Vector3f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
    }
}
