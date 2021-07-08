package xyz.chromabeam.component;

import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.chromabeam.engine.render.texture.TextureAtlas;
import xyz.chromabeam.engine.render.texture.TextureRegionI;

import java.lang.reflect.InvocationTargetException;

/**
 * The basic interface for ChromaBeam's components. Note that components MUST be position and rotation-blind, as all computations
 * MUST be position-irrelevant by design. This means that you should NEVER have a way to retrieve information using
 * world coordinates inside components.
 * Note: You should also implement one or more of BeamProducer, BeamConsumer or InstantBeamManipulator for functionality.
 * Without any of those, the component cannot work with beams. (This is behaviour is only used for wall-like components).
 */
public abstract class Component implements ComponentI{
    private static final int MASK_TICKABLE = 1;
    private static final int MASK_CONSUMER = 2;
    private static final int MASK_PRODUCER = 4;
    private static final int MASK_INSTANT = 8;
    private static final int MASK_INTERACTIVE = 16;

    private final int typeBitMask;
    private TextureRegionI[] textures;
    private TextureRegionI[][] colorMasks;
    private int activeTexture = 0;
    private int targetTexture = 0;
    private final String name;
    private final String id;
    private int colorMaskCount;

    //Added for fixing a potential bug with the copy() method.
    private Component() {
        throw new UnsupportedOperationException("Cannot instantiate a raw component!");
    }

    public Component(String name, String id, int colorMaskCount) {
        typeBitMask =
                ((this instanceof Tickable)                 ? MASK_TICKABLE             : 0) |
                ((this instanceof BeamConsumer)             ? MASK_CONSUMER             : 0) |
                ((this instanceof BeamProducer)             ? MASK_PRODUCER             : 0) |
                ((this instanceof BeamInstantManipulator)   ? MASK_INSTANT : 0) |
                ((this instanceof UserInteractive)          ? MASK_INTERACTIVE : 0);
        this.name = name;
        this.id = id;
        this.colorMaskCount = colorMaskCount;
    }


    public final boolean isTickable() {
        return (typeBitMask & MASK_TICKABLE) == MASK_TICKABLE;
    }

    public final boolean isConsumer() {
        return (typeBitMask & MASK_CONSUMER) == MASK_CONSUMER;
    }

    public final boolean isProducer() {
        return (typeBitMask & MASK_PRODUCER) == MASK_PRODUCER;
    }

    public final boolean isInstantManipulator() {
        return (typeBitMask & MASK_INSTANT) == MASK_INSTANT;
    }

    public final boolean isInteractive() {
        return (typeBitMask & MASK_INTERACTIVE) == MASK_INTERACTIVE;
    }

    public void initialize(TextureAtlas atlas) {
        textures = atlas.get(id);
        colorMasks = new TextureRegionI[colorMaskCount][];
        for (int i = 0; i < colorMaskCount; i++) {
            colorMasks[i] = atlas.get(id + "$colormask_" + i);
        }
    }

    protected void setActiveTexture(int frame) {
        targetTexture = frame;
    }

    /**
     * Deep-copies the component's data into another component of identical type.
     */
    public void copy(Component other) {
        if (!(other.getClass().equals(this.getClass()))) throw new IllegalArgumentException("Cannot copy component data into component with different type!");
        other.textures = textures;
        other.activeTexture = activeTexture;
        other.targetTexture = targetTexture;
        other.colorMaskCount = colorMaskCount;
        other.colorMasks = colorMasks;
    }

    /**
     * Creates an uninitialized component of the exact same type
     */
    public final Component newInstance() {
        try {
            return getClass().getConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final boolean isGraphicsChanged() {
        return activeTexture != targetTexture;
    }

    @Override
    public final void updateGraphics() {
        activeTexture = targetTexture;
    }

    @Override
    public final TextureRegionI getTexture() {
        return textures[activeTexture];
    }

    @Override
    public TextureRegionI getColorMaskTexture(int mask) {
        return colorMasks[mask][activeTexture];
    }

    @Override
    public Vector4f getColorMaskColor(int mask, Vector4f buffer) {
        return buffer.set(0);
    }

    @Override
    public int getColorMaskCount() {
        return colorMaskCount;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public String getID() {
        return id;
    }
}
