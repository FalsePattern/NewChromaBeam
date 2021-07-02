package xyz.chromabeam.demo;

import org.lwjgl.opengl.GL33C;
import org.xml.sax.SAXException;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.engine.InputDispatcher;
import xyz.chromabeam.engine.render.chunk.RenderChunk;
import xyz.chromabeam.ui.Button;
import xyz.chromabeam.ui.UIManager;
import xyz.chromabeam.ui.font.Font;
import xyz.chromabeam.world.FlatWorld2D;
import xyz.chromabeam.world.InteractionManager;
import xyz.chromabeam.component.Component;
import xyz.chromabeam.demo.components.basic.*;
import xyz.chromabeam.engine.render.Camera;
import xyz.chromabeam.engine.render.Shader;
import xyz.chromabeam.engine.render.beam.BeamRenderer;
import xyz.chromabeam.engine.render.beam.DeferredRenderer;
import xyz.chromabeam.engine.render.chunk.ChunkRenderer;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import xyz.chromabeam.engine.render.texture.TextureTile;
import xyz.chromabeam.engine.render.world.Renderer;
import xyz.chromabeam.engine.window.Window;
import xyz.chromabeam.ui.ScreenSpaceUIDrawer;
import xyz.chromabeam.util.ResourceUtil;
import xyz.chromabeam.world.World2D;
import xyz.chromabeam.world.WorldRenderer;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Demo {
    public static void main(String[] args) throws IOException {
        //Thread.sleep(10000 );
        final var closed = new AtomicBoolean(false);
        final var window = new Window(800, 600, "ChromaBeam Dev demo 0.0.1", () -> closed.set(true));
        final var atlas = new TextureAtlas(getTextures());

        final var componentRenderer = new ChunkRenderer(RenderChunk.CHUNK_SIDE_LENGTH);
        final var beamRenderer = new BeamRenderer();
        final var blurRenderer = new DeferredRenderer(window.getWidth(), window.getHeight(), beamRenderer, "pos2uv", "beamQuad");
        final var flatShader = Shader.fromShaderResource("ui", "color4");
        final var fontShader = Shader.fromShaderResource("pos2uv", "uv");
        final var uiRenderer = new ScreenSpaceUIDrawer(window.getWidth(), window.getHeight(), flatShader, fontShader);
        final var font = Font.loadFromResource("Roboto32");
        blurRenderer.clearColor(0.2f, 0.2f, 0.2f, 1.0f);
        uiRenderer.setFont(font);

        final var camera = new Camera();
        componentRenderer.setCamera(camera);
        beamRenderer.setCamera(camera);
        blurRenderer.setCamera(camera);
        window.addResizeCallback((w, h) -> {
            camera.setViewport(0, 0, w, h);
            GL33C.glViewport(0, 0, w, h);
        });
        camera.setZoom(32f);

        final World2D world = new FlatWorld2D(new WorldRenderer((x, y) -> {
            var c = componentRenderer.allocateChunk();
            c.x = x;
            c.y = y;
            return c;
        }), beamRenderer);
        try (window; atlas; flatShader; fontShader; font; uiRenderer; componentRenderer; beamRenderer; blurRenderer) {
            final var components = new Component[]{new Block(), new Emitter(), new Gate(), new Mirror(), new Splitter(), new Delayer()};
            for (var component : components) {
                component.initialize(atlas);
            }
            final var intMan = new InteractionManager( world, camera, components, 0);
            final var uiMan = new UIManager(window.getWidth(), window.getHeight(), 1);

            var box = new Button(100, 100, 100, 100, Color.RED, Color.GREEN, Color.BLUE);
            uiMan.addChild(box);

            final var inputDispatcher = new InputDispatcher(window.keyboard, window.mouse);

            inputDispatcher.registerInputHandler(intMan);
            inputDispatcher.registerInputHandler(uiMan);

            window.addResizeCallback(blurRenderer);
            window.addResizeCallback(uiRenderer);
            window.addResizeCallback(uiMan);
            window.vSync(0);
            window.show();
            int ticks = 0;
            double average = 0;
            var r = new Random(1);

            for (int y = -256; y < 255; y++) {
                for (int x = -256; x < 255; x++) {
                    if (r.nextInt(2) == 0) {
                        var c = components[1].newInstance();
                        components[1].copy(c);
                        world.set(x, y, Direction.values()[r.nextInt(4)], r.nextBoolean(), c);
                    }
                }
            }
            while (!closed.get()) {
                Window.pollEvents();
                var start = System.nanoTime();
                world.update();
                var end = System.nanoTime();
                average += (end - start) / 100000d;
                ticks++;
                inputDispatcher.processInput();
                Renderer.clear(0, 0, 0, 1);
                blurRenderer.render();
                atlas.bind();
                componentRenderer.render();
                atlas.unbind();
                //uiMan.draw(uiRenderer);
                //uiRenderer.drawText(200, 200, "It's text!");
                //uiRenderer.render();
                window.swap();
                if (ticks % 100 == 0) {
                    System.out.printf("Average tick duration in last 100 ticks: %.3f us\n", average);
                    average = 0;
                    ticks = 0;
                }
            }
        }
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
