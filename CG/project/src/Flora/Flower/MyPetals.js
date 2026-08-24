import { CGFobject } from '../../../../lib/CGF.js';

export class MyPetals extends CGFobject {


    constructor(scene, subdivisions = 10, width = 0.5, height = 1.0) {
        super(scene);
        this.subdivisions = subdivisions;
        this.width = width;
        this.height = height;
        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];

        this.vertices.push(0, 0, 0);
        this.normals.push(0, 1, 0);
        this.texCoords.push(0.5, 0.5);

        const step = Math.PI / this.subdivisions;

        for (let i = 0; i <= this.subdivisions; i++) {
            const angle = i * step;
            const x = this.width * Math.cos(angle);
            const z = this.height * Math.sin(angle);

            this.vertices.push(x, 0, z);
            this.normals.push(0, 1, 0);
            this.texCoords.push(
                (x / this.width + 1) / 2,
                1 - (z / this.height + 1) / 2
            );
        }

        for (let i = 1; i <= this.subdivisions; i++) {
            this.indices.push(0, i, i + 1);
            this.indices.push(0, i + 1, i);
        }

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }

}