import { CGFobject } from '../../../lib/CGF.js';

/**
 * MyCircle
 * @constructor
 * @param scene
 * @param slices
 */
export class MyCircle extends CGFobject {
    constructor(scene, slices = 32) {
        super(scene);
        this.slices = slices;
        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];

        this.vertices.push(0, 0, 0);
        this.normals.push(0, 0, 1);
        this.texCoords.push(0.5, 0.5);

        const alphaAng = 2 * Math.PI / this.slices;

        for (let i = 0; i <= this.slices; i++) {
            const ang = i * alphaAng;
            const x = Math.cos(ang);
            const y = Math.sin(ang);

            this.vertices.push(x, y, 0);
            this.normals.push(0, 0, 1);

            this.texCoords.push(0.5 + x * 0.5, 0.5 - y * 0.5);
        }

        for (let i = 1; i <= this.slices; i++) {
            this.indices.push(0, i, i + 1);
        }

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
}