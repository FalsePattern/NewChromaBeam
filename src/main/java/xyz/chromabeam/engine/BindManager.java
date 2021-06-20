package xyz.chromabeam.engine;

import xyz.chromabeam.Global;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33C.*;

public class BindManager {
    private static final Map<Integer, Integer> EBO_MAP;

    private static int boundVAO = 0;
    private static Exception lastVAOBind;

    private static int boundEBO = 0;
    private static Exception lastEBOBind;

    private static int boundVBO = 0;
    private static Exception lastVBOBind;

    private static final Map<Integer, Integer> boundTexture;
    private static final Map<Integer, Exception> lastTextureBind;

    private static int boundProgram = 0;
    private static Exception lastProgramUse;

    private static int boundFrameBuffer = 0;
    private static Exception lastFrameBufferBind;


    static {
        if (Global.DEBUG) {
            EBO_MAP = new HashMap<>();
            boundTexture = new HashMap<>();
            lastTextureBind = new HashMap<>();
            lastEBOBind = lastVBOBind = lastVAOBind = lastProgramUse = lastFrameBufferBind = new Exception("Never bound");
        } else {
            EBO_MAP = null;
            boundTexture = null;
            lastTextureBind = null;
            lastEBOBind = lastVBOBind = lastVAOBind = lastProgramUse = null;
        }
    }

    public static void bindVertexArray(int array) {
        if (Global.DEBUG) {
            if (array != 0 && boundVAO != 0) throw new IllegalStateException("Tried to bind VAO when one was already bound!", lastVAOBind);
            if (array == 0 && boundVAO == 0) throw new IllegalStateException("Tried to unbind VAO when none was bound!", lastVAOBind);
            if (array != 0 && EBO_MAP.containsKey(array)) {
                boundEBO = EBO_MAP.get(array);
            }
            lastVAOBind = new Exception("Last " + (array == 0 ? "unbind" : "bind") + " location stacktrace:");
            boundVAO = array;
        }
        glBindVertexArray(array);
    }

    public static void bindBuffer(int target, int buffer) {
        if (Global.DEBUG) {
            switch (target) {
                case GL_ELEMENT_ARRAY_BUFFER -> {
                    if (buffer != 0 && boundEBO != 0) throw new IllegalStateException("Tried to bind EBO when one was already bound!", lastEBOBind);
                    if (buffer == 0 && boundEBO == 0) throw new IllegalStateException("Tried to unbind EBO when none was bound!", lastEBOBind);
                    if (boundVAO != 0) {
                        EBO_MAP.put(boundVAO, buffer);
                    }
                    lastEBOBind = new Exception("Last " + (buffer == 0 ? "unbind" : "bind") + " location stacktrace:");
                    boundEBO = buffer;
                }
                case GL_ARRAY_BUFFER -> {
                    if (buffer != 0 && boundVBO != 0) throw new IllegalStateException("Tried to bind VBO when one was already bound!", lastVBOBind);
                    if (buffer == 0 && boundVBO == 0) throw new IllegalStateException("Tried to unbind VBO when none was bound!", lastVBOBind);
                    lastVBOBind = new Exception("Last " + (buffer == 0 ? "unbind" : "bind") + " location stacktrace:");
                    boundVBO = buffer;
                }
                default -> throw new IllegalArgumentException("Could not debug buffer bind: Unknown buffer type: " + target);
            }
        }
        glBindBuffer(target, buffer);
    }

    public static void bindTexture(int target, int texture) {
        if (Global.DEBUG) {
            int bound = boundTexture.computeIfAbsent(target, (ignored) -> 0);
            if (bound != 0 && texture != 0) throw new IllegalStateException("Tried to bind texture when one was already bound!", lastTextureBind.get(target));
            if (bound == 0 && texture == 0) throw new IllegalStateException("Tried to unbind texture when none was bound!", lastTextureBind.computeIfAbsent(target, (ignored) -> new Exception("Never bound")));
            boundTexture.put(target, texture);
            lastTextureBind.put(target, new Exception("Last " + (texture == 0 ? "unbind" : "bind") + " location stacktrace:"));
        }
        glBindTexture(target, texture);
    }

    public static void useProgram(int program) {
        if (Global.DEBUG) {
            if (boundProgram != 0 && program != 0) throw new IllegalStateException("Tried to use shader program when one was already in use!", lastProgramUse);
            if (boundProgram == 0 && program == 0) throw new IllegalStateException("Tried to release shader program when none was in use!", lastProgramUse);
            boundProgram = program;
            lastProgramUse = new Exception("Last " + (program == 0 ? "release" : "use") + " location stacktrace:");
        }
        glUseProgram(program);
    }

    public static void bindFrameBuffer(int frameBuffer) {
        if (Global.DEBUG) {
            if (boundFrameBuffer != 0 && frameBuffer != 0) throw new IllegalStateException("Tried to bind framebuffer when one was already bound!", lastFrameBufferBind);
            if (boundFrameBuffer == 0 && frameBuffer == 0) throw new IllegalStateException("Tried to unbind framebuffer when none was bound!", lastFrameBufferBind);
            boundFrameBuffer = frameBuffer;
            lastFrameBufferBind = new Exception("Last " + (frameBuffer == 0 ? "unbind" : "bind") + " location stacktrace:");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
    }

    public static int DEBUG_boundBuffer(int target) {
        if (Global.DEBUG) {
            return switch (target) {
                case GL_ELEMENT_ARRAY_BUFFER -> boundEBO;
                case GL_ARRAY_BUFFER -> boundVBO;
                default -> throw new IllegalArgumentException("Could not retrieve bound buffer: Unknown buffer type: " + target);
            };
        }
        throw new IllegalStateException("Tried to call debug function from non-debug build!");
    }

}
