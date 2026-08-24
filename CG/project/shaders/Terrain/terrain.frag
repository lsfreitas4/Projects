#ifdef GL_ES
precision highp float;
#endif

varying vec2 vTextureCoord;

uniform sampler2D uSampler;   // grass texture
uniform sampler2D uSampler2;  // dirt 

float random (in vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123);
}

float noise (in vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f*f*(3.0-2.0*f);
    return mix(a, b, u.x) + (c - a)* u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
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

void main() {
    vec4 healthyGrass = texture2D(uSampler, vTextureCoord);
    vec4 dirt         = texture2D(uSampler2, vTextureCoord);

    float road = roadMask(vTextureCoord);

    // Dirt texture on the road, grass on the prairie.
    gl_FragColor = mix(healthyGrass, dirt, road);
}
