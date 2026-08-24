
attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec2 aTextureCoord;

uniform mat4 uMVMatrix;
uniform mat4 uPMatrix;
uniform mat4 uNMatrix;
uniform float timeFactor;

varying vec2 vTextureCoord;
uniform sampler2D uSampler2;

uniform float normScale;

void main() {
	vTextureCoord = aTextureCoord;

	float xOffset = sin(timeFactor) * normScale * 0.03;
	vec3 animatedPosition = aVertexPosition + vec3(xOffset, 0.0, 0.0);

	gl_Position = uPMatrix * uMVMatrix * vec4(animatedPosition, 1.0);
}

