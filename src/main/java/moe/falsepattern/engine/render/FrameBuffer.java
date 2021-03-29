package moe.falsepattern.engine.render;

import manifold.ext.rt.api.Jailbreak;
import moe.falsepattern.engine.Bindable;
import moe.falsepattern.engine.render.texture.Texture;
import moe.falsepattern.engine.window.WindowResizeCallback;
import moe.falsepattern.util.Destroyable;

import static org.lwjgl.opengl.GL33C.*;

public class FrameBuffer implements WindowResizeCallback, Destroyable, Bindable {

    private int width;
    private int height;
    private final int address;

    @Jailbreak //Access private stuff without reflection
    private Texture texture;

    public FrameBuffer(int width, int height) {
        address = glGenFramebuffers();
        accept(width, height);
    }

    private void rebuild() {
        if (texture != null) {
            texture.destroy();
        }
        glBindFramebuffer(GL_FRAMEBUFFER, address);
        texture = new Texture(width, height, true);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture.address, 0);
        glDrawBuffers(GL_COLOR_ATTACHMENT0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Failed to configure frame buffer!");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void accept(int width, int height) {
        this.width = width;
        this.height = height;
        rebuild();
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, address);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public Texture getTexture() {
        return texture;
    }

    @Override
    public void destroy() {
        if (texture != null) {
            texture.destroy();
        }
        glDeleteFramebuffers(address);
    }
}
