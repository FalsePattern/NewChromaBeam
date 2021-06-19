package xyz.chromabeam.ui.font;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record Glyph(int x, int y, byte width, byte height, byte xOffset, byte yOffset, byte xAdvance, byte page) {

    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(x);
        outputStream.writeInt(y);
        outputStream.writeByte(width);
        outputStream.writeByte(height);
        outputStream.writeByte(xOffset);
        outputStream.writeByte(yOffset);
        outputStream.writeByte(xAdvance);
        outputStream.writeByte(page);
    }

    public static Glyph deserialize(DataInputStream inputStream) throws IOException {
        return new Glyph(
                inputStream.readInt(), inputStream.readInt(),
                inputStream.readByte(), inputStream.readByte(),
                inputStream.readByte(), inputStream.readByte(),
                inputStream.readByte(), inputStream.readByte());
    }
}
