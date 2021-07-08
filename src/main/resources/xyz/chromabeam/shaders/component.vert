#version 330 core
layout(location = 0) in vec2 position;
layout(location = 1) in vec2 uvIN;
layout(location = 2) in vec4 colorIN;

uniform mat3 projectionMatrix;
uniform vec2 chunk;

out vec2 uv;
out vec4 color;
void main() {
    uv = uvIN;
    color = colorIN;
    gl_Position = vec4((projectionMatrix * vec3(position + chunk, 1.0)).xy, 0.0, 1.0);
}

