package moe.falsepattern.engine.render;

import manifold.ext.rt.api.Jailbreak;
import moe.falsepattern.engine.Bindable;
import moe.falsepattern.engine.render.texture.Texture;
import moe.falsepattern.engine.window.WindowResizeCallback;
import moe.falsepattern.util.Destroyable;

import java.util.Stack;

import static org.lwjgl.opengl.GL20C.glDrawBuffers;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL30C.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL32C.glFramebufferTexture;

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
            throw new IllegalStateException("Failed to configure framebuffer!");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void accept(int width, int height) {
        this.width = width;
        this.height = height;
        rebuild();
    }

    private static final Stack<Integer> fbStack = new Stack<>();
    public void bind() {
        fbStack.push(address);
        glBindFramebuffer(GL_FRAMEBUFFER, address);
    }

    public void unbind() {
        fbStack.pop();
        if (fbStack.empty()) {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        } else {
            glBindFramebuffer(GL_FRAMEBUFFER, fbStack.peek());
        }
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
