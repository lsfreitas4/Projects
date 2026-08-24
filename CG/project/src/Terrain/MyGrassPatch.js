import { CGFobject } from '../../../lib/CGF.js';

export class MyGrassPatch extends CGFobject {
    constructor(scene, bladeSpecs) {
        super(scene);
        this.bladeSpecs = bladeSpecs;
        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];

        const bladeWidth = 0.08;
        const bladeHeight = 0.7;
        const bladeBend = 0.12;
        const halfW = bladeWidth * 0.5;

        let vertexIndex = 0;

        for (const spec of this.bladeSpecs) {
            const worldX = spec.worldX;
            const worldZ = spec.worldZ;
            const rotY = spec.rotY;
            const scale = spec.scale;

            const bladeVertices = [
                -halfW, 0.0, 0.0,
                halfW, 0.0, 0.0,
                -halfW * 0.25 + bladeBend, bladeHeight, 0.0,
                halfW * 0.25 + bladeBend, bladeHeight, 0.0
            ];

            for (let i = 0; i < bladeVertices.length; i += 3) {
                let vx = bladeVertices[i] * scale;
                let vy = bladeVertices[i + 1] * scale;
                let vz = bladeVertices[i + 2] * scale;


                const cy = Math.cos(rotY);
                const sy = Math.sin(rotY);
                const rotX = vx * cy - vz * sy;
                const rotZ = vx * sy + vz * cy;

                this.vertices.push(rotX + worldX, vy, rotZ + worldZ);
                this.normals.push(0, 0, 1);
            }

            const i0 = vertexIndex;
            const i1 = vertexIndex + 1;
            const i2 = vertexIndex + 2;
            const i3 = vertexIndex + 3;

            this.indices.push(i0, i2, i1);
            this.indices.push(i1, i2, i3);

            this.texCoords.push(0, 0, 1, 0, 0, 1, 1, 1);

            vertexIndex += 4;
        }

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
}