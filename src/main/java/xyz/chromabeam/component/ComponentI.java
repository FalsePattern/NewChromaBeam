package xyz.chromabeam.component;

import xyz.chromabeam.engine.render.texture.TextureRegionI;

/**
 * INTERNAL USE ONLY, DO NOT IMPLEMENT THIS!!!!!!!! USE {@link Component} INSTEAD!
 */
public interface ComponentI {
    boolean isTickable();

    boolean isConsumer();

    boolean isProducer();

    boolean isInstantManipulator();

    boolean isInteractive();

    /**
     * True if and only if the component's graphics have been modified relative to the previous tick's state.
     */
    boolean isGraphicsChanged();

    /**
     * The graphics of the component may only be changed when this function is called.
     */
    void updateGraphics();

    /**
     * The texture of the component during the current tick. This may only change when {@link #updateGraphics} is called,
     * otherwise it will cause graphical de-synchronisation.
     */
    TextureRegionI getTexture();

    /**
     * The human-readable name of this component.
     */
    String getName();
}
