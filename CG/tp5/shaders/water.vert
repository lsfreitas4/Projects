attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec2 aTextureCoord;

uniform mat4 uMVMatrix;
uniform mat4 uPMatrix;
uniform mat4 uNMatrix;

uniform sampler2D uSampler2;
uniform float timeFactor;
uniform float normScale;

varying vec2 vTextureCoord;
varying vec2 vMapCoord;

void main() {
	vTextureCoord = fract(aTextureCoord + vec2(timeFactor * 0.03, timeFactor * 0.02));
	vMapCoord = fract(aTextureCoord + vec2(timeFactor * 0.025, timeFactor * 0.015));

	float height = texture2D(uSampler2, vMapCoord).b;
	float scale = clamp(abs(normScale), 0.0, 50.0) / 50.0;
	float displacement = (height - 0.5) * (0.08 * scale);
	vec3 displacedPosition = aVertexPosition + aVertexNormal * displacement;

	gl_Position = uPMatrix * uMVMatrix * vec4(displacedPosition, 1.0);
}
