package xyz.chromabeam.engine.render.world;

import xyz.chromabeam.engine.render.Camera;

import static org.lwjgl.opengl.GL11C.*;

public abstract class Renderer {
    protected Camera camera;

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public abstract void render();


    public static void clear(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
        glClear(GL_COLOR_BUFFER_BIT);
    }
}
