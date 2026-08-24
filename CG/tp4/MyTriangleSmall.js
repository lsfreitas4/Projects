import {CGFobject} from '../lib/CGF.js';
/**
 * MyTriangleSmall
 * @constructor
 * @param scene - Reference to MyScene object
 */
export class MyTriangleSmall extends CGFobject {
    constructor(scene) {
        super(scene);
        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [
            //Frente
            0, 1, 0,
            -1, 0, 0,
            1, 0, 0,

            //Tras

            0, 1, 0,
            -1, 0, 0,
            1, 0, 0
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
            0.5, 1.0, 
            0.0, 0.5,
            0, 1.0,           
            
            0.5, 1.0, 
            0.0, 0.5,
            0, 1.0,
        ]


        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
}
