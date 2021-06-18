#version 330 core
layout(location = 0) in vec2 position;
layout(location = 1) in vec4 color;

out vec4 fragColor;
void main() {
    fragColor = color;
    gl_Position = vec4(position, 0, 1);
}