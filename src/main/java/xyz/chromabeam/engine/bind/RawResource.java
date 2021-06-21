package xyz.chromabeam.engine.bind;

import xyz.chromabeam.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RawResource {
    private final String resourceName;
    private final Supplier<Integer> generator;
    private final Consumer<Integer> deleter;
    private final Consumer<Integer> binder;

    private final List<Integer> managedResources;
    private final Map<Integer, Exception> resourceCreation;
    private int boundResource;
    private Exception lastResourceBind;

    RawResource(String resourceName, Supplier<Integer> generator, Consumer<Integer> deleter, Consumer<Integer> binder) {
        if (!Global.DEBUG) throw new IllegalStateException("Could not create debug resource manager in non-debug context!");
        this.resourceName = resourceName;
        this.generator = generator;
        this.deleter = deleter;
        this.binder = binder;
        managedResources = new ArrayList<>();
        resourceCreation = new HashMap<>();

    }

    int genResource() {
        int resource = generator.get();
        managedResources.add(resource);
        resourceCreation.put(resource, new Exception(resourceName + " with id " + resource + " created here:"));
        return resource;
    }

    void deleteResource(int resource) {
        if (resource == 0) throw new IllegalArgumentException("Tried to delete " + resourceName + " with a null pointer!");
        if (!managedResources.contains(resource)) throw new IllegalArgumentException("Tried to delete unmanaged " + resourceName + "!");
        if (boundResource == resource) throw new IllegalStateException("Tried to delete bound " + resourceName + "!");
        managedResources.remove(Integer.valueOf(resource));
        resourceCreation.remove(resource);
        deleter.accept(resource);
    }

    void bindResource(int resource) {
        if (resource == 0) throw new IllegalArgumentException("Tried to bind " + resourceName + " with a null pointer!");
        if (!managedResources.contains(resource)) throw new IllegalArgumentException("Tried to bind unmanaged " + resourceName + "!");
        if (boundResource != 0) throw new IllegalStateException("Tried to bind " + resourceName + " when one was already bound!", lastResourceBind);
        lastResourceBind = new Exception("Last bind location stacktrace:");
        boundResource = resource;
        binder.accept(resource);
    }

    void unbindResource(int resource) {
        if (resource == 0) throw new IllegalArgumentException("Tried to unbind " + resourceName + " with a null pointer!");
        if (!managedResources.contains(resource)) throw new IllegalArgumentException("Tried to unbind unmanaged " + resourceName + "!");
        if (boundResource == 0) throw new IllegalStateException("Tried to unbind " + resourceName + " when none was bound!", lastResourceBind);
        if (boundResource != resource) throw new IllegalArgumentException("Tried to unbind " + resourceName + " when a different one was bound!", lastResourceBind);
        lastResourceBind = new Exception("Last unbind location stacktrace:");
        boundResource = 0;
        binder.accept(0);
    }

    void collectNotDeletedExceptions(Consumer<Exception> accumulator) {
        managedResources.forEach((resource) -> accumulator.accept(resourceCreation.get(resource)));
    }

    int bound() {
        return boundResource;
    }
}
