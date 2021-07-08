#version 330 core
in vec2 uv;
in vec4 color;

uniform sampler2D textureSampler;

out vec4 colorOUT;
void main() {
    colorOUT = color * texture(textureSampler, uv);
}
