import { CGFobject } from '../../../../lib/CGF.js';

export class MyRock extends CGFobject {
    constructor(scene, options = {}) {
        super(scene);
        this.radius = options.radius ?? 1.0;
        this.sectorCount = options.sectorCount ?? 12;
        this.stackCount = options.stackCount ?? 12;

        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];

        let sectorStep = (2 * Math.PI) / this.sectorCount;
        let stackStep = (Math.PI / 2) / this.stackCount;

        for (let i = 0; i <= this.stackCount; ++i) {
            let stackAngle = Math.PI / 2 - i * stackStep;
            let xy = this.radius * Math.cos(stackAngle);
            let y = this.radius * Math.sin(stackAngle);

            for (let j = 0; j <= this.sectorCount; ++j) {
                let sectorAngle = j * sectorStep;
                let x = xy * Math.cos(sectorAngle);
                let z = xy * Math.sin(sectorAngle);

                this.vertices.push(x, y, z);

                // Vetor normalizado para usar na perturbação
                let len = Math.sqrt(x * x + y * y + z * z) || 1;
                this.normals.push(x / len, y / len, z / len);

                this.texCoords.push(j / this.sectorCount, i / this.stackCount);
            }
        }

        for (let i = 0; i < this.stackCount; i++) {
            let k1 = i * (this.sectorCount + 1);
            let k2 = k1 + this.sectorCount + 1;

            for (let j = 0; j < this.sectorCount; j++, k1++, k2++) {
                this.indices.push(k1, k2, k1 + 1);
                this.indices.push(k1 + 1, k2, k2 + 1);
            }
        }

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
}