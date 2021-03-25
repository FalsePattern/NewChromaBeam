package moe.falsepattern.engine.render.texture;

import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL33C.*;

import java.awt.image.BufferedImage;

public class Texture implements TextureRegionI{
    private final int address;
    private final int w;
    private final int h;
    public Texture(BufferedImage image, boolean mipMap) {
        address = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, address);
        var buf = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * 4);
        try {
            {
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        int i = (y * image.getWidth() + x) * 4;
                        int rgb = image.getRGB(x, y);
                        buf.put(i, (byte) ((rgb >> 16) & 0xff));
                        buf.put(i + 1, (byte) ((rgb >>  8) & 0xff));
                        buf.put(i + 2, (byte) (rgb & 0xff));
                        buf.put(i + 3, (byte) ((rgb >> 24) & 0xff));
                    }
                }
            }
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
            if (mipMap) {
                glGenerateMipmap(GL_TEXTURE_2D);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            }
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        } finally {
            MemoryUtil.memFree(buf);
        }
        glBindTexture(GL_TEXTURE_2D, 0);
        this.w = image.getWidth();
        this.h = image.getHeight();
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, address);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void destroy() {
        glDeleteTextures(address);
    }

    @Override
    public float u0() {
        return 0;
    }

    @Override
    public float v0() {
        return 0;
    }

    @Override
    public float u1() {
        return 1;
    }

    @Override
    public float v1() {
        return 1;
    }

    @Override
    public int x() {
        return 0;
    }

    @Override
    public int y() {
        return 0;
    }

    @Override
    public int width() {
        return w;
    }

    @Override
    public int height() {
        return h;
    }
}
