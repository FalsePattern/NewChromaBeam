package xyz.chromabeam.ui;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL33C;
import xyz.chromabeam.engine.render.Shader;
import xyz.chromabeam.engine.render.buffer.VertexArray;
import xyz.chromabeam.engine.render.world.Renderer;
import xyz.chromabeam.engine.window.WindowResizeCallback;
import xyz.chromabeam.ui.font.Font;
import xyz.chromabeam.util.Destroyable;

public class ScreenSpaceUIDrawer extends Renderer implements UIDrawer, WindowResizeCallback, Destroyable {
    private static final int VERTEX_DIMENSIONS = 2;
    private static final int VERTEX_COLORS = 4;
    private static final int VERTEX_UV = 2;
    private static final int QUAD_VERTICES = 6;
    private static final int BUFFER_SIZE = 6144;

    private final VertexArray uiBuffer;
    private final VertexArray textBuffer;
    private final Shader flatShader;
    private final Shader fontShader;
    private float horizontalMultiplier;
    private float verticalMultiplier;
    private int queuedVertices = 0;
    private final float[] b;
    private Font activeFont;
    private boolean fontMode = false;

    private final Vector4f drawBuffer = new Vector4f();
    private final Vector2f posBuffer = new Vector2f();

    private Vector4f unproject(RectI2D input, Vector4f buffer) {
        return buffer.set(input.position.x * horizontalMultiplier - 1, input.position.y * verticalMultiplier + 1, input.size.x * horizontalMultiplier, input.size.y * verticalMultiplier);
    }

    private Vector4f unproject(int x, int y, int w, int h, Vector4f buffer) {
        return buffer.set(x * horizontalMultiplier - 1, y * verticalMultiplier + 1, w * horizontalMultiplier, h * verticalMultiplier);
    }

    private Vector2f unprojectPos(int x, int y, Vector2f buffer) {
        return buffer.set(x * horizontalMultiplier - 1, y * verticalMultiplier + 1);
    }

    public ScreenSpaceUIDrawer(int width, int height, Shader flatShader, Shader fontShader) {
        this.horizontalMultiplier = 2f / width;
        this.verticalMultiplier = -2f / height;
        this.flatShader = flatShader;
        this.fontShader = fontShader;
        uiBuffer = new VertexArray(BUFFER_SIZE, VERTEX_DIMENSIONS, VERTEX_COLORS);
        textBuffer = new VertexArray(BUFFER_SIZE, VERTEX_DIMENSIONS, VERTEX_UV);
        b = new float[Math.max(uiBuffer.floatsPerVertex, textBuffer.floatsPerVertex) * QUAD_VERTICES];
    }

    @Override
    public void render() {
        if (queuedVertices > 0) {
            if (fontMode) {
                fontShader.bind();
                textBuffer.bind();
                activeFont.texture.bind();
            } else {
                flatShader.bind();
                uiBuffer.bind();
            }
            GL33C.glDrawArrays(GL11C.GL_TRIANGLES, 0, queuedVertices);
            queuedVertices = 0;
            if (fontMode) {
                activeFont.texture.unbind();
                textBuffer.unbind();
                fontShader.unbind();
            } else {
                uiBuffer.unbind();
                flatShader.unbind();
            }
        }
    }

