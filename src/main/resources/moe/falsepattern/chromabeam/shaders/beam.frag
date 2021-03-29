#version 330 core
in vec3 beamColor;
out vec4 color;

void main() {
    color = vec4(beamColor, 1);
}