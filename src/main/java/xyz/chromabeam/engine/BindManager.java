package xyz.chromabeam.engine;

import xyz.chromabeam.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL33C.*;

public class BindManager {
    private static final Map<Integer, Integer> boundEBO;
    private static final Map<Integer, Exception> lastEBOBind;

    private static int boundVAO = 0;
    private static Exception lastVAOBind;

    private static int boundVBO = 0;
    private static Exception lastVBOBind;

    private static final Map<Integer, Integer> boundTexture;
    private static final Map<Integer, Exception> lastTextureBind;

    private static int boundShader = 0;
    private static Exception lastShaderBind;

    private static int boundFrameBuffer = 0;
    private static Exception lastFrameBufferBind;


    private static final List<Integer> managedVAOs;
    private static final List<Integer> managedVBOs;
    private static final List<Integer> managedEBOs;
    private static final List<Integer> managedTextures;
    private static final List<Integer> managedShaders;
    private static final List<Integer> managedFrameBuffers;

    private static final Map<Integer, Exception> VAOCreation;
    private static final Map<Integer, Exception> VBOCreation;
    private static final Map<Integer, Exception> EBOCreation;
    private static final Map<Integer, Exception> TextureCreation;
    private static final Map<Integer, Exception> ShaderCreation;
    private static final Map<Integer, Exception> FrameBufferCreation;


    static {
        if (Global.DEBUG) {
            boundEBO = new HashMap<>();
            lastEBOBind = new HashMap<>();
            boundTexture = new HashMap<>();
            lastTextureBind = new HashMap<>();
            managedVAOs = new ArrayList<>();
            managedVBOs = new ArrayList<>();
            managedEBOs = new ArrayList<>();
            managedTextures = new ArrayList<>();
            managedShaders = new ArrayList<>();
            managedFrameBuffers = new ArrayList<>();
            VAOCreation = new HashMap<>();
            VBOCreation = new HashMap<>();
            EBOCreation = new HashMap<>();
            TextureCreation = new HashMap<>();
            ShaderCreation = new HashMap<>();
            FrameBufferCreation = new HashMap<>();
            lastVBOBind = lastVAOBind = lastShaderBind = lastFrameBufferBind = new Exception("Never bound");
        } else {
            boundEBO = null;
            boundTexture = null;
            lastTextureBind = null;
            lastVBOBind = lastVAOBind = lastShaderBind = null;
            managedVAOs = managedVBOs = managedEBOs = managedTextures = managedShaders = managedFrameBuffers = null;
            VAOCreation = VBOCreation = EBOCreation = TextureCreation = ShaderCreation = FrameBufferCreation = null;
        }
    }

    public static int genVertexArrays() {
        int array = glGenVertexArrays();
        if (Global.DEBUG) {
            managedVAOs.add(array);
            VAOCreation.put(array, new Exception("VAO with id " + array + " created here:"));
        }
        return array;
    }

    public static void deleteVertexArrays(int array) {
        if (Global.DEBUG) {
            if (array == 0) throw new IllegalArgumentException("Tried to delete VAO with a null pointer!");
            if (!managedVAOs.contains(array)) throw new IllegalArgumentException("Tried to delete unmanaged VAO!");
            if (boundVAO == array) throw new IllegalStateException("Tried to delete bound VAO!");
            managedVAOs.remove(Integer.valueOf(array));
            VAOCreation.remove(array);
        }
        glDeleteVertexArrays(array);
    }

    public static void bindVertexArray(int array) {
        if (Global.DEBUG) {
            if (array == 0) throw new IllegalArgumentException("Tried to bind VAO with a null pointer!");
            if (!managedVAOs.contains(array)) throw new IllegalArgumentException("Tried to bind unmanaged VAO!");
            if (boundVAO != 0) throw new IllegalStateException("Tried to bind VAO when one was already bound!", lastVAOBind);
            boundEBO.putIfAbsent(array, 0);
            lastVAOBind = new Exception("Last bind location stacktrace:");
            boundVAO = array;
        }
        glBindVertexArray(array);
    }

    public static void unbindVertexArray(int array) {
        if (Global.DEBUG) {
            if (array == 0) throw new IllegalArgumentException("Tried to unbind VAO with a null pointer!");
            if (!managedVAOs.contains(array)) throw new IllegalArgumentException("Tried to unbind unmanaged VAO!");
            if (boundVAO == 0) throw new IllegalStateException("Tried to unbind VAO when none was bound!", lastVAOBind);
            if (boundVAO != array) throw new IllegalArgumentException("Tried to unbind VAO when a different one was bound!", lastVAOBind);
            lastVAOBind = new Exception("Last unbind location stacktrace:");
            boundVAO = 0;
        }
        glBindVertexArray(0);
    }

