package xyz.chromabeam.engine.render.world;


import xyz.chromabeam.engine.render.Shader;
import xyz.chromabeam.util.Destroyable;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33C.*;

public abstract class WorldRenderer extends Renderer implements Destroyable {
    private final int projectionMatrixUniform;
    private final Shader shader;
    private final FloatBuffer matrixBuffer;

    protected final int[] childUniforms;

    public WorldRenderer(String vertexShaderName, String fragmentShaderName, String... customUniforms) {
        String[] uniStrings = new String[customUniforms.length + 1];
        uniStrings[0] = "projectionMatrix";
        System.arraycopy(customUniforms, 0, uniStrings, 1, customUniforms.length);
        try {
            shader = Shader.fromShaderResource(vertexShaderName, fragmentShaderName, uniStrings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var unis = shader.getUniforms();
        projectionMatrixUniform = unis[0];
        childUniforms = new int[customUniforms.length];
        System.arraycopy(unis, 1, childUniforms, 0, customUniforms.length);
        matrixBuffer = MemoryUtil.memAllocFloat(9);
    }

    public void render() {
        shader.bind();
        camera.getProjectionMatrix().get3x3(matrixBuffer);
        glUniformMatrix3fv(projectionMatrixUniform, false, matrixBuffer);
        renderContent();
    }

    protected abstract void renderContent();

    @Override
    public void destroy() {
        shader.destroy();
        MemoryUtil.memFree(matrixBuffer);
    }
}
