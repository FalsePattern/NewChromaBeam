package xyz.chromabeam.component;

import xyz.chromabeam.world.BeamEmitter;

/**
 * Beam producers can emit beams, however, they cannot process incoming beams by themselves.
 */
public interface BeamProducer extends ComponentI {

    /**
     * Ticked components MUST create their output beams here!
     */
    void emitBeams(BeamEmitter beamEmitter);
}
