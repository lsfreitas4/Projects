// MyWheelsCap.js
import { CGFobject } from '../../../../lib/CGF.js';


export class MyWheelsCap extends CGFobject {
    constructor(scene, options = {}) {
        super(scene);
        this.slices = options.slices ?? 100;
        this.innerRadius = options.innerRadius ?? 0.8;
        this.outerRadius = options.outerRadius ?? 1.0;
        this.z = options.z ?? 0.0;
        this.facing = options.facing ?? 1;
        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];

        const alphaAng = 2 * Math.PI / this.slices;
        const nz = this.facing >= 0 ? 1 : -1;

        for (let i = 0; i < this.slices; i++) {
            const ang = i * alphaAng;
            const cosA = Math.cos(ang);
            const sinA = Math.sin(ang);

            this.vertices.push(cosA * this.outerRadius, sinA * this.outerRadius, this.z);
            this.normals.push(0, 0, nz);
            this.texCoords.push(0.5 + cosA * 0.5, 0.5 + sinA * 0.5);

            // Vértice interior
            this.vertices.push(cosA * this.innerRadius, sinA * this.innerRadius, this.z);
            this.normals.push(0, 0, nz);
            this.texCoords.push(0.5 + cosA * 0.35, 0.5 + sinA * 0.35);
        }

        for (let i = 0; i < this.slices; i++) {
            const next = (i + 1) % this.slices;
            const oA = i * 2;
            const iA = i * 2 + 1;
            const oB = next * 2;
            const iB = next * 2 + 1;

            if (nz > 0) {

                this.indices.push(oA, iA, oB);
                this.indices.push(oB, iA, iB);
            } else {
                this.indices.push(oA, oB, iA);
                this.indices.push(oB, iB, iA);
            }
        }

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
}