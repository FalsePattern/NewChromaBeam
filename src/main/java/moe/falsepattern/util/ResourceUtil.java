package moe.falsepattern.util;

import lombok.NonNull;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;

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