    public static int genBuffers(int target) {
        if (Global.DEBUG) {
            return switch (target) {
                case GL_ELEMENT_ARRAY_BUFFER -> {
                    int ebo = glGenBuffers();
                    managedEBOs.add(ebo);
                    EBOCreation.put(ebo, new Exception("EBO with id " + ebo + " created here:"));
                    yield ebo;
                }
                case GL_ARRAY_BUFFER -> {
                    int vbo = glGenBuffers();
                    managedVBOs.add(vbo);
                    VBOCreation.put(vbo, new Exception("VBO with id " + vbo + " created here:"));
                    yield vbo;
                }
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
                    if (buffer == 0) throw new IllegalArgumentException("Tried to delete EBO with a null pointer!");
                    if (!managedEBOs.contains(buffer)) throw new IllegalArgumentException("Tried to delete unmanaged EBO!");
                    if (boundVAO != 0 && boundEBO.get(boundVAO) == buffer) throw new IllegalStateException("Tried to delete bound EBO!");
                    managedEBOs.remove(Integer.valueOf(buffer));
                    EBOCreation.remove(buffer);
                }
                case GL_ARRAY_BUFFER -> {
                    if (buffer == 0) throw new IllegalArgumentException("Tried to delete VBO with a null pointer!");
                    if (!managedVBOs.contains(buffer)) throw new IllegalArgumentException("Tried to delete unmanaged VBO!");
                    if (boundVBO == buffer) throw new IllegalStateException("Tried to delete bound VBO!");
                    managedVBOs.remove(Integer.valueOf(buffer));
                    VBOCreation.remove(buffer);
                }
                default -> throw new IllegalArgumentException("Could not debug buffer deletion: Unknown buffer type: " + target);
            }
        }
        glDeleteBuffers(buffer);
    }

    public static void bindBuffer(int target, int buffer) {
        if (Global.DEBUG) {
            switch (target) {
                case GL_ELEMENT_ARRAY_BUFFER -> {
                    if (buffer == 0) throw new IllegalArgumentException("Tried to bind EBO with a null pointer!");
                    if (!managedEBOs.contains(buffer)) throw new IllegalArgumentException("Tried to bind unmanaged EBO!");
                    if (boundVAO == 0) throw new IllegalStateException("Tried to bind EBO when a VAO was not bound!");
                    if (boundEBO.get(boundVAO) != 0) throw new IllegalStateException("Tried to bind EBO when one was already bound!", lastEBOBind.computeIfAbsent(boundVAO, (ignored) -> new Exception("Never bound")));
                    boundEBO.put(boundVAO, buffer);
                    lastEBOBind.put(boundVAO, new Exception("Last bind location stacktrace:"));
                }
                case GL_ARRAY_BUFFER -> {
                    if (buffer == 0) throw new IllegalArgumentException("Tried to bind VBO with a null pointer!");
                    if (!managedVBOs.contains(buffer)) throw new IllegalArgumentException("Tried to bind unmanaged VBO!");
                    if (boundVBO != 0) throw new IllegalStateException("Tried to bind VBO when one was already bound!", lastVBOBind);
                    lastVBOBind = new Exception("Last bind location stacktrace:");
                    boundVBO = buffer;
                }
                default -> throw new IllegalArgumentException("Could not debug buffer bind: Unknown buffer type: " + target);
            }
        }
        glBindBuffer(target, buffer);
    }

    public static void unbindBuffer(int target, int buffer) {
        if (Global.DEBUG) {
            switch (target) {
                case GL_ELEMENT_ARRAY_BUFFER -> {
                    if (buffer == 0) throw new IllegalArgumentException("Tried to unbind EBO with a null pointer!");
                    if (!managedEBOs.contains(buffer)) throw new IllegalArgumentException("Tried to unbind unmanaged EBO!");
                    if (boundVAO == 0) throw new IllegalStateException("Tried to unbind EBO while a VAO was not bound!");
                    if (boundEBO.get(boundVAO) == 0) throw new IllegalStateException("Tried to unbind EBO when none was bound!", lastEBOBind.computeIfAbsent(boundVAO, (ignored) -> new Exception("Never bound")));
                    if (boundEBO.get(boundVAO) != buffer) throw new IllegalArgumentException("Tried to unbind EBO when a different one was bound!", lastEBOBind.get(boundVAO));
                    boundEBO.put(boundVAO, 0);
                    lastEBOBind.put(boundVAO, new Exception("Last unbind location stacktrace:"));
                }
                case GL_ARRAY_BUFFER -> {
                    if (buffer == 0) throw new IllegalArgumentException("Tried to unbind VBO with a null pointer!");
                    if (!managedVBOs.contains(buffer)) throw new IllegalArgumentException("Tried to unbind unmanaged VBO!");
                    if (boundVBO == 0) throw new IllegalStateException("Tried to unbind VBO when none was bound!", lastVBOBind);
                    if (boundVBO != buffer) throw new IllegalArgumentException("Tried to unbind VBO when a different one was bound!", lastVBOBind);
                    boundVBO = 0;
                    lastVBOBind = new Exception("Last unbind location stacktrace:");
                }
                default -> throw new IllegalArgumentException("Could not debug buffer unbind: Unknown buffer type: " + target);
            }
        }
        glBindBuffer(target, 0);
    }

    public static int genTextures() {
        int texture = glGenTextures();
        if (Global.DEBUG) {
            managedTextures.add(texture);
            TextureCreation.put(texture, new Exception("Texture with id " + texture + " created here:"));
        }
        return texture;
    }

    public static void deleteTextures(int texture) {
        if (Global.DEBUG) {
            if (texture == 0) throw new IllegalArgumentException("Tried to delete texture with a null pointer!");
            if (!managedTextures.contains(texture)) throw new IllegalArgumentException("Tried to delete unmanaged texture!");
            if (boundTexture.containsValue(texture)) throw new IllegalStateException("Tried to delete bound texture!");
            managedTextures.remove(Integer.valueOf(texture));
            TextureCreation.remove(texture);
        }
        glDeleteTextures(texture);
    }

    public static void bindTexture(int target, int texture) {
        if (Global.DEBUG) {
            if (texture == 0) throw new IllegalArgumentException("Tried to bind texture with a null pointer!");
            if (!managedTextures.contains(texture)) throw new IllegalArgumentException("Tried to bind unmanaged texture!");
            int bound = boundTexture.computeIfAbsent(target, (ignored) -> 0);
            if (bound != 0) throw new IllegalStateException("Tried to bind texture when one was already bound!", lastTextureBind.get(target));
            boundTexture.put(target, texture);
            lastTextureBind.put(target, new Exception("Last bind location stacktrace:"));
        }
        glBindTexture(target, texture);
    }

    public static void unbindTexture(int target, int texture) {
        if (Global.DEBUG) {
            if (texture == 0) throw new IllegalStateException("Tried to unbind texture with a null pointer!");
            if (!managedTextures.contains(texture)) throw new IllegalArgumentException("Tried to unbind unmanaged texture!");
            int bound = boundTexture.computeIfAbsent(target, (ignored) -> 0);
            if (bound == 0) throw new IllegalStateException("Tried to unbind texture when none was bound!", lastTextureBind.computeIfAbsent(target, (ignored) -> new Exception("Never bound")));
            if (bound != texture) throw new IllegalArgumentException("Tried to unbind texture when a different one was bound!", lastTextureBind.get(target));
            boundTexture.put(target, 0);
            lastTextureBind.put(target, new Exception("Last unbind location stacktrace:"));
        }
        glBindTexture(target, 0);
    }

    public static int createProgram() {
        int shader = glCreateProgram();
        if (Global.DEBUG) {
            managedShaders.add(shader);
            ShaderCreation.put(shader, new Exception("Shader with id " + shader + " created here:"));
        }
        return shader;
    }

    public static void deleteProgram(int shader) {
        if (Global.DEBUG) {
            if (shader == 0) throw new IllegalArgumentException("Tried to delete shader with a null pointer!");
            if (!managedShaders.contains(shader)) throw new IllegalArgumentException("Tried to delete unmanaged shader!");
            if (boundShader == shader) throw new IllegalStateException("Tried to delete bound shader!");
            managedShaders.remove(Integer.valueOf(shader));
            ShaderCreation.remove(shader);
        }
        glDeleteProgram(shader);
    }

    public static void bindShader(int shader) {
        if (Global.DEBUG) {
            if (shader == 0) throw new IllegalArgumentException("Tried to bind shader with a null pointer!");
            if (!managedShaders.contains(shader)) throw new IllegalArgumentException("Tried to bind unmanaged shader!");
            if (boundShader != 0) throw new IllegalStateException("Tried to bind shader when one was already bound!", lastShaderBind);
            boundShader = shader;
            lastShaderBind = new Exception("Last bind location stacktrace:");
        }
        glUseProgram(shader);
    }

    public static void unbindShader(int shader) {
        if (Global.DEBUG) {
            if (shader == 0) throw new IllegalArgumentException("Tried to unbind shader with a null pointer!");
            if (!managedShaders.contains(shader)) throw new IllegalArgumentException("Tried to unbind unmanaged shader!");
            if (boundShader == 0) throw new IllegalStateException("Tried to unbind shader when none was bound!", lastShaderBind);
            if (shader != boundShader) throw new IllegalStateException("Tried to unbind shader when a different one was bound!", lastShaderBind);
            boundShader = 0;
            lastShaderBind = new Exception("Last unbind location stacktrace:");
        }
        glUseProgram(0);
    }

    public static int genFrameBuffers() {
        int frameBuffer = glGenFramebuffers();
        if (Global.DEBUG) {
            managedFrameBuffers.add(frameBuffer);
            FrameBufferCreation.put(frameBuffer, new Exception("Frame buffer with id " + frameBuffer + " created here:"));
        }
        return frameBuffer;
    }

    public static void deleteFrameBuffers(int frameBuffer) {
        if (Global.DEBUG) {
            if (frameBuffer == 0) throw new IllegalArgumentException("Tried to delete frame buffer with a null pointer!");
            if (!managedFrameBuffers.contains(frameBuffer)) throw new IllegalArgumentException("Tried to delete unmanaged frame buffer!");
            if (boundFrameBuffer == frameBuffer) throw new IllegalStateException("Tried to delete bound frame buffer!");
            managedFrameBuffers.remove(Integer.valueOf(frameBuffer));
            FrameBufferCreation.remove(frameBuffer);
        }
        glDeleteFramebuffers(frameBuffer);
    }

    public static void bindFrameBuffer(int frameBuffer) {
        if (Global.DEBUG) {
            if (frameBuffer == 0) throw new IllegalArgumentException("Tried to bind frame buffer with a null pointer!");
            if (!managedFrameBuffers.contains(frameBuffer)) throw new IllegalArgumentException("Tried to bind unmanaged frame buffer!");
            if (boundFrameBuffer != 0) throw new IllegalStateException("Tried to bind frame buffer when one was already bound!", lastFrameBufferBind);
            boundFrameBuffer = frameBuffer;
            lastFrameBufferBind = new Exception("Last bind location stacktrace:");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
    }

    public static void unbindFrameBuffer(int frameBuffer) {
        if (Global.DEBUG) {
            if (frameBuffer == 0) throw new IllegalArgumentException("Tried to unbind frame buffer with a null pointer!");
            if (!managedFrameBuffers.contains(frameBuffer)) throw new IllegalArgumentException("Tried to unbind unmanaged frame buffer!");
            if (boundFrameBuffer == 0) throw new IllegalStateException("Tried to unbind frame buffer when none was bound!", lastFrameBufferBind);
            if (frameBuffer != boundFrameBuffer) throw new IllegalStateException("Tried to unbind frame buffer when a different one was bound!", lastFrameBufferBind);
            boundFrameBuffer = 0;
            lastFrameBufferBind = new Exception("Last unbind location stacktrace:");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public static int DEBUG_boundBuffer(int target) {
        if (Global.DEBUG) {
            return switch (target) {
                case GL_ELEMENT_ARRAY_BUFFER -> boundEBO.get(boundVBO);
                case GL_ARRAY_BUFFER -> boundVBO;
                default -> throw new IllegalArgumentException("Could not retrieve bound buffer: Unknown buffer type: " + target);
            };
        }
        throw new IllegalStateException("Tried to call debug function from non-debug build!");
    }

    public static void DEBUG_verifyAllDeleted() {
        if (!Global.DEBUG) {
            throw new IllegalStateException("Tried to call debug function from non-debug build!");
        }
        var exceptions = new ArrayList<Exception>();
        collectExceptions(managedVAOs, VAOCreation, exceptions);
        collectExceptions(managedVBOs, VBOCreation, exceptions);
        collectExceptions(managedEBOs, EBOCreation, exceptions);
        collectExceptions(managedTextures, TextureCreation, exceptions);
        collectExceptions(managedShaders, ShaderCreation, exceptions);
        collectExceptions(managedFrameBuffers, FrameBufferCreation, exceptions);
        if (exceptions.size() != 0) {
            System.err.println(exceptions.size() + " leaked OpenGL objects: ");
            exceptions.forEach(Throwable::printStackTrace);
        }
    }

    private static void collectExceptions(List<Integer> managedList, Map<Integer, Exception> exceptionMap, List<Exception> accumulator) {
        managedList.forEach((object) -> accumulator.add(exceptionMap.get(object)));
    }

}
