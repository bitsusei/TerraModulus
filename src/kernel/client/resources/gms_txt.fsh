#version 110

varying vec2 texCoord;

uniform sampler2D msdfTex; // The generated MSDF atlas texture
uniform vec4 textColor;    // Desired text color
uniform vec2 texSize;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

#define DISTANCE_RANGE 3.0
#define FRACTION_1_64 0.015625

// Reference: https://github.com/Blatko1/awesome-msdf
// Reference: https://medium.com/@sihaolu/performant-crisp-text-rendering-in-metal-with-multi-channel-signed-distance-field-msdf-9acd634d0052
void main() {
    vec3 texel = texture2D(msdfTex, texCoord).rgb;
    float dist = median(texel.r, texel.g, texel.b);

    vec2 screenTexSize = 1.0 / fwidth(texCoord);
    float screenPxRange = max(0.5 * dot(vec2(DISTANCE_RANGE) / texSize, screenTexSize), 1.0);
    float pxDist = screenPxRange * (dist - 0.5);

    float opacity = clamp(pxDist + 0.5, 0.0, 1.0);

    if (opacity < FRACTION_1_64) discard;
    gl_FragColor = vec4(textColor.rgb, textColor.a * opacity);
}
