#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Radius;

out vec4 fragColor;

void main() {
    vec4 blurred = vec4(0.0);
    float totalSamples = 0.0;
    for(float r = -Radius; r <= Radius; r += 0.1) {
        vec4 sampleValue = texture(DiffuseSampler, texCoord + oneTexel * r * BlurDir);
        blurred += sampleValue;
        totalSamples += 1.0;
    }
    fragColor = blurred / totalSamples;

    // just ignor the blurring for testing
//    fragColor = texture(DiffuseSampler, texCoord);
}
