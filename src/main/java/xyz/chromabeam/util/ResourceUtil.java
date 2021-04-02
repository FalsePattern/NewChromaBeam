package xyz.chromabeam.util;

import lombok.NonNull;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;

/**
 * Tools for reading resource files from the jar. This is only used where the data is known to exists, so Lombok is
 * used to suppress exception checks. If these throw, the game can't run anyways, so no need to pollute the code
 * with exception checks on deeper levels.
 */
public final class ResourceUtil {

    @SneakyThrows
    public static @NonNull BufferedImage readImageFromResource(@NonNull String path) {
        return ImageIO.read(getStreamFromResource(path));
    }

    @SneakyThrows
    public static @NonNull String readStringFromResource(@NonNull String path) {
        return String.valueOf(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(getStreamFromResource(path).readAllBytes())));
    }

    @SneakyThrows
    public static @NonNull byte[] readFileFromResource(@NonNull String path) {
         return getStreamFromResource(path).readAllBytes();
    }

    @SneakyThrows
    public static @NonNull InputStream getStreamFromResource(@NonNull String path) {
        var stream = ResourceUtil.class.getResourceAsStream(path);
        if (stream == null) {
            throw new MissingResourceException("Could not find texture", "ResourceUtil", path);
        }
        return stream;
    }

    public static @NonNull String getShaderPath(@NonNull String name) {
        return "/xyz/chromabeam/shaders/" + name;
    }

    public static @NonNull String readShaderFromResource(@NonNull String name) {
        return readStringFromResource(getShaderPath(name));
    }
}
