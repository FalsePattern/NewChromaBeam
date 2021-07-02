package xyz.chromabeam.component;

import xyz.chromabeam.beam.Direction;
import xyz.chromabeam.world.BeamEmitter;

/**
 * Instant manipulators are like the wires of ChromaBeam. These include mirrors, beam splitters, color filter screens, and so on.
 */
public interface BeamInstantManipulator extends ComponentI {

    /**
     * Handle a beam hitting the component in the specified direction with the specified color.
     * @param direction The direction of the incoming beam. Note: this is the way the beam is headed, not the face it
     *                  hit, so a beam with direction RIGHT hit the LEFT face of the component, and so on...
     * @param instantOutput !!!WARNING!!! You should only use instant components if you are 100% sure that it cannot cause
     *                      uncontrolled beam loops! Otherwise it can cause the game to hang.
     */
    void incomingBeam(Direction direction, float red, float green, float blue, BeamEmitter instantOutput);

}
