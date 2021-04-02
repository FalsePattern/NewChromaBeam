package xyz.chromabeam.engine;

/**
 * A class for uniting bind/unbind calls for external resources.
 */
public interface Bindable {
    /**
     * Binds the external content contained an object.
     */
    void bind();

    /**
     * Unbinds the external content currently bound.
     */
    void unbind();

    static void runWith(Runnable runnable, Bindable... bindables) {
        for (var bindable: bindables) {
            bindable.bind();
        }
        runnable.run();
        for (var bindable: bindables) {
            bindable.unbind();
        }
    }
}
