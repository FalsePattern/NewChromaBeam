package moe.falsepattern.engine.render.texture;

public interface TextureRegionI {
    float u0();
    float v0();
    float u1();
    float v1();
    int x();
    int y();
    int width();
    int height();
    void bind();
    void unbind();
}
