#version 330 core
layout(location = 0) in vec2 position;
layout(location = 1) in vec2 uvIN;

out vec2 uv;
uniform mat3 projectionMatrix;
uniform vec2 chunk;
void main() {
    uv = uvIN;
    gl_Position = vec4((projectionMatrix * vec3(position + chunk, 1.0)).xy, 0.0, 1.0);
}

