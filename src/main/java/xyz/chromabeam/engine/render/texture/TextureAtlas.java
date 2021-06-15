package xyz.chromabeam.engine.render.texture;

import xyz.chromabeam.engine.Constants;

import java.awt.AlphaComposite;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * A large texture containing multiple independent smaller textures. Used for improving render performance by A LOT.
 * By default, the game uses 2 atlases: a World atlas with components and related graphics, and a GUI atlas for text,
 * buttons, and other interface related stuff.
 */
public class TextureAtlas extends Texture {

    private final HashMap<String, TextureRegion[]> textures;
    public TextureAtlas(ArrayList<TextureTile> tiles) {
        super(generateAtlasImage(tiles), true);
        textures = new HashMap<>();
        var tmpTex = new HashMap<String, List<TextureRegion>>();
        for (var tile: tiles) {
            var frameList = tmpTex.computeIfAbsent(tile.textureName, (ignored) -> new ArrayList<>());
            var geom = tile.textureGeometry;
            while (frameList.size() <= tile.textureFrame) {
                frameList.add(null);
            }
            frameList.set(tile.textureFrame, new TextureRegion(this, geom.x, geom.y, geom.width, geom.height));
        }
        tmpTex.forEach((key, value) -> textures.put(key, value.toArray(TextureRegion[]::new)));
    }

    /**
     * Retrieves a texture with a given name, and at specific frame, if it's animated.
     * @param name The name of the texture that needs to be retrieved
     * @param frame The frame of the animation inside the texture to retrieve. If it's not animated, this should be 0.
     * @return The requested texture frame, or null, if it doesn't exist.
     */
    public TextureRegion getFrame(String name, int frame) {
        if (frame < 0) return null;
        var frames = textures.getOrDefault(name, null);
        return frames != null && frames.length > frame ? frames[frame] : null;
    }

    public int getFrameCount(String name) {
        var frames = textures.getOrDefault(name, null);
        if (frames == null) return 0;
        return frames.length;
    }

    public boolean hasTexture(String name) {
        return Objects.nonNull(textures.getOrDefault(name, null));
    }

    public TextureRegion[] get(String name) {
        return textures.getOrDefault(name, null);
    }

    //Helper function for putting packed tiles into a texture atlas image.
    private static BufferedImage generateAtlasImage(List<TextureTile> tiles) {
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
        return result;
    }

    //I'm not proud of this one, but it works, so whatever.
    private static Rectangle pack(List<TextureTile> tiles) {
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
}
