package xyz.chromabeam.engine.render.beam;

import org.joml.Vector4f;
import xyz.chromabeam.engine.render.Camera;
import xyz.chromabeam.engine.render.FrameBuffer;
import xyz.chromabeam.engine.render.Shader;
import xyz.chromabeam.engine.render.VertexBuffer;
import xyz.chromabeam.engine.render.world.Renderer;
import xyz.chromabeam.engine.render.world.WorldRenderer;
import xyz.chromabeam.engine.window.WindowResizeCallback;
import xyz.chromabeam.util.Destroyable;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL33C;

import java.io.IOException;

public class DeferredRenderer extends Renderer implements Destroyable, WindowResizeCallback {
    private final VertexBuffer buffer;
    private final Shader shader;
    private final FrameBuffer backBuffer;
    private final FrameBuffer tempBuffer;
    private final WorldRenderer subRenderer;
    private final int horizontalBlurUniform;

    private final Vector4f clearColor = new Vector4f(0,0,0,1);
    public DeferredRenderer(int width, int height, WorldRenderer subRenderer, String deferredVertexShader, String deferredFragmentShader) {
        this.subRenderer = subRenderer;
        try {
            shader = Shader.fromShaderResource(deferredVertexShader, deferredFragmentShader, "horizontal");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        horizontalBlurUniform = shader.getUniforms()[0];
        buffer = new VertexBuffer(4, 2, 2);
        buffer.getBufferForWriting().put(0, new float[]{
                -1, 1 , 0, 1,
                -1, -1, 0, 0,
                1 , -1, 1, 0,
                1 , 1 , 1, 1
        });
        backBuffer = new FrameBuffer(width, height);
        tempBuffer = new FrameBuffer(width, height);
    }

    @Override
    public void setCamera(Camera camera) {
        subRenderer.setCamera(camera);
    }

    public void clearColor(float r, float g, float b, float a) {
        clearColor.set(r, g, b, a);
    }

    public void render() {
        backBuffer.bind();
        Renderer.clear(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
        subRenderer.render();

        shader.bind();
        buffer.bind();
        backBuffer.getTexture().bind();
        tempBuffer.bind();
        Renderer.clear(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
        GL33C.glUniform1i(horizontalBlurUniform, 1);
        GL33C.glDrawArrays(GL11C.GL_TRIANGLE_FAN, 0, 4);
        tempBuffer.unbind();

        tempBuffer.getTexture().bind();
        GL33C.glUniform1i(horizontalBlurUniform, 0);
        GL33C.glDrawArrays(GL11C.GL_TRIANGLE_FAN, 0, 4);
    }

    @Override
    public void destroy() {
        shader.destroy();
        buffer.destroy();
        backBuffer.destroy();
        tempBuffer.destroy();
    }

    @Override
    public void accept(int width, int height) {
        backBuffer.accept(width, height);
        tempBuffer.accept(width, height);
    }
}
