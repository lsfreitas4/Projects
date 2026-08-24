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
            // Frente
            0, 0, 0,
            1, 1, 0,
            2, 0, 0,
            3, 1, 0,

            // Trás
            0, 0, 0,
            1, 1, 0,
            2, 0, 0,
            3, 1, 0
        ];

        this.indices = [
            1, 0, 2,
            1, 2, 3,

            5, 6, 4,
            5, 7, 6
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
