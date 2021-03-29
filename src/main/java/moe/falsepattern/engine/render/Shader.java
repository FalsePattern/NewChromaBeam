package moe.falsepattern.engine.render;

import moe.falsepattern.engine.Bindable;
import moe.falsepattern.util.Destroyable;
import moe.falsepattern.util.ResourceUtil;

import java.util.Arrays;
import java.util.Stack;

import static org.lwjgl.opengl.GL33C.*;

/**
 * Basic wrapper class for GLSL shaders.
 */
public class Shader implements Destroyable, Bindable {
    private final int program;
    private final int[] uniforms;

    public static Shader fromShaderResource(String shaderName, String... uniforms) {
        return new Shader(ResourceUtil.readShaderFromResource(shaderName + ".vert"), ResourceUtil.readShaderFromResource(shaderName + ".frag"), uniforms);
    }

    public Shader(String vertexSource, String fragmentSource, String... uniforms) {
        int vertexShader = compileShader(vertexSource, GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragmentSource, GL_FRAGMENT_SHADER);
        program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        glDetachShader(program, vertexShader);
        glDetachShader(program, fragmentShader);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            var log = glGetProgramInfoLog(program);
            glDeleteProgram(program);
            throw new IllegalStateException("Failed to link shader program: " + log);
        }
        this.uniforms = new int[uniforms.length];
        for (int i = 0; i < uniforms.length; i++) {
            this.uniforms[i] = glGetUniformLocation(program, uniforms[i]);
        }
    }

    public int[] getUniforms() {
        return Arrays.copyOf(uniforms, uniforms.length);
    }

    private static int compileShader(String source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            var log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalArgumentException("Failed to compile shader: " + log);
        }
        return shader;
    }

    private static final Stack<Integer> shaderStack = new Stack<>();

    public void bind() {
        glUseProgram(program);
        shaderStack.push(program);
    }

    public void unbind() {
        shaderStack.pop();
        if (shaderStack.empty()) {
            glUseProgram(0);
        } else {
            glUseProgram(shaderStack.peek());
        }
    }

    @Override
    public void destroy() {
        glDeleteProgram(program);
    }
}
