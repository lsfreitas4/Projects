#ifdef GL_ES
precision highp float;
#endif

varying vec2 vTextureCoord;
varying vec2 vMapCoord;

uniform sampler2D uSampler;
uniform sampler2D uSampler2;
uniform float timeFactor;

void main() {
	vec2 animatedCoord = fract(vTextureCoord + vec2(0.0, timeFactor * 0.02));
	vec4 waterColor = texture2D(uSampler, animatedCoord);
	vec4 waterMask = texture2D(uSampler2, vMapCoord);

	float mask = smoothstep(0.45, 0.8, waterMask.b);
	waterColor = mix(waterColor, vec4(0.2, 0.3, 0.45, 1.0), 0.2 * mask);

	gl_FragColor = waterColor;
}
