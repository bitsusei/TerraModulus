#version 110

varying vec4 texColor;
varying vec3 texNormal;

uniform mat4 filter;
uniform vec3 lightDir;
uniform float nearThreshold;
uniform float farThreshold;
uniform vec3 fogColor;

const float E = 2.71828182845;

void main() {
    float ambientStrength = 0.1;
    vec3 lightColor = vec3(1.0, 1.0, 1.0);
    vec3 ambient = ambientStrength * lightColor;
    vec3 norm = normalize(texNormal);
    vec3 lightDirNorm = normalize(-lightDir);
    float diff = max(dot(norm, lightDirNorm), 0.0);
    vec3 diffuse = diff * lightColor;
    gl_FragColor = filter * vec4(ambient + diffuse, 1.0) * texColor;
    float depth = gl_FragCoord.z * 2.0 - 1.0; // NDC
    if (depth > farThreshold) {
        gl_FragColor = mix(gl_FragColor, vec4(fogColor, 1.0), pow((depth - farThreshold) / (1.0 - farThreshold), E));
    } else if (depth < -nearThreshold) {
        gl_FragColor = mix(gl_FragColor, vec4(fogColor, 1.0), pow((-depth - nearThreshold) / (1.0 - nearThreshold), E));
    }
}
