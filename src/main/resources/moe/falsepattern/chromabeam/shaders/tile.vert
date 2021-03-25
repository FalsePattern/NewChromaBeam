#version 330 core
layout(location = 0) in vec2 position;
layout(location = 1) in vec2 uvIN;

out vec2 uv;
uniform vec2 chunkOffset;
uniform vec2 aspect;
uniform float zoom;
void main() {
    uv = uvIN;
    gl_Position = vec4((position - chunkOffset) * aspect * zoom, 0.0, 1.0);
}