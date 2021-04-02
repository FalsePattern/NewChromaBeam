#version 330 core
layout(location = 0) in vec2 position;
layout(location = 1) in vec3 color;

out vec3 beamColor;
uniform mat3 projectionMatrix;
uniform float zoom;
void main() {
    beamColor = color;
    gl_Position = vec4((projectionMatrix * vec3(position, 1.0)).xy, 0.0, 1.0);
}