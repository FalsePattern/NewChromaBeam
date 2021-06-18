package xyz.chromabeam.engine;

/**
 * A class for uniting bind/unbind calls for external resources.
 */
public interface Bindable {
    /**
     * Binds the external content contained in the object.
     */
    void bind();

    /**
     * Unbinds the external content currently bound.
     */
    void unbind();
}
