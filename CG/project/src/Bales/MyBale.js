import {CGFobject, CGFtexture, CGFappearance} from '../../../lib/CGF.js';
import { MyArrow } from './Arrow/MyArrow.js';

/**
* MyCylinder
* @constructor
 * @param scene - Reference to MyScene object
 * @param slices - number of divisions around Z
 * @param stacks - number of divisions along Z
*/
export class MyBale extends CGFobject {
    constructor(scene, slices = 32, stacks = 32) {
        super(scene);

        this.slices = slices;
        this.stacks = stacks;
        this.position = vec3.fromValues(0, 0, 0);
        this.attached = false;

        this.initBuffers();
        this.initMaterial();
    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];

        const alphaAng = 2 * Math.PI / this.slices;

        for (let j = 0; j <= this.stacks; j++) {
            const z = j / this.stacks;
            for (let i = 0; i < this.slices; i++) {
                const ang = i * alphaAng;
                const x = Math.cos(ang);
                const y = Math.sin(ang);
                this.vertices.push(x, y, z);
                this.normals.push(x, y, 0);
                this.texCoords.push(i / this.slices, z);
            }
        }

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

        const bottomCenterIndex = this.vertices.length / 3;
        this.vertices.push(0, 0, 0);
        this.normals.push(0, 0, -1);
        this.texCoords.push(0.5, 0.5);

        const bottomRingStart = this.vertices.length / 3;
        for (let i = 0; i < this.slices; i++) {
            const ang = i * alphaAng;
            const x = Math.cos(ang);
            const y = Math.sin(ang);
            this.vertices.push(x, y, 0);
            this.normals.push(0, 0, -1);
            this.texCoords.push(0.5 + 0.5 * x, 0.5 + 0.5 * y);
        }

        for (let i = 0; i < this.slices; i++) {
            const next = (i + 1) % this.slices;
            this.indices.push(bottomCenterIndex, bottomRingStart + next, bottomRingStart + i);
        }

        const topCenterIndex = this.vertices.length / 3;
        this.vertices.push(0, 0, 1);
        this.normals.push(0, 0, 1);
        this.texCoords.push(0.5, 0.5);

        const topRingStart = this.vertices.length / 3;
        for (let i = 0; i < this.slices; i++) {
            const ang = i * alphaAng;
            const x = Math.cos(ang);
            const y = Math.sin(ang);
            this.vertices.push(x, y, 1);
            this.normals.push(0, 0, 1);
            this.texCoords.push(0.5 + 0.5 * x, 0.5 + 0.5 * y);
        }

        for (let i = 0; i < this.slices; i++) {
            const next = (i + 1) % this.slices;
            this.indices.push(topCenterIndex, topRingStart + i, topRingStart + next);
        }

        this.arrow = new MyArrow(this.scene);

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }

    initMaterial(){
        this.baleTexture = new CGFtexture(this.scene, 'textures/Bale/bale.jpg');
        this.baleMaterial = new CGFappearance(this.scene);
        this.baleMaterial.setAmbient(0.2, 0.2, 0.2, 1);
        this.baleMaterial.setDiffuse(0.4, 0.4, 0.4, 1);
        this.baleMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.baleMaterial.setShininess(10);
        this.baleMaterial.setTexture(this.baleTexture);
        this.baleMaterial.setTextureWrap('REPEAT', 'REPEAT');

    }

    display(){

        this.scene.pushMatrix();

            this.baleMaterial.apply();
            this.scene.rotate(Math.PI/2, 1, 0, 0);
            this.scene.translate(0, 0, -2);
            this.scene.scale(1, 1, 2);
            super.display();

        this.scene.popMatrix();

    }
}
