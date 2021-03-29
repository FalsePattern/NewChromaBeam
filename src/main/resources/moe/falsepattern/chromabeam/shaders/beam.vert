#version 330 core
layout(location = 0) in vec2 position;
layout(location = 1) in vec3 color;

out vec3 beamColor;
uniform vec2 camera;
uniform vec2 aspect;
uniform float zoom;
void main() {
    beamColor = color;
    gl_Position = vec4((position - camera) * aspect * zoom, 0.0, 1.0);
}