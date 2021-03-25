package moe.falsepattern.engine.render.texture;

import moe.falsepattern.engine.Constants;
import moe.falsepattern.util.FPMath;
import org.joml.Math;
import org.joml.Vector2i;

import java.awt.AlphaComposite;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingDeque;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class TextureAtlas implements TextureI {
    private Texture texture;
    private final HashMap<String, List<TextureRegion>> textures;
    public TextureAtlas(List<TextureTile> tiles) {
        textures = new HashMap<>();
        tiles.sort(Comparator.reverseOrder());
        var finalSize = pack(tiles);
        var result = new BufferedImage(finalSize.width, finalSize.height, TYPE_INT_ARGB);
        var gfx = result.createGraphics();
        gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        gfx.fillRect(0,0,result.getWidth(),result.getHeight());
        gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        for (var tile : tiles) {
            gfx.drawImage(tile.texture, tile.textureGeometry.x, tile.textureGeometry.y, null);
        }
        texture = new Texture(result, true);
        for (var tile: tiles) {
            var frameList = textures.computeIfAbsent(tile.textureName, (ignored) -> new ArrayList<>());
            var geom = tile.textureGeometry;
            while (frameList.size() <= tile.textureFrame) {
                frameList.add(null);
            }
            frameList.set(tile.textureFrame, new TextureRegion(texture, geom.x, geom.y, geom.width, geom.height));
        }
    }

    public TextureRegion getTexture(String name, int frame) {
        var frames = textures.getOrDefault(name, null);
        return frames != null && frames.size() >= frame ? frames.get(frame) : null;
    }

    private Rectangle pack(List<TextureTile> tiles) {
        boolean notFound = true;
        boolean widthDouble = true;
        int gap = Constants.TEXTURE_GAP;
        int width = 64;
        int height = 64;
        while (notFound) {
            if (widthDouble) width *= 2; else height *= 2; widthDouble = !widthDouble;
            var partitions = new LinkedBlockingDeque<Rectangle>();
            partitions.push(new Rectangle(0, 0, width, height));
            outer:
            for (var tile: tiles) {
                while (partitions.size() > 0) {
                    var partition = partitions.pop();
                    var geom = tile.textureGeometry;
                    if (geom.width + gap <= partition.width && geom.height + gap <= partition.height) {
                        geom.x = partition.x;
                        geom.y = partition.y;
                        int right = geom.x + geom.width + gap;
                        int bottom = geom.y + geom.height + gap;
                        partitions.push(new Rectangle(geom.x, bottom, partition.width, partition.height - geom.height - gap));
                        partitions.push(new Rectangle(right, geom.y, partition.width - geom.width - gap, geom.height + gap));
                        continue outer;
                    }
                }
                break;
            }
            notFound = partitions.size() == 0;
        }
        return new Rectangle(0, 0, width, height);
    }

    @Override
    public float u0() {
        return 0;
    }

    @Override
    public float v0() {
        return 0;
    }

    @Override
    public float u1() {
        return 1;
    }

    @Override
    public float v1() {
        return 1;
    }

    @Override
    public int x() {
        return 0;
    }

    @Override
    public int y() {
        return 0;
    }

    @Override
    public int width() {
        return texture.width();
    }

    @Override
    public int height() {
        return texture.height();
    }

    public void destroy() {
        texture.destroy();
    }

    @Override
    public void bind() {
        texture.bind();
    }

    @Override
    public void unbind() {
        texture.unbind();
    }
}
