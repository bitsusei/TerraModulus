#version 110

attribute vec3 pos;
attribute vec3 normal;
attribute vec4 color;

varying vec4 texColor;
varying vec3 texNormal;
varying vec4 lightPos;

uniform vec3 lightDir;
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightTransform;

void main() {
    gl_Position = projection * view * model * vec4(pos, 1.0);
    vec3 norm = normalize(normal);
    vec3 lightDirNorm = normalize(-lightDir);
    float bias = max(0.05 * (1.0 - dot(norm, lightDirNorm)), 0.005);
    lightPos = lightTransform * model * vec4(pos + norm * bias, 1.0);
    texNormal = normal;
    texColor = color;
}
