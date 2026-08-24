import {CGFobject} from '../lib/CGF.js';
/**
 * MyDiamond
 * @constructor
 * @param scene - Reference to MyScene object
 */
export class MyDiamond extends CGFobject {
    constructor(scene) {
        super(scene);
        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [
            // Frente
            -1, 0, 0,
            0, -1, 0,
            0, 1, 0,
            1, 0, 0,

            // Trás
            -1, 0, 0,
            0, -1, 0,
            0, 1, 0,
            1, 0, 0
        ];

        this.indices = [
            0, 1, 2,
            1, 3, 2,

            4, 6, 5,
            5, 6, 7
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

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
}
