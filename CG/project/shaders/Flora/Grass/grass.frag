#ifdef GL_ES
precision highp float;
#endif

varying vec2 vWorldXZ;

uniform float uDenseThreshold;   
uniform float uDeadThreshold;    

// Biome colors.
const vec3 COLOR_DEAD   = vec3(0.70, 0.55, 0.20);
const vec3 COLOR_NORMAL = vec3(0.35, 0.72, 0.18);
const vec3 COLOR_DENSE  = vec3(0.08, 0.40, 0.05);

float random2(vec2 st) {
    return fract(sin(dot(st, vec2(12.9898, 78.233))) * 43758.5453123);
}

float noise2(vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);
    float a = random2(i);
    float b = random2(i + vec2(1.0, 0.0));
    float c = random2(i + vec2(0.0, 1.0));
    float d = random2(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}


float patchMask(vec2 worldXZ) {
    float broad  = noise2(worldXZ * 0.07);
    float detail = noise2(worldXZ * 0.28);
    return clamp(broad * 0.75 + detail * 0.25, 0.0, 1.0);
}

void main() {
    float score = patchMask(vWorldXZ);

    float toDead  = smoothstep(uDeadThreshold  - 0.04, uDeadThreshold  + 0.04, score);
    float toDense = 1.0 - smoothstep(uDenseThreshold - 0.04, uDenseThreshold + 0.04, score);

    vec3 color = COLOR_NORMAL;
    color = mix(color, COLOR_DEAD,  toDead);
    color = mix(color, COLOR_DENSE, toDense);

    gl_FragColor = vec4(color, 1.0);
}
