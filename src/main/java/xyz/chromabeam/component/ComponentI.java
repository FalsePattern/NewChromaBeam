package xyz.chromabeam.component;

import manifold.ext.rt.api.Self;
import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.world.BeamEmitter;
import xyz.chromabeam.engine.render.texture.TextureRegionI;

/**
 * The basic interface for ChromaBeam's components. Note that components MUST be position-blind, as all computations
 * MUST be position-irrelevant by design. This means that you should NEVER have a way to retrieve information using
 * world coordinates inside components.
 */
public interface ComponentI {
    /**
     * Handle a beam hitting the component in the specified direction with the specified color.
     * @param direction The direction of the incoming beam. Note: this is the way the beam is headed, not the face it
     *                  hit, so a beam with direction RIGHT hit the LEFT face of the component.
     */
    void incomingBeam(Direction direction, float red, float green, float blue);

    /**
     * Executes 1 time unit of simulation in the component. Components MUST do graphics and output changes here.
     */
    void tick(BeamEmitter beamEmitter);

    /**
     * Creates an identical copy of the component, but with each internal value deep-copied to be fully independent
     * of the original component. After copy, the two objects MUST have no relations to each other, otherwise it will
     * result in undefined behaviour during simulation.
     * @return The copy of the component
     */
    @Self ComponentI copy();

    /**
     * Returns true if and only if the component's graphics have been modified relative to the previous tick's state.
     * @return True if the component's graphics changed, false otherwise
     */
    boolean graphicsChanged();

    /**
     * Gets the texture of the component during the current tick. This may only change when {@link #tick} is called,
     * otherwise it will cause graphical de-synchronisation.
     * @return The current texture of the component.
     */
    TextureRegionI getTexture();
}
