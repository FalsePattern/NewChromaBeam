package moe.falsepattern.engine.render.beam;

import moe.falsepattern.engine.render.*;
import moe.falsepattern.engine.render.world.Renderer;
import moe.falsepattern.engine.render.world.WorldRenderer;
import moe.falsepattern.engine.window.WindowResizeCallback;
import moe.falsepattern.util.Destroyable;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL33C;

public class DeferredRenderer extends Renderer implements Destroyable, WindowResizeCallback {
    private final VertexBuffer buffer;
    private final Shader shader;
    private final FrameBuffer backBuffer;
    private final FrameBuffer tempBuffer;
    private final WorldRenderer subRenderer;
    private final int horizontalblur;
    public DeferredRenderer(int width, int height, WorldRenderer subRenderer, String deferredShader) {
        this.subRenderer = subRenderer;
        shader = Shader.fromShaderResource(deferredShader, "horizontal");
        horizontalblur = shader.getUniforms()[0];
        buffer = new VertexBuffer(4, 4, 2);
        buffer.getBufferForWriting().put(0, new float[]{
                -1, 1 , 0, 1, 0, 1,
                -1, -1, 0, 1, 0, 0,
                1 , -1, 0, 1, 1, 0,
                1 , 1 , 0, 1, 1, 1
        });
        backBuffer = new FrameBuffer(width, height);
        tempBuffer = new FrameBuffer(width, height);
    }

    @Override
    public void setCamera(Camera camera) {
        subRenderer.setCamera(camera);
    }

    public void render() {
        backBuffer.bind();
        Renderer.clear(0, 0, 0, 0);
        subRenderer.render();
        backBuffer.unbind();
        shader.bind();
        buffer.bind();
        backBuffer.getTexture().bind();
        tempBuffer.bind();
        GL33C.glUniform1i(horizontalblur, 1);
        GL33C.glDrawArrays(GL11C.GL_TRIANGLE_FAN, 0, 4);
        tempBuffer.unbind();
        backBuffer.getTexture().unbind();
        tempBuffer.getTexture().bind();
        GL33C.glUniform1i(horizontalblur, 0);
        GL33C.glDrawArrays(GL11C.GL_TRIANGLE_FAN, 0, 4);
        tempBuffer.getTexture().unbind();
        buffer.unbind();
        shader.unbind();
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
