package xyz.chromabeam.component;

import xyz.chromabeam.world.BeamEmitter;

/**
 * Beam producers can emit beams, however, they cannot process incoming beams by themselves.
 */
public interface BeamProducer extends Tickable {

    /**
     * Ticked components MUST create their output beams here! However, a call of this function MUST NOT change the component's state.
     * Therefore, if a component is a BeamProducer, but not a Tickable, it MUST only ever emit the same beam.
     */
    void emitBeams(BeamEmitter beamEmitter);

    /**
     * @return True if the component wants to modify it's emitted beams after being ticked, false if the component's output beams didn't change.
     */
    boolean wantEmit();

}
