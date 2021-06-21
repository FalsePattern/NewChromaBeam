package xyz.chromabeam.engine.render.texture;

import xyz.chromabeam.engine.bind.BindManager;
import xyz.chromabeam.engine.Bindable;
import xyz.chromabeam.engine.render.buffer.FrameBuffer;
import xyz.chromabeam.util.Destroyable;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL33C.*;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * The simplest wrapper object for OpenGL textures. This is a 1 to 1 size region that maps to the entire input texture.
 * Used internally for abstracting away GL calls from the rest of the code.
 */
public class Texture implements TextureRegionI, Destroyable, Bindable {
    private final int address;
    private final int w;
    private final int h;
    private final ByteBuffer dynamicBuffer;
    private final boolean dynamic;

    public Texture(BufferedImage image, boolean mipMap) {
        this(image, mipMap, false);
    }

    public Texture(BufferedImage image, boolean mipMap, boolean dynamic) {
        this.dynamic = dynamic;
        address = BindManager.genTextures();
        bind();
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
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        } finally {
            if (dynamic) {
                dynamicBuffer = buf;
            } else {
                dynamicBuffer = null;
                MemoryUtil.memFree(buf);
            }
        }
        configureTexture(mipMap);
        this.w = image.getWidth();
        this.h = image.getHeight();
    }

    public Texture(int width, int height) {
        this(width, height, false);
    }

    public Texture(int width, int height, boolean hdr) {
        this(width, height, hdr, false);
    }

    public Texture(int width, int height, boolean hdr, boolean dynamic) {
        this.dynamic = dynamic;
        address = BindManager.genTextures();
        bind();
        glTexImage2D(GL_TEXTURE_2D, 0,hdr ? GL_RGBA32F : GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        configureTexture(false);
        this.w = width;
        this.h = height;
        if (dynamic) {
            dynamicBuffer = MemoryUtil.memAlloc(w * h * 4);
        } else {
            dynamicBuffer = null;
        }
    }

    private void configureTexture(boolean mipMap) {
        if (mipMap) {
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        unbind();
    }

    public void bind() {
        BindManager.bindTexture(GL_TEXTURE_2D, address);
    }

    public void unbind() {
        BindManager.unbindTexture(GL_TEXTURE_2D, address);
    }

    @Override
    public void destroy() {
        BindManager.deleteTextures(address);
        if (dynamic) {
            MemoryUtil.memFree(dynamicBuffer);
        }
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

    public int address(FrameBuffer.Friend friend) {
        Objects.requireNonNull(friend);
        return address;
    }
}
