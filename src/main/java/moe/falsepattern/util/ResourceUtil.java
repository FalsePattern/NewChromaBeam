package moe.falsepattern.util;

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
        return ImageIO.read(getStreamFromResouce(path));
    }

    @SneakyThrows
    public static @NonNull String readStringFromResource(@NonNull String path) {
        return String.valueOf(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(getStreamFromResouce(path).readAllBytes())));
    }

    @SneakyThrows
    public static @NonNull byte[] readFileFromResource(@NonNull String path) {
         return getStreamFromResouce(path).readAllBytes();
    }

    @SneakyThrows
    public static @NonNull InputStream getStreamFromResouce(@NonNull String path) {
        var stream = ResourceUtil.class.getResourceAsStream(path);
        if (stream == null) {
            throw new MissingResourceException("Could not find texture", "ResourceUtil", path);
        }
        return stream;
    }
}
