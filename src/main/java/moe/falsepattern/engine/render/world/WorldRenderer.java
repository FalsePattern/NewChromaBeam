package moe.falsepattern.engine.render.world;


import moe.falsepattern.engine.Bindable;
import moe.falsepattern.engine.render.Camera;
import moe.falsepattern.engine.render.Shader;
import moe.falsepattern.util.Destroyable;

import static moe.falsepattern.util.GLHelpers.glUniform2f;
import static org.lwjgl.opengl.GL20C.glUniform1f;

public abstract class WorldRenderer extends Renderer implements Destroyable {
    private final int cameraUniform;
    private final int aspectUniform;
    private final int zoomUniform;
    private final Shader shader;

    protected final int[] childUniforms;

    public WorldRenderer(String shaderName, String... customUniforms) {
        String[] uniStrings = new String[customUniforms.length + 3];
        uniStrings[0] = "camera";
        uniStrings[1] = "aspect";
        uniStrings[2] = "zoom";
        System.arraycopy(customUniforms, 0, uniStrings, 3, customUniforms.length);
        shader = Shader.fromShaderResource(shaderName, uniStrings);
        var unis = shader.getUniforms();
        cameraUniform = unis[0];
        aspectUniform = unis[1];
        zoomUniform = unis[2];
        childUniforms = new int[customUniforms.length];
        System.arraycopy(unis, 3, childUniforms, 0, customUniforms.length);
    }

    public void render() {
        shader.bind();
        glUniform2f(cameraUniform, camera.pos);
        glUniform2f(aspectUniform, camera.aspect);
        glUniform1f(zoomUniform, camera.getRenderZoom());
        renderContent();
        shader.unbind();
    }

    protected abstract void renderContent();

    @Override
    public void destroy() {
        shader.destroy();
    }
}
