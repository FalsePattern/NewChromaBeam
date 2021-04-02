package moe.falsepattern.engine.render.world;


import moe.falsepattern.engine.render.Shader;
import moe.falsepattern.util.Destroyable;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static moe.falsepattern.util.GLHelpers.glUniform2f;
import static org.lwjgl.opengl.GL33C.*;

public abstract class WorldRenderer extends Renderer implements Destroyable {
    private final int projectionMatrixUniform;
    private final Shader shader;
    private final FloatBuffer matrixBuffer;

    protected final int[] childUniforms;

    public WorldRenderer(String shaderName, String... customUniforms) {
        String[] uniStrings = new String[customUniforms.length + 1];
        uniStrings[0] = "projectionMatrix";
        System.arraycopy(customUniforms, 0, uniStrings, 1, customUniforms.length);
        shader = Shader.fromShaderResource(shaderName, uniStrings);
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
