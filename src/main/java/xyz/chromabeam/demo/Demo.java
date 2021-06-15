package xyz.chromabeam.demo;

import org.lwjgl.opengl.GL33C;
import xyz.chromabeam.InteractionManager;
import xyz.chromabeam.component.BeamConsumer;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.demo.components.basic.*;
import xyz.chromabeam.engine.render.Camera;
import xyz.chromabeam.engine.render.beam.BeamRenderer;
import xyz.chromabeam.engine.render.beam.DeferredRenderer;
import xyz.chromabeam.engine.render.chunk.ChunkRenderer;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import xyz.chromabeam.engine.render.texture.TextureTile;
import xyz.chromabeam.engine.render.world.Renderer;
import xyz.chromabeam.engine.window.Window;
import xyz.chromabeam.util.ResourceUtil;
import xyz.chromabeam.world.World2D;
import xyz.chromabeam.world.WorldChunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class Demo {
    public static void main(String[] args) {
        final var closed = new AtomicBoolean(false);
        final var window = new Window(800, 600, "ChromaBeam Dev demo 0.0.1", () -> closed.set(true));
        final var atlas = new TextureAtlas(getTextures());

        final var componentRenderer = new ChunkRenderer(WorldChunk.CHUNK_SIDE_LENGTH);
        final var beamRenderer = new BeamRenderer();
        final var blurRenderer = new DeferredRenderer(window.getWidth(), window.getHeight(), beamRenderer, "beamQuad");
        blurRenderer.clearColor(0.2f, 0.2f, 0.2f, 1.0f);

        window.addResizeCallback(blurRenderer);
        window.vSync(1);
        final var camera = new Camera();
        componentRenderer.setCamera(camera);
        beamRenderer.setCamera(camera);
        blurRenderer.setCamera(camera);
        window.addResizeCallback((w, h) -> {
            camera.setViewport(0, 0, w, h);
            GL33C.glViewport(0, 0, w, h);
        });

        final var world = new World2D(componentRenderer, beamRenderer);
        final var components = new Component[]{new Block(), new Emitter(), new Gate(), new Mirror(), new Splitter(), new Delayer()};
        for (var component : components) {
            component.initialize(atlas);
        }
        final var intMan = new InteractionManager(window.keyboard, window.mouse, world, camera, components);
        camera.setZoom(32f);
        window.show();
        while (!closed.get()) {
            Window.pollEvents();
            intMan.handleInput();
            world.update();
            Renderer.clear(0, 0, 0, 1);
            blurRenderer.render();
            atlas.bind();
            componentRenderer.render();
            atlas.unbind();
            window.swap();
        }
        world.destroy();
        blurRenderer.destroy();
        beamRenderer.destroy();
        componentRenderer.destroy();
        atlas.destroy();
        window.destroy();
    }

    private static final String textureRoot = "/xyz/chromabeam/textures/demo/";
    private static final String[] textures = new String[]{"block", "emitter", "gate", "mirror", "delayer", "splitter"};
    private static ArrayList<TextureTile> getTextures() {
        return new ArrayList<>(Arrays
                .stream(textures)
                .<TextureTile>mapMulti((texture, consumer) -> {
                    try {
                        TextureTile.splitIntoTiles(ResourceUtil.readImageFromResource(textureRoot + texture + ".png"), 32, 32, texture).forEach(consumer);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList());
    }
}
