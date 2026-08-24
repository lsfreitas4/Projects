import {CGFobject} from '../lib/CGF.js';

/**
* MyCylinder
* @constructor
 * @param scene - Reference to MyScene object
 * @param slices - number of divisions around Z
 * @param stacks - number of divisions along Z
*/
export class MyCylinder extends CGFobject {
    constructor(scene, slices, stacks) {
        super(scene);

        this.slices = slices;
        this.stacks = stacks;

        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];

        const alphaAng = 2 * Math.PI / this.slices;

        // Unique vertices: one ring per stack level, with shared vertices between adjacent faces
        for (let j = 0; j <= this.stacks; j++) {
            const z = j / this.stacks;

            for (let i = 0; i < this.slices; i++) {
                const ang = i * alphaAng;
                const x = Math.cos(ang);
                const y = Math.sin(ang);

                this.vertices.push(x, y, z);

                // Normal of perfect cylinder (normalized)
                this.normals.push(x, y, 0);

                this.texCoords.push(i / this.slices, z);
            }
        }

        // Build side quads as two triangles
        for (let j = 0; j < this.stacks; j++) {
            const currentRing = j * this.slices;
            const nextRing = (j + 1) * this.slices;

            for (let i = 0; i < this.slices; i++) {
                const nextSlice = (i + 1) % this.slices;

                const a = currentRing + i;
                const b = currentRing + nextSlice;
                const c = nextRing + i;
                const d = nextRing + nextSlice;

                this.indices.push(a, b, c);
                this.indices.push(c, b, d);
            }
        }

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
}
