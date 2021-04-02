#version 330 core
layout(location = 0) in vec4 position;
layout(location = 1) in vec2 uvIN;

out vec2 TexCoords;
void main() {
    TexCoords = uvIN;
    gl_Position = position;
}