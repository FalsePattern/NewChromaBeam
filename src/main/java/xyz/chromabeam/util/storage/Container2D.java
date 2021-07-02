package xyz.chromabeam.util.storage;

import org.joml.Vector2i;
import xyz.chromabeam.beam.Direction;

import java.util.List;
import java.util.function.Supplier;

/**
 * A 2-dimensional integer-indexed datastructure allowing both positive and negative indices.
 */
public interface Container2D<T> extends Iterable<T> {

    /**
     * @return The amount of non-null objects contained.
     */
    long getElementCount();

    /**
     * @return The element at the specified coordinates, or null if the cell is empty
     */
    T get(int x, int y);


    /**
     * Reads a specific cell, and if it's null, then sets it to the computed value.
     * @return The value at the specified location, or the computed value if empty.
     */
    default T getOrCompute(int x, int y, Supplier<T> computeIfEmpty) {
        var result = get(x, y);
        if (result == null) {
            result = computeIfEmpty.get();
            set(x, y, result);
        }
        return result;
    }

    /**
     * Reads a specific cell, and if it's null, returns the default value.
     * @return The value at the specified location, or the default value if empty.
     */
    default T getOrDefault(int x, int y, T defaultValue) {
        var result = get(x, y);
        return result == null ? defaultValue : result;
    }

    /**
     * Replaces the element at the specified coordinates with the specified element.
     * @return The old element, or null if the cell is empty
     */
    T set(int x, int y, T data);

    /**
     * Replaces the cell at the specified location with null
     * @return The old contents of the cell
     */
    T remove(int x, int y);

    /**
     * @return True if the specified cell is empty
     */
    boolean isEmpty(int x, int y);

    /**
     * Sets every cell to null.
     */
    void clear();

    /**
     * @return All elements in this container, which are non-null
     */
    List<T> getNonNullUnordered();

    //alternatives for ease of use

    /**
     * @return The element at the specified coordinates, or null if the cell is empty
     */
    default T get(Vector2i position) {
        return get(position.x, position.y);
    }

    /**
     * Replaces the element at the specified coordinates with the specified element.
     * @return The old element, or null if the cell is empty
     */
    default T set(Vector2i position, T data) {
        return set(position.x, position.y, data);
    }

    /**
     * Replaces the cell at the specified location with null
     * @return The old contents of the cell
     */
    default T remove(Vector2i position) {
        return remove(position.x, position.y);
    }

    /**
     * @return True if the specified cell is empty
     */
    default boolean isEmpty(Vector2i position) {
        return isEmpty(position.x, position.y);
    }

    boolean isEmptyRight(int x, int y);

    boolean isEmptyDown(int x, int y);

    boolean isEmptyLeft(int x, int y);

    boolean isEmptyUp(int x, int y);

    default boolean isEmptyDir(int x, int y, Direction direction) {
        return switch (direction) {
            case RIGHT -> isEmptyRight(x, y);
            case DOWN -> isEmptyDown(x, y);
            case LEFT -> isEmptyLeft(x, y);
            case UP -> isEmptyUp(x, y);
        };
    }

    boolean getRight(int x, int y, Vector2i buffer);

    boolean getDown(int x, int y, Vector2i buffer);

    boolean getLeft(int x, int y, Vector2i buffer);

    boolean getUp(int x, int y, Vector2i buffer);

    default boolean getDir(int x, int y, Direction direction, Vector2i buffer) {
        return switch (direction){
            case RIGHT -> getRight(x, y, buffer);
            case DOWN -> getDown(x, y, buffer);
            case LEFT -> getLeft(x, y, buffer);
            case UP -> getUp(x, y, buffer);
        };
    }
}
