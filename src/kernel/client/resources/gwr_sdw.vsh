#version 110

attribute vec3 pos;

uniform mat4 model;
uniform mat4 lightTransform;

void main() {
    gl_Position = lightTransform * model * vec4(pos, 1.0);
}
