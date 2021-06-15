package xyz.chromabeam.engine.render.texture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Used by {@link TextureAtlas} for sorting incoming textures into the atlas image. Contains a texture as a
 * {@link BufferedImage}, it's canonical name, it's frame id (used for animated textures), and it's position+dimensions,
 * the last of which will be modified by the {@link TextureAtlas} during sorting to reflect it's final position in the
 * atlas. This should only be used for passing textures into the {@link TextureAtlas}' constructor.
 */
public class TextureTile implements Comparable<TextureTile> {

    final BufferedImage texture;
    final String textureName;
    final int textureFrame;
    final Rectangle textureGeometry;

    public static List<TextureTile> splitIntoTiles(BufferedImage texture, int tileWidth, int tileHeight, String name) {
        if (texture.getWidth() % tileWidth != 0 || texture.getHeight() % tileHeight != 0) {
            throw new IllegalArgumentException("Cannot split texture " + name + " into " + tileWidth + "x" + tileHeight + "-sized tiles: texture size is not a multiple of tile size!");
        }
        var output = new ArrayList<TextureTile>();
        int nX = texture.getWidth() / tileWidth;
        int nY = texture.getHeight() / tileHeight;
        for (int y = 0; y < nY; y++) {
            for (int x = 0; x < nX; x++) {
                var subregion = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_ARGB);
                for (int tY = 0; tY < tileHeight; tY++) {
                    for (int tX = 0; tX < tileWidth; tX++) {
                        subregion.setRGB(tX, tY, texture.getRGB(x * tileWidth + tX, y * tileHeight + tY));
                    }
                }
                output.add(new TextureTile(subregion, name, y * nX + x));
            }
        }
        return output;
    }

    public TextureTile(BufferedImage texture, String textureName) {
        this(texture, textureName, 0);
    }

    private TextureTile(BufferedImage texture, String textureName, int textureFrame) {
        this.texture = texture;
        this.textureName = textureName;
        this.textureFrame = textureFrame;
        this.textureGeometry = new Rectangle(0, 0, texture.getWidth(), texture.getHeight());
    }

    @Override
    public int compareTo(TextureTile o) {
        if (textureGeometry.height == o.textureGeometry.height) {
            return textureGeometry.width - o.textureGeometry.width;
        } else {
            return textureGeometry.height - o.textureGeometry.height;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextureTile) {
            @SuppressWarnings("PatternVariableCanBeUsed") //Manifold cannot compile if we use this feature :cry:
            var tex = (TextureTile) obj;
            return Objects.equals(texture, tex.texture) && Objects.equals(textureName, tex.textureName)
                    && Objects.equals(textureFrame, tex.textureFrame) && Objects.equals(textureGeometry, tex.textureGeometry);
        } else {
            return false;
        }
    }
}
