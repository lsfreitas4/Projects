import {CGFobject} from '../../../../lib/CGF.js';

export class MyCoverOuter extends CGFobject {

    constructor(scene, radius = 1.0){
        super(scene);
        this.slices = 100;
        this.stacks = 6;
        this.width = 6;
        this.radius = radius;
        this.initBuffers();

    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];

        const alphaAng =  Math.PI / this.slices;

        // Unique vertices: one ring per stack level, with shared vertices between adjacent faces
        for (let j = 0; j <= this.stacks; j++) {
            const z = (j / this.stacks) * this.width;

            for (let i = 0; i <= this.slices; i++) {
                const ang = i * alphaAng;
                const x = Math.cos(ang);
                const y = Math.sin(ang);

                this.vertices.push(x * this.radius, y * this.radius, z);

                // Normal of perfect cylinder (normalized)
                this.normals.push(x, y, 0);

                this.texCoords.push(i / this.slices, z);
            }
        }

        // Build side quads as two triangles
        const ringSize = this.slices + 1;

        for (let j = 0; j < this.stacks; j++) {
            const currentRing = j * ringSize;
            const nextRing = (j + 1) * ringSize;

            for (let i = 0; i < this.slices; i++) {

                const a = currentRing + i;
                const b = currentRing + i + 1;
                const c = nextRing + i;
                const d = nextRing + i + 1;

                this.indices.push(a, b, c);
                this.indices.push(c, b, d);
            }
        }

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();

    }



}