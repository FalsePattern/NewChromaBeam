package xyz.chromabeam.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Button extends UIRectangle {
    private final Color base;
    private final Color hover;
    private final Color click;
    private boolean pressed = false;
    private boolean hovered = false;

    private final List<Runnable> onClick = new ArrayList<>();

    public Button(int x, int y, int w, int h, Color baseColor, Color hoverColor, Color clickColor) {
        super(x, y, w, h, false);
        this.base = baseColor;
        this.hover = hoverColor;
        this.click = clickColor;
    }

    @Override
    public void mouseEnter(int x, int y) {
        hovered = true;
    }

    @Override
    public void mouseLeave(int x, int y) {
        hovered = false;
    }

    @Override
    public void mousePress(int x, int y) {
        pressed = true;
    }

    @Override
    public void mouseRelease(int x, int y) {
        if (pressed && hovered) {
            onClick.forEach(Runnable::run);
        }
        pressed = false;
    }

    public void onClick(Runnable runnable) {
        onClick.add(runnable);
    }

    @Override
    public void draw(UIDrawer drawer) {
        var color = pressed ? click : hovered ? hover : base;
        var buf = RectI2D.getBuffer();
        drawer.drawFilledRect(getAbsoluteRect(buf), color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
    }
}
