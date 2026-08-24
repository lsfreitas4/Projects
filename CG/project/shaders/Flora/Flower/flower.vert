attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec2 aTextureCoord;

uniform mat4 uMVMatrix;
uniform mat4 uPMatrix;
uniform mat4 uNMatrix;

uniform float nScale;
uniform float uTerrainWidth;
uniform float uBaseLift;

uniform vec2 uFlowerWorldXZ;

varying vec2 vTextureCoord;
varying vec3 vNormal;

float random (in vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

float noise (in vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm (in vec2 st) {
    float value = 0.0;
    float amp   = 0.5;
    float freq  = 1.0;
    for (int i = 0; i < 4; i++) {
        value += amp * noise(st * freq);
        freq  *= 2.0;
        amp   *= 0.5;
    }
    return value;
}

float roadMask(vec2 uv) {
    vec2 p = uv - 0.5;
    float dist  = length(p);
    float angle = atan(p.y, p.x);

    float baseRadius = 0.32;
    float wobble = 0.0;
    wobble += 0.030 * sin(angle * 3.0 + 1.7);
    wobble += 0.018 * sin(angle * 7.0 + 0.5);
    wobble += 0.012 * noise(vec2(angle * 2.0, 4.3)) * 2.0 - 0.012;
    float ringRadius = baseRadius + wobble;

    float halfWidth = 0.060 + 0.015 * sin(angle * 5.0 + 2.1);
    float feather   = 0.025;

    float d = abs(dist - ringRadius);
    float ring = 1.0 - smoothstep(halfWidth, halfWidth + feather, d);

    return clamp(ring, 0.0, 1.0);
}

float roadHeightAt(vec2 uv) {
    float base = 0.15;
    return base;
}

void main() {
    vTextureCoord = aTextureCoord;
    vNormal = normalize(vec3(uNMatrix * vec4(aVertexNormal, 0.0)));

    vec2 terrainUV = vec2(
        uFlowerWorldXZ.x / uTerrainWidth + 0.5,
        uFlowerWorldXZ.y / uTerrainWidth + 0.5
    );

    float prairie  = fbm(terrainUV * 4.0);
    float road     = roadMask(terrainUV);
    float roadH    = roadHeightAt(terrainUV);
    float elevation = mix(prairie, roadH, road);
    float terrainY = elevation * nScale + uBaseLift;

    float scaleY = length(uMVMatrix[1].xyz);
    float lift   = (scaleY > 0.00001) ? (terrainY / scaleY) : terrainY;

    vec3 displaced = aVertexPosition + vec3(0.0, lift, 0.0);

    gl_Position = uPMatrix * uMVMatrix * vec4(displaced, 1.0);
}
