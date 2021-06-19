#version 330 core
layout(location = 0) in vec2 position;
layout(location = 1) in vec3 color;

out vec4 fragColor;
uniform mat3 projectionMatrix;
uniform float zoom;
void main() {
    fragColor = vec4(color, 1);
    gl_Position = vec4((projectionMatrix * vec3(position, 1.0)).xy, 0.0, 1.0);
}