package xyz.chromabeam.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Tools for reading resource files from the jar. This is only used where the data is known to exists, so Lombok is
 * used to suppress exception checks. If these throw, the game can't run anyways, so no need to pollute the code
 * with exception checks on deeper levels.
 */
public final class ResourceUtil {

    public static BufferedImage readImageFromResource(String path) throws IOException {
        return ImageIO.read(getStreamFromResource(path));
    }

    public static String readStringFromResource(String path) throws IOException {
        return String.valueOf(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(getStreamFromResource(path).readAllBytes())));
    }

    public static byte[] readFileFromResource(String path) throws IOException {
         return getStreamFromResource(path).readAllBytes();
    }

    public static InputStream getStreamFromResource(String path) throws IOException {
        var stream = ResourceUtil.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Could not find texture at " + path);
        }
        return stream;
    }

    public static String getShaderPath(String name) {
        return "/xyz/chromabeam/shaders/" + name;
    }

    public static String readShaderFromResource(String name) throws IOException {
        return readStringFromResource(getShaderPath(name));
    }
}
