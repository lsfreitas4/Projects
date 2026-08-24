#ifdef GL_ES
precision highp float;
#endif

varying vec2 vTextureCoord;
varying vec3 vNormal;


uniform vec4 uOverrideColor;

void main() {
    vec3 baseColor = (uOverrideColor.a >= 0.0) ? uOverrideColor.rgb : vec3(1.0);

    vec3 N = normalize(vNormal);
    vec3 L = normalize(vec3(0.4, 0.85, -0.4));
    float ndl = max(dot(N, L), 0.0);

    vec3 color = baseColor * (0.55 + 0.45 * ndl);

    gl_FragColor = vec4(color, 1.0);
}
