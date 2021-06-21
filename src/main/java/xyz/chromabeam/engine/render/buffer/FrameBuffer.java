package xyz.chromabeam.engine.render.buffer;

import xyz.chromabeam.engine.bind.BindManager;
import xyz.chromabeam.engine.Bindable;
import xyz.chromabeam.engine.render.texture.Texture;
import xyz.chromabeam.engine.window.WindowResizeCallback;
import xyz.chromabeam.util.Destroyable;

import static org.lwjgl.opengl.GL33C.*;

public class FrameBuffer implements WindowResizeCallback, Destroyable, Bindable {

    public static class Friend {
        private Friend() {}
        private static final Friend FRIEND = new Friend();
    }

    private int width;
    private int height;
    private final int address;

    private Texture texture;

    public FrameBuffer(int width, int height) {
        address = BindManager.genFrameBuffers();
        windowResize(width, height);
    }

    private void rebuild() {
        if (texture != null) {
            texture.destroy();
        }
        bind();
        texture = new Texture(width, height, true);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture.address(Friend.FRIEND), 0);
        glDrawBuffers(GL_COLOR_ATTACHMENT0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Failed to configure frame buffer!");
        }
        unbind();
    }

    @Override
    public void windowResize(int width, int height) {
        this.width = width;
        this.height = height;
        rebuild();
    }

    public void bind() {
        BindManager.bindFrameBuffer(address);
    }

    public void unbind() {
        BindManager.unbindFrameBuffer(address);
    }

    public Texture getTexture() {
        return texture;
    }

    @Override
    public void destroy() {
        if (texture != null) {
            texture.destroy();
        }
        BindManager.deleteFrameBuffers(address);
    }
}
