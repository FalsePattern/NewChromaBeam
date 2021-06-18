package xyz.chromabeam.ui;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL33C;
import xyz.chromabeam.engine.render.Shader;
import xyz.chromabeam.engine.render.VertexBuffer;
import xyz.chromabeam.engine.render.world.Renderer;
import xyz.chromabeam.engine.window.WindowResizeCallback;

public class ScreenSpaceUIDrawer extends Renderer implements UIDrawer, WindowResizeCallback {
    private static final int VERTEX_DIMENSIONS = 2;
    private static final int VERTEX_COLORS = 4;
    private static final int VERTEX_SIZE = VERTEX_DIMENSIONS + VERTEX_COLORS;
    private static final int QUAD_VERTICES = 6;
    private static final int QUAD_SIZE = VERTEX_SIZE * QUAD_VERTICES;
    private static final int BUFFER_SIZE = 6144;
    private final VertexBuffer UIBuffer;
    private final Shader shader;
    private float horizontalMultiplier;
    private float verticalMultiplier;
    private int queuedVertices = 0;
    private final float[] b = new float[QUAD_SIZE];


    public ScreenSpaceUIDrawer(int width, int height, Shader shader) {
        this.horizontalMultiplier = 2f / width;
        this.verticalMultiplier = -2f / height;
        this.shader = shader;
        UIBuffer = new VertexBuffer(BUFFER_SIZE, VERTEX_DIMENSIONS, VERTEX_COLORS);
    }

    @Override
    public void render() {
        if (queuedVertices > 0) {
            shader.bind();
            UIBuffer.bind();
            GL33C.glDrawArrays(GL11C.GL_TRIANGLES, 0, queuedVertices);
            queuedVertices = 0;
        }
    }

    @Override
    public void drawFilledRect(RectI2D rect, float red, float green, float blue, float alpha) {
        var x = rect.position.x * horizontalMultiplier - 1;
        var y = rect.position.y * verticalMultiplier + 1;
        var w = rect.size.x * horizontalMultiplier;
        var h = rect.size.y * verticalMultiplier;
        if (BUFFER_SIZE - QUAD_VERTICES <= queuedVertices) {
            render();
            queuedVertices = 0;
        }
        var buf = UIBuffer.getBufferForWriting();
        b[0] = x;
        b[1] = y;

        b[24] = b[6] = x;
        b[25] = b[7] = y + h;

        b[18] = b[12] = x + w;
        b[19] = b[13] = y;

        b[30] = x + w;
        b[31] = y + h;

        b[2] = b[8] = b[14] = b[20] = b[26] = b[32] = red;
        b[3] = b[9] = b[15] = b[21] = b[27] = b[33] = green;
        b[4] = b[10] = b[16] = b[22] = b[28] = b[34] = blue;
        b[5] = b[11] = b[17] = b[23] = b[29] = b[35] = alpha;

        buf.put(queuedVertices * VERTEX_SIZE, b);
        queuedVertices += QUAD_VERTICES;
    }

    @Override
    public void accept(int width, int height) {
        this.horizontalMultiplier = 2f / width;
        this.verticalMultiplier = -2f / height;
    }
}
