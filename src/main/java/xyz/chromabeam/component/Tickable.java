package xyz.chromabeam.component;

/**
 * Tickables are components that do specific operations non-instantly, such as gates and delayers.
 */
public interface Tickable extends ComponentI {
    /**
     * Executes 1 time unit of simulation in the component.
     */
    void tick();
}
