package moe.falsepattern.engine.render.texture;

public interface TextureRegionI {

    /**
     * @return Left edge of the tile in texture UV coordinates.
     */
    float u0();

    /**
     * @return Top edge of the tile in texture UV coordinates.
     */
    float v0();

    /**
     * @return Right edge of the tile in texture UV coordinates.
     */
    float u1();

    /**
     * @return Bottom edge of the tile in texture UV coordinates.
     */
    float v1();

    /**
     * @return Horizonal position of the top-left point of the tile in texture pixel coordinates.
     */
    int x();

    /**
     * @return Vertical position of the top-left point of the tile in texture pixel coordinates.
     */
    int y();

    /**
     * @return Horizonal size of the tile in texture pixel coordinates.
     */
    int width();

    /**
     * @return Vertical size of the tile in texture pixel coordinates.
     */
    int height();

    /**
     * Attach the underlying OpenGL texture to the render pipeline. This overwrites previously attached textures as per
     * opengl specifications.
     */
    void bind();

    /**
     * Detach the underlying OpenGL texture to the render pipeline. This will detach any texture, regarless of whether
     * it's related to this tile or not.
     */
    void unbind();
}
