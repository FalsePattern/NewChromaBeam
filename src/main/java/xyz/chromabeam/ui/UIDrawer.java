package xyz.chromabeam.ui;

import xyz.chromabeam.ui.font.Font;

public interface UIDrawer {
    void drawFilledRect(RectI2D rect, float red, float green, float blue, float alpha);
    RectI2D getTextRect(int x, int y, String text, RectI2D buffer);
    void drawText(int x, int y, String text);
    void setFont(Font font);
}
