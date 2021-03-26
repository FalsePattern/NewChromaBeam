package moe.falsepattern.engine.render.texture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class TextureTile implements Comparable<TextureTile> {

    final BufferedImage texture;
    final String textureName;
    final int textureFrame;
    final Rectangle textureGeometry;

    public TextureTile(BufferedImage texture, String textureName, int textureFrame) {
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
        if (obj instanceof TextureTile tex) {
            return Objects.equals(texture, tex.texture) && Objects.equals(textureName, tex.textureName)
                    && Objects.equals(textureFrame, tex.textureFrame) && Objects.equals(textureGeometry, tex.textureGeometry);
        } else {
            return false;
        }
    }
}