    @Override
    public void drawFilledRect(RectI2D rect, float red, float green, float blue, float alpha) {
        if (fontMode) {
            render();
            fontMode = false;
        }
        var dim = unproject(rect, drawBuffer);
        if (BUFFER_SIZE - QUAD_VERTICES <= queuedVertices) {
            render();
            queuedVertices = 0;
        }
        b[0]  = dim.x;         b[1]  = dim.y;         b[2]  = red; b[3]  = green; b[4]  = blue; b[5]  = alpha;
        b[6]  = dim.x;         b[7]  = dim.y + dim.w; b[8]  = red; b[9]  = green; b[10] = blue; b[11] = alpha;
        b[12] = dim.x + dim.z; b[13] = dim.y;         b[14] = red; b[15] = green; b[16] = blue; b[17] = alpha;
        b[18] = dim.x + dim.z; b[19] = dim.y;         b[20] = red; b[21] = green; b[22] = blue; b[23] = alpha;
        b[24] = dim.x;         b[25] = dim.y + dim.w; b[26] = red; b[27] = green; b[28] = blue; b[29] = alpha;
        b[30] = dim.x + dim.z; b[31] = dim.y + dim.w; b[32] = red; b[33] = green; b[34] = blue; b[35] = alpha;

        uiBuffer.getWriteBuffer().put(queuedVertices * uiBuffer.floatsPerVertex, b, 0, uiBuffer.floatsPerVertex * QUAD_VERTICES);
        queuedVertices += QUAD_VERTICES;
    }

    @Override
    public RectI2D getTextRect(int x, int y, String text, RectI2D buffer) {
        var chars = text.toCharArray();
        buffer.position.set(x, y);
        buffer.size.y = activeFont.lineHeight;
        int width = 0;
        for (int i = 0, charsLength = chars.length; i < charsLength; i++) {
            var glyph = activeFont.getGlyph(chars[i]);
            if (i > 0) {
                width += activeFont.getKerning(chars[i - 1], chars[i]);
            }
            width += glyph.xAdvance();
        }
        buffer.size.x = width;
        return buffer;
    }

    @Override
    public void drawText(int x, int y, String text) {
        if (!fontMode) {
            render();
            fontMode = true;
        }
        float X = x * horizontalMultiplier - 1;
        float Y = y * verticalMultiplier + 1;
        var chars = text.toCharArray();
        int row = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\n') {
                row++;
                X = x * horizontalMultiplier - 1;
                Y = (y + row * activeFont.lineHeight) * verticalMultiplier + 1;
                continue;
            }
            if (BUFFER_SIZE - QUAD_VERTICES <= queuedVertices) {
                render();
                queuedVertices = 0;
            }
            var glyph = activeFont.getGlyph(chars[i]);
            var W = glyph.width() * horizontalMultiplier;
            var H = glyph.height() * verticalMultiplier;
            if (i > 0) {
                X += activeFont.getKerning(chars[i - 1], chars[i]) * horizontalMultiplier;
            }
            var XX = X + (glyph.xOffset() * horizontalMultiplier);
            var YY = Y + (glyph.yOffset() * verticalMultiplier);
            var region = activeFont.getRegion(chars[i]);
            b[0]  = XX;     b[1]  = YY;     b[2]  = region.u0(); b[3]  = region.v0();
            b[4]  = XX;     b[5]  = YY + H; b[6]  = region.u0(); b[7]  = region.v1();
            b[8]  = XX + W; b[9]  = YY;     b[10] = region.u1(); b[11] = region.v0();
            b[12] = XX + W; b[13] = YY;     b[14] = region.u1(); b[15] = region.v0();
            b[16] = XX;     b[17] = YY + H; b[18] = region.u0(); b[19] = region.v1();
            b[20] = XX + W; b[21] = YY + H; b[22] = region.u1(); b[23] = region.v1();
            textBuffer.getWriteBuffer().put(queuedVertices * textBuffer.floatsPerVertex, b, 0, textBuffer.floatsPerVertex * QUAD_VERTICES);
            queuedVertices += QUAD_VERTICES;
            X += glyph.xAdvance() * horizontalMultiplier;
        }
    }

    @Override
    public void setFont(Font font) {
        if (fontMode && queuedVertices > 0) render();
        activeFont = font;
    }

    @Override
    public void windowResize(int width, int height) {
        this.horizontalMultiplier = 2f / width;
        this.verticalMultiplier = -2f / height;
    }


    @Override
    public void destroy() {
        uiBuffer.destroy();
        textBuffer.destroy();
    }
}
