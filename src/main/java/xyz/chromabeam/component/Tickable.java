package xyz.chromabeam.component;

import xyz.chromabeam.world.BeamEmitter;

/**
 * Tickables are time-aware components. These are gates, delayers, and other non-instant components.
 */
public interface Tickable extends ComponentI {
    /**
     * Executes 1 time unit of simulation in the component.
     */
    void tick();
}
