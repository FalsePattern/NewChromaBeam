#version 330 core
layout(location = 0) in vec2 position;
layout(location = 1) in vec2 uvIN;

out vec2 uv;
void main() {
    uv = uvIN;
    gl_Position = vec4(position, 0, 1);
}