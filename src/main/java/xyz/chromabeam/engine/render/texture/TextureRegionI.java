package xyz.chromabeam.engine.render.texture;

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
     * @return Horizontal position of the top-left point of the tile in texture pixel coordinates.
     */
    int x();

    /**
     * @return Vertical position of the top-left point of the tile in texture pixel coordinates.
     */
    int y();

    /**
     * @return Horizontal size of the tile in texture pixel coordinates.
     */
    int width();

    /**
     * @return Vertical size of the tile in texture pixel coordinates.
     */
    int height();
}
