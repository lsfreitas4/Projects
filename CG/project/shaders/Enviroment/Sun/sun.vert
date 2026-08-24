attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
attribute vec2 aTextureCoord;

uniform mat4 uMVMatrix;
uniform mat4 uPMatrix;
uniform mat4 uNMatrix;
uniform sampler2D uSampler;
uniform float timeFactor;

varying vec2 vTextureCoord;
varying vec3 vNormal;

void main() {

	vTextureCoord = aTextureCoord;
    vNormal = normalize((uNMatrix * vec4(aVertexNormal, 0.0)).xyz);
	
    vec2 vertScroll = vec2(sin(timeFactor * 0.3), cos(timeFactor * 0.2)) * 0.15;
    vec4 dispSample = texture2D(uSampler, aTextureCoord + vertScroll);
    float intensity = dot(dispSample.rgb, vec3(0.3, 0.6, 0.1));

    vec3 offset = aVertexNormal * intensity * 0.05;
    gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition + offset, 1.0);
}

