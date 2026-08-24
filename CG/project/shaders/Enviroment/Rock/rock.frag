#ifdef GL_ES
precision highp float;
#endif

varying vec2 vTextureCoord;
varying float vNoise;

uniform sampler2D uSampler;

float random(in vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

float noise(in vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

void main() {
    vec4 rockTex = texture2D(uSampler, vTextureCoord * 2.0);
    
    vec3 mossColor = vec3(0.22, 0.31, 0.14);
    vec3 darkStone = vec3(0.18, 0.16, 0.15);
    
    vec3 baseMix = mix(rockTex.rgb, darkStone, clamp(vNoise * 0.3, 0.0, 1.0));
    float mossMask = smoothstep(0.4, 0.7, vNoise);
    vec3 finalColor = mix(baseMix, mossColor, mossMask * 0.6);

    gl_FragColor = vec4(finalColor, 1.0);
}