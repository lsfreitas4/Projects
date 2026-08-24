import {CGFobject} from '../lib/CGF.js';
/**
 * MyTriangle
 * @constructor
 * @param scene - Reference to MyScene object
 */
export class MyQuad extends CGFobject {
    constructor(scene) {
        super(scene);
        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [
            -0.5, 0, -0.5, // A
            0.5, 0, -0.5, // B
            -0.5, 0, 0.5, // C
            0.5, 0, 0.5 // D
        ];

        this.indices = [
            2, 1, 0,
            0, 1, 2,

            1, 2, 3,
            3, 2, 1
        ];

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
}
