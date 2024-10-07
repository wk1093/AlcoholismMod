#version 150

uniform sampler2D DiffuseSampler;

uniform float Wobble;
uniform float Time;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float freq = 0.5;
    float xOffset = sin(texCoord.y * freq + Time * 3.1415926535 * 2.0) * Wobble*0.02;
    float yOffset = cos(texCoord.x * freq + Time * 3.1415926535 * 2.0) * Wobble*0.02;
    vec2 tex = texCoord+vec2(xOffset, yOffset);
    fragColor = texture(DiffuseSampler, tex);
}
