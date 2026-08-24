import {CGFobject} from '../lib/CGF.js';
/**
 * MyParallelogram
 * @constructor
 * @param scene - Reference to MyScene object
 */
export class MyParallelogram extends CGFobject {
    constructor(scene) {
        super(scene);
        this.initBuffers();
    }
    
    initBuffers() {
        this.vertices = [
            //Frente
            0, 0, 0,	//0
            1, 1, 0,	//1
            2, 0, 0,	//2
            3, 1, 0,	//3

            //Tras
            0, 0, 0,	//4
            1, 1, 0,	//5
            2, 0, 0,	//6
            3, 1, 0		//7
        ];

        //Counter-clockwise reference of vertices
        this.indices = [
            1, 0, 2,
            1, 2, 3,
            
            4, 5, 6,
            5, 7, 6,
        
        ];

        this.normals = [
			0, 0, 1,
            0, 0, 1,
            0, 0, 1,
			0, 0, 1,

            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
			0, 0, -1

		];

        this.texCoords = [
            0.25, 0.75,
            0.5, 1,
            0.75, 0.75,
            1, 1,

            0.25, 0.75,
            0.5, 1,
            0.75, 0.75,
            1, 1,
        ]

        //The defined indices (and corresponding vertices)
        //will be read in groups of three to draw triangles
        this.primitiveType = this.scene.gl.TRIANGLES;

        this.initGLBuffers();
    }
}

