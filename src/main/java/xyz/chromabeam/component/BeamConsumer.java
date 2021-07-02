package xyz.chromabeam.component;

import xyz.chromabeam.beam.Direction;

/**
 * Beam consumers can detect incoming beams, however, they cannot emit beams by themselves.
 */
public interface BeamConsumer extends Tickable {
    /**
     * Handle a beam hitting the component in the specified direction with the specified color.
     * @param direction The direction of the incoming beam. Note: this is the way the beam is headed, not the face it
     *                  hit, so a beam with direction RIGHT hit the LEFT face of the component.
     */
    void incomingBeam(Direction direction, float red, float green, float blue);


}
