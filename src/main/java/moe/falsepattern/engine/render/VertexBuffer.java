package moe.falsepattern.engine.render;

import moe.falsepattern.engine.Bindable;
import moe.falsepattern.util.Destroyable;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.Stack;

import static org.lwjgl.opengl.GL33C.*;

public class VertexBuffer implements Bindable, Destroyable {

    private final int vao;
    private final int vbo;
    private final FloatBuffer buffer;
    private boolean changed = false;
    private final int attribs;
    public VertexBuffer(int vertices, int... attributes) {
        attribs = attributes.length;
        int floatsPerVertex = 0;
        for (var attrib: attributes) {
            floatsPerVertex += attrib;
        }
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices * floatsPerVertex * 4, GL_DYNAMIC_DRAW);
        int stride = floatsPerVertex * 4;
        int offset = 0;
        for (int i = 0; i < attribs; i++) {
            glVertexAttribPointer(i, attributes[i], GL_FLOAT, false, stride, offset);
            offset += attributes[i] * 4;
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        if (vaoStack.empty()) {
            glBindVertexArray(0);
        } else {
            glBindVertexArray(vaoStack.peek());
        }
        buffer = MemoryUtil.memCallocFloat(vertices * floatsPerVertex);
    }

    public FloatBuffer getBufferForWriting() {
        changed = true;
        return buffer;
    }

    private static final Stack<Integer> vaoStack = new Stack<>();
    @Override
    public void bind() {
        if (changed) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            changed = false;
        }
        vaoStack.push(vao);
        glBindVertexArray(vao);
        for (int i = 0; i < attribs; i++) {
            glEnableVertexAttribArray(i);
        }
    }

    @Override
    public void unbind() {
        vaoStack.pop();
        for (int i = 0; i < attribs; i++) {
            glDisableVertexAttribArray(i);
        }
        if (vaoStack.empty()) {
            glBindVertexArray(0);
        } else {
            glBindVertexArray(vaoStack.peek());
        }
    }

    @Override
    public void destroy() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        MemoryUtil.memFree(buffer);
    }
}
