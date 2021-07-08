package xyz.chromabeam.engine.bind;

import org.lwjgl.opengl.GL33C;
import xyz.chromabeam.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33C.*;

public class BindManager {
    private static final Map<Integer, Integer> vaoToEboMap;

    private static final RawResource vaoManager;
    private static final RawResource eboManager;
    private static final RawResource vboManager;
    private static final RawResource textureManager;
    private static final RawResource shaderManager;
    private static final RawResource frameBufferManager;


    static {
        if (Global.DEBUG) {
            vaoToEboMap = new HashMap<>();
            vaoManager = new RawResource("VAO", GL33C::glGenVertexArrays, GL33C::glDeleteVertexArrays, GL33C::glBindVertexArray);
            eboManager = new RawResource("EBO", GL33C::glGenBuffers, GL33C::glDeleteBuffers, (buffer) -> glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer));
            vboManager = new RawResource("VBO", GL33C::glGenBuffers, GL33C::glDeleteBuffers, (buffer) -> glBindBuffer(GL_ARRAY_BUFFER, buffer));
            textureManager = new RawResource("texture", GL33C::glGenTextures, GL33C::glDeleteTextures, (texture) -> glBindTexture(GL_TEXTURE_2D, texture));
            shaderManager = new RawResource("shader", GL33C::glCreateProgram, GL33C::glDeleteProgram, GL33C::glUseProgram);
            frameBufferManager = new RawResource("frame buffer", GL33C::glGenFramebuffers, GL33C::glDeleteFramebuffers, (frameBuffer) -> glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer));
        }
    }
    public static int genVertexArrays() {
        if (Global.DEBUG) {
            int vao = vaoManager.genResource();
            vaoToEboMap.put(vao, 0);
            return vao;
        } else {
            return glGenVertexArrays();
        }
    }
    public static void deleteVertexArrays(int array) {
        if (Global.DEBUG) {
            vaoManager.deleteResource(array);
            vaoToEboMap.remove(array);
        } else {
            glDeleteVertexArrays(array);
        }
    }
    public static void bindVertexArray(int array) {
        if (Global.DEBUG) {
            vaoManager.bindResource(array);
            int ebo = vaoToEboMap.get(array);
            if (ebo != 0) eboManager.bindResource(ebo);
        } else {
            glBindVertexArray(array);
        }
    }
    public static void unbindVertexArray(int array) {
        if (Global.DEBUG) {
            vaoManager.unbindResource(array);
            int ebo = vaoToEboMap.get(array);
            if (ebo != 0) eboManager.unbindResource(ebo);
        } else {
            glBindVertexArray(0);
        }
    }

    public static int genBuffers(int target) {
        if (Global.DEBUG) {
            return switch (target) {
                case GL_ELEMENT_ARRAY_BUFFER -> eboManager.genResource();
                case GL_ARRAY_BUFFER -> vboManager.genResource();
                default -> throw new IllegalArgumentException("Could not debug buffer creation: Unknown buffer type: " + target);
            };
        } else {
            return glGenBuffers();
        }
    }
    public static void deleteBuffers(int target, int buffer) {
        if (Global.DEBUG) {
            switch (target) {
                case GL_ELEMENT_ARRAY_BUFFER -> {
                    if (vaoToEboMap.containsValue(buffer)) throw new IllegalStateException("Tried to delete EBO used by a VAO!");
                    eboManager.deleteResource(buffer);
                }
                case GL_ARRAY_BUFFER -> vboManager.deleteResource(buffer);
                default -> throw new IllegalArgumentException("Could not debug buffer deletion: Unknown buffer type: " + target);
            }
        } else {
            glDeleteBuffers(buffer);
        }
    }
    public static void bindBuffer(int target, int buffer) {
        if (Global.DEBUG) {
            switch (target) {
                case GL_ELEMENT_ARRAY_BUFFER -> {
                    int vao = vaoManager.bound();
                    if (vao == 0) throw new IllegalStateException("Tried to bind EBO when a VAO was not bound!");
                    eboManager.bindResource(buffer);
                    vaoToEboMap.put(vao, buffer);
                }
                case GL_ARRAY_BUFFER -> vboManager.bindResource(buffer);
                default -> throw new IllegalArgumentException("Could not debug buffer bind: Unknown buffer type: " + target);
            }
        } else {
            glBindBuffer(target, buffer);
        }
    }
    public static void unbindBuffer(int target, int buffer) {
        if (Global.DEBUG) {
            switch (target) {
                case GL_ELEMENT_ARRAY_BUFFER -> {
                    int vao = vaoManager.bound();
                    if (vao == 0) throw new IllegalStateException("Tried to unbind EBO when a VAO was not bound!");
                    eboManager.unbindResource(buffer);
                    vaoToEboMap.put(vao, 0);
                }
                case GL_ARRAY_BUFFER -> vboManager.unbindResource(buffer);
                default -> throw new IllegalArgumentException("Could not debug buffer unbind: Unknown buffer type: " + target);
            }
        } else {
            glBindBuffer(target, 0);
        }
    }

    public static int genTextures() {
        return Global.DEBUG ? textureManager.genResource() : glGenTextures();
    }
    public static void deleteTextures(int texture) {
        if (Global.DEBUG) textureManager.deleteResource(texture); else glDeleteTextures(texture);
    }
    public static void bindTexture(int target, int texture) {
        if (Global.DEBUG) {
            if (target != GL_TEXTURE_2D) throw new IllegalArgumentException("Could not bind texture with unknown target type!");
            textureManager.bindResource(texture);
        } else {
            glBindTexture(target, texture);
        }
    }
    public static void unbindTexture(int target, int texture) {
        if (Global.DEBUG) {
            if (target != GL_TEXTURE_2D) throw new IllegalArgumentException("Could not bind texture with unknown target type!");
            textureManager.unbindResource(texture);
        } else {
            glBindTexture(target, 0);
        }
    }

    public static int createProgram() {
        return Global.DEBUG ? shaderManager.genResource() : glCreateProgram();
    }
    public static void deleteProgram(int shader) {
        if (Global.DEBUG) shaderManager.deleteResource(shader); else glDeleteProgram(shader);
    }
    public static void bindShader(int shader) {
        if (Global.DEBUG) shaderManager.bindResource(shader); else glUseProgram(shader);
    }
    public static void unbindShader(int shader) {
        if (Global.DEBUG) shaderManager.unbindResource(shader); else glUseProgram(0);
    }

    public static int genFrameBuffers() {
        return Global.DEBUG ? frameBufferManager.genResource() : glGenFramebuffers();
    }
    public static void deleteFrameBuffers(int frameBuffer) {
        if (Global.DEBUG) frameBufferManager.deleteResource(frameBuffer); else glDeleteFramebuffers(frameBuffer);
    }
    public static void bindFrameBuffer(int frameBuffer) {
        if (Global.DEBUG) frameBufferManager.bindResource(frameBuffer); else glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
    }
    public static void unbindFrameBuffer(int frameBuffer) {
        if (Global.DEBUG) frameBufferManager.unbindResource(frameBuffer); else glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public static int DEBUG_boundBuffer(int target) {
        if (!Global.DEBUG) {
            throw new IllegalStateException("Tried to call debug function from non-debug build!");
        }
        return switch (target) {
            case GL_ELEMENT_ARRAY_BUFFER -> vaoToEboMap.get(vaoManager.bound());
            case GL_ARRAY_BUFFER -> vboManager.bound();
            default -> throw new IllegalArgumentException("Could not retrieve bound buffer: Unknown buffer type: " + target);
        };
    }

    public static int DEBUG_boundVAO() {
        if (!Global.DEBUG) {
            throw new IllegalStateException("Tried to call debug function from non-debug build!");
        }
        return vaoManager.bound();
    }

    public static void DEBUG_verifyAllDeleted() {
        if (!Global.DEBUG) {
            throw new IllegalStateException("Tried to call debug function from non-debug build!");
        }
        var exceptions = new ArrayList<Exception>();
        vaoManager.collectNotDeletedExceptions(exceptions::add);
        vboManager.collectNotDeletedExceptions(exceptions::add);
        eboManager.collectNotDeletedExceptions(exceptions::add);
        textureManager.collectNotDeletedExceptions(exceptions::add);
        shaderManager.collectNotDeletedExceptions(exceptions::add);
        frameBufferManager.collectNotDeletedExceptions(exceptions::add);
        if (exceptions.size() != 0) {
            System.err.println(exceptions.size() + " leaked OpenGL objects: ");
            exceptions.forEach(Throwable::printStackTrace);
        }
    }

}
