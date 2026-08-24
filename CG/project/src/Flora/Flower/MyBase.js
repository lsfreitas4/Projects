import { CGFobject } from '../../../../lib/CGF.js';

export class MyBase extends CGFobject {


    constructor(scene, options = {}) {
        super(scene);
        this.width = options.width ?? MyBase.randomBetween(0.2, 0.4);
        this.height = options.height ?? MyBase.randomBetween(0.5, 2.0);

        this.color = options.color ?? [
            0,
            MyBase.randomBetween(0.3, 0.9),
            0
        ];

        this.initBuffers();
    }

    static randomBetween(min, max) {
        return min + Math.random() * (max - min);
    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];

        const w = this.width / 2;
        const h = this.height;

        this.vertices.push(
            -w, 0, 0,
            w, 0, 0,
            w, h, 0,
            -w, h, 0
        );

        for (let i = 0; i < 4; i++) {
            this.normals.push(0, 0, 1);
        }

        this.texCoords.push(
            0, 1,
            1, 1,
            1, 0,
            0, 1
        );

        this.indices.push(0, 1, 2);
        this.indices.push(0, 2, 3);

        this.indices.push(0, 2, 1);
        this.indices.push(0, 3, 2);

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();

    }

}