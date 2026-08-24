import {CGFobject} from '../lib/CGF.js';
/**
 * MyTriangle
 * @constructor
 * @param scene - Reference to MyScene object
 */
export class MyTriangle extends CGFobject {
    constructor(scene) {
        super(scene);
        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [
            //Frente
            -1, 1, 0,
            -1, -1, 0,
            1, -1, 0,

            //Tras
            -1, 1, 0,
            -1, -1, 0,
            1, -1, 0

        ];

        this.indices = [
            0, 1, 2,
            3, 5, 4
        ];
        
        this.normals = [
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,

            0, 0, -1,
            0, 0, -1,
            0, 0, -1

        ];

        this.texCoords = [
            0.0, 0.0,  // vertex 0: (-1, 1)
            0.0, 0.5,  // vertex 1: (-1,-1)
            0.5, 0.5,  // vertex 2: ( 1,-1)
            // back face mirrors front
            0.0, 0.0,
            0.0, 0.5,
            0.5, 0.5,
        ];


        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }

    updateTexCoords(coords) {
		this.texCoords = [...coords];
		this.updateTexCoordsGLBuffers();
	}
}
