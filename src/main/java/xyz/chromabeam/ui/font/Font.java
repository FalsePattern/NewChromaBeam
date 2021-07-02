package xyz.chromabeam.ui.font;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import xyz.chromabeam.engine.render.texture.Texture;
import xyz.chromabeam.engine.render.texture.TextureRegion;
import xyz.chromabeam.engine.render.texture.TextureRegionI;
import xyz.chromabeam.util.Destroyable;
import xyz.chromabeam.util.ResourceUtil;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Font implements Destroyable {

    private static final Map<Character, Integer> nullKern = Collections.emptyMap();

    public final int lineHeight;
    public final int base;
    private final Map<Character, Glyph> glyphs = new HashMap<>();
    private final Map<Character, Map<Character, Integer>> kernings = new HashMap<>();
    public final Texture texture;
    private final Map<Character, TextureRegionI> regions = new HashMap<>();

    public Glyph getGlyph(char character) {
        return glyphs.get(character);
    }

    public TextureRegionI getRegion(char character) {
        return regions.get(character);
    }

    public int getKerning(char first, char second) {
        return kernings.getOrDefault(first, nullKern).getOrDefault(second, 0);
    }


    private Font(int lineHeight, int base, BufferedImage img) {
        this.lineHeight = lineHeight;
        this.base = base;
        this.texture = new Texture(img, false);
    }

    private void setupRegions() {
        glyphs.forEach((character, glyph) -> {
            var region = new TextureRegion(texture, glyph.x(), glyph.y(), glyph.width(), glyph.height());
            regions.put(character, region);
        });
    }

    public void serialize(OutputStream outputStream) throws IOException {
        var dOut = new DataOutputStream(outputStream);
        dOut.writeInt(lineHeight);
        dOut.writeInt(base);
        serializeGlyphs(dOut);
        serializeKernings(dOut);
        dOut.flush();
    }

    private void serializeGlyphs(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(glyphs.size());
        for (var entry: glyphs.entrySet()) {
            outputStream.writeChar(entry.getKey());
            entry.getValue().serialize(outputStream);
        }
    }
    private void serializeKernings(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(kernings.size());
        for (var kerningGroup : kernings.entrySet()) {
            outputStream.writeChar(kerningGroup.getKey());
            outputStream.writeInt(kerningGroup.getValue().size());
            for (var kerning : kerningGroup.getValue().entrySet()) {
                outputStream.writeChar(kerning.getKey());
                outputStream.writeByte(kerning.getValue());
            }
        }
    }

    public static Font loadFromResource(String fontName) throws IOException {
        try (var bin = ResourceUtil.getStreamFromResource("/xyz/chromabeam/fonts/" + fontName + ".fnt")) {
            var texture = ImageIO.read(ResourceUtil.getStreamFromResource("/xyz/chromabeam/fonts/" + fontName + ".png"));
            return deserialize(bin, texture);
        }
    }

    public static Font deserialize(InputStream inputStream, BufferedImage image) throws IOException {
        var dIn = new DataInputStream(inputStream);
        var font = new Font(dIn.readInt(), dIn.readInt(), image);
        deserializeGlyphs(font, dIn);
        deserializeKernings(font, dIn);
        font.setupRegions();
        return font;
    }

    private static void deserializeGlyphs(Font font, DataInputStream inputStream) throws IOException {
        int count = inputStream.readInt();
        for (int i = 0; i < count; i++) {
            font.glyphs.put(inputStream.readChar(), Glyph.deserialize(inputStream));
        }
    }
    private static void deserializeKernings(Font font, DataInputStream inputStream) throws IOException {
        int count = inputStream.readInt();
        for (int i = 0; i < count; i++) {
            var group = new HashMap<Character, Integer>();
            font.kernings.put(inputStream.readChar(), group);
            int kerns = inputStream.readInt();
            for (int j = 0; j < kerns; j++) {
                group.put(inputStream.readChar(), (int)inputStream.readByte());
            }
        }
    }

    public static Font deserializeXML(String fontCode, BufferedImage image) throws ParserConfigurationException, IOException, SAXException {
        Document document;
        {
            var factory = DocumentBuilderFactory.newInstance();
            var builder = factory.newDocumentBuilder();
            var input = new ByteArrayInputStream(StandardCharsets.US_ASCII.encode(fontCode).array());
            document = builder.parse(input);
        }
        Font font;
        {
            var common = (Element) document.getElementsByTagName("common").item(0);
            font = new Font(Integer.parseInt(common.getAttribute("lineHeight")), Integer.parseInt(common.getAttribute("base")), image);
        }
        {
            var charsTag = (Element) document.getElementsByTagName("chars").item(0);
            int count = Integer.parseInt(charsTag.getAttribute("count"));
            var chars = charsTag.getElementsByTagName("char");
            for (int i = 0; i < count; i++) {
                var chr = (Element) chars.item(i);
                font.glyphs.put(
                        (char)Integer.parseInt(chr.getAttribute("id")),
                        new Glyph(
                                Integer.parseInt(chr.getAttribute("x")),
                                Integer.parseInt(chr.getAttribute("y")),
                                Byte.parseByte(chr.getAttribute("width")),
                                Byte.parseByte(chr.getAttribute("height")),
                                Byte.parseByte(chr.getAttribute("xoffset")),
                                Byte.parseByte(chr.getAttribute("yoffset")),
                                Byte.parseByte(chr.getAttribute("xadvance")),
                                Byte.parseByte(chr.getAttribute("page"))
                        )
                );
            }
        }
        {
            var kerningsTag = (Element) document.getElementsByTagName("kernings").item(0);
            int count = Integer.parseInt(kerningsTag.getAttribute("count"));
            var kernings = kerningsTag.getElementsByTagName("kerning");
            for (int i = 0; i < count; i++) {
                var kerning = (Element) kernings.item(i);
                font.kernings.computeIfAbsent(
                        (char)Integer.parseInt(kerning.getAttribute("first")),
                        HashMap::new
                ).put(
                        (char)Integer.parseInt(kerning.getAttribute("second")),
                        (int)Byte.parseByte(kerning.getAttribute("amount"))
                );
            }
        }
        font.setupRegions();
        return font;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Font other) {
            return glyphs.equals(other.glyphs) && kernings.equals(other.kernings);
        } else {
            return false;
        }
    }

    @Override
    public void destroy() {
        texture.destroy();
    }
}

