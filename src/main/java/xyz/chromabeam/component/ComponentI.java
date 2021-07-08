package xyz.chromabeam.component;

import org.joml.Vector3f;
import org.joml.Vector4f;
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
     * The base texture of the component during the current tick. This may only change when {@link #updateGraphics} is called,
     * otherwise it will cause graphical de-synchronisation.
     */
    TextureRegionI getTexture();

    /**
     * A specific color mask of the component. Usually, these are synchronized to the component's state.
     */
    TextureRegionI getColorMaskTexture(int mask);

    /**
     * Get the color of the specified color mask, and stores it inside the buffer.
     * @return The passed in buffer for chaining.
     */
    Vector4f getColorMaskColor(int mask, Vector4f buffer);

    /**
     * The amount of color masks this component has. This must never change once initialized.
     */
    int getColorMaskCount();


    /**
     * The human-readable name of this component.
     */
    String getName();

    /**
     * The computer-readable identifier of this component.
     */
    String getID();
}
