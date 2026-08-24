#ifdef GL_ES
precision highp float;
#endif

uniform float timeFactor;
uniform sampler2D uSampler;

varying vec2 vTextureCoord;
varying vec3 vNormal;

void main() {
    vec2 s1 = vec2(sin(timeFactor * 0.5), cos(timeFactor * 0.3)) * 0.1;
    vec2 s2 = vec2(cos(timeFactor * 0.4), sin(timeFactor * 0.6)) * 0.07;

    vec4 layer1 = texture2D(uSampler, vTextureCoord + s1);
    vec4 layer2 = texture2D(uSampler, vTextureCoord * 1.5 + s2);
    vec4 color = mix(layer1, layer2, 0.5);

    float intensity = dot(color.rgb, vec3(0.3, 0.6, 0.1));
    vec3 hotTint = mix(vec3(1.0, 0.3, 0.0), vec3(1.0, 0.9, 0.2), intensity);
    vec3 tinted = mix(color.rgb, hotTint, 0.4);

    float rim = 1.0 - abs(dot(normalize(vNormal), vec3(0.0, 0.0, 1.0)));
    vec3 corona = vec3(1.0, 0.4, 0.0) * pow(rim, 2.5) * 0.8;

    gl_FragColor = vec4(tinted + corona, 1.0);
}

