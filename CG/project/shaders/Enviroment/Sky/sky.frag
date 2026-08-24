	#ifdef GL_ES
	precision highp float;
	#endif

	uniform float timeFactor;
	varying vec2 vTextureCoord;

	uniform sampler2D uSampler; // sky texture
	uniform sampler2D uSampler2; // cloud texture



	void main() {
		vec2 coord = vTextureCoord;
		
		vec4 skyColor = texture2D(uSampler, coord);

		vec2 offset = vec2(timeFactor*0.005, 0.0);
		vec2 wrappedCoord = fract(coord + offset);
		
		vec4 skyCloud = texture2D(uSampler2, wrappedCoord);

		float cloudFactor = smoothstep(0.0, 1.0, skyCloud.a);
		gl_FragColor = mix(skyColor, skyCloud, cloudFactor*0.5);
	}
