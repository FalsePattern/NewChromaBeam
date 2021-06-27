package xyz.chromabeam.demo;

import org.lwjgl.opengl.GL33C;
import org.xml.sax.SAXException;
import xyz.chromabeam.Global;
import xyz.chromabeam.engine.bind.BindManager;
import xyz.chromabeam.engine.InputDispatcher;
import xyz.chromabeam.ui.Button;
import xyz.chromabeam.ui.UIManager;
import xyz.chromabeam.ui.font.Font;
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
import xyz.chromabeam.world.WorldChunk;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class Demo {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        final var closed = new AtomicBoolean(false);
        final var window = new Window(800, 600, "ChromaBeam Dev demo 0.0.1", () -> closed.set(true));
        final var atlas = new TextureAtlas(getTextures());

        final var componentRenderer = new ChunkRenderer(WorldChunk.CHUNK_SIDE_LENGTH);
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

        final var world = new World2D(componentRenderer, beamRenderer);
        try (window; atlas; world; flatShader; fontShader; font; uiRenderer; componentRenderer; beamRenderer; blurRenderer) {
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
            window.vSync(1);
            window.show();
            while (!closed.get()) {
                Window.pollEvents();
                world.update();
                inputDispatcher.processInput();
                Renderer.clear(0, 0, 0, 1);
                blurRenderer.render();
                atlas.bind();
                componentRenderer.render();
                atlas.unbind();
                uiMan.draw(uiRenderer);
                uiRenderer.drawText(200, 200, "It's text!");
                uiRenderer.render();
                window.swap();
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
