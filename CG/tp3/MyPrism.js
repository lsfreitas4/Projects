import {CGFobject} from '../lib/CGF.js';

/**
* MyPrism
* @constructor
 * @param scene - Reference to MyScene object
 * @param slices - number of prism sides
 * @param stacks - number of divisions along Z
*/
export class MyPrism extends CGFobject {
    constructor(scene, slices, stacks)
    {
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

        for (let i = 0; i < this.slices; i++) {
            const ang = i * alphaAng;
            const nextAng = (i + 1) * alphaAng;

            const ca = Math.cos(ang);
            const sa = Math.sin(ang);
            const cb = Math.cos(nextAng);
            const sb = Math.sin(nextAng);

            const midAng = ang + alphaAng / 2;
            const nx = Math.cos(midAng);
            const ny = Math.sin(midAng);

            for (let j = 0; j < this.stacks; j++) {
                const z0 = j / this.stacks;
                const z1 = (j + 1) / this.stacks;

                const base = this.vertices.length / 3;

                // Four vertices for each quad face section
                this.vertices.push(ca, sa, z0);
                this.vertices.push(cb, sb, z0);
                this.vertices.push(ca, sa, z1);
                this.vertices.push(cb, sb, z1);

                // Same normal for the four vertices of this face section (flat shading)
                this.normals.push(nx, ny, 0);
                this.normals.push(nx, ny, 0);
                this.normals.push(nx, ny, 0);
                this.normals.push(nx, ny, 0);

                this.indices.push(base, base + 1, base + 2);
                this.indices.push(base + 2, base + 1, base + 3);

                this.texCoords.push(i / this.slices, z0);
                this.texCoords.push((i + 1) / this.slices, z0);
                this.texCoords.push(i / this.slices, z1);
                this.texCoords.push((i + 1) / this.slices, z1);
            }
        }

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
}
