import { CGFobject } from '../../../../lib/CGF.js';

/**
* MySun
* @constructor
 * @param scene - Reference to MyScene object
 * @param stackCount - number of divisions around Z
 * @param sectorCount - number of divisions along Z
 * @param radius - radius of the sphere
*/
export class MyMiddle extends CGFobject {
    constructor(scene, options = {}) {
        super(scene);

        this.radius = options.radius ?? MyMiddle.randomBetween(0.15, 0.25);
        this.sectorCount = options.sectorCount ?? 32;
        this.stackCount = options.stackCount ?? 32;
        this.color = options.color ?? MyMiddle.randomCenterColor();

        this.initBuffers();
    }

    static randomBetween(min, max) {
        return min + Math.random() * (max - min);
    }

    static randomCenterColor() {
        const palettes = [
            [1.0, 0.8, 0.0],   // golden yellow
            [0.6, 0.3, 0.0],   // brown
            [1.0, 1.0, 0.2],   // bright yellow
        ];
        return palettes[Math.floor(Math.random() * palettes.length)];
    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];


        let x, y, z, xy;

        let nx, ny, nz, lengthInv = 1.0 / this.radius;
        let s, t;

        let sectorStep = (2 * Math.PI) / this.sectorCount;
        let stackStep = (Math.PI / 2) / this.stackCount;

        let stackAngle, sectorAngle;

        for (let i = 0; i <= this.stackCount; ++i) {
            stackAngle = Math.PI / 2 - i * stackStep;

            xy = this.radius * Math.cos(stackAngle);
            y = this.radius * Math.sin(stackAngle);

            for (let j = 0; j <= this.sectorCount; ++j) {

                sectorAngle = j * sectorStep;

                x = xy * Math.cos(sectorAngle);
                z = xy * Math.sin(sectorAngle);

                this.vertices.push(x, y, z);


                nx = x * lengthInv;
                ny = y * lengthInv;
                nz = z * lengthInv;

                this.normals.push(nx, ny, nz);


                s = 1.0 - (j / this.sectorCount);
                t = i / this.stackCount;
                this.texCoords.push(s, t);

            }

        }

        let k1, k2;

        for (let i = 0; i < this.stackCount; i++) {

            k1 = i * (this.sectorCount + 1);
            k2 = k1 + this.sectorCount + 1;

            for (let j = 0; j < this.sectorCount; j++, k1++, k2++) {

                this.indices.push(k1, k2, k1 + 1);
                this.indices.push(k1 + 1, k2, k2 + 1);


                this.indices.push(k1, k1 + 1, k2);
                this.indices.push(k1 + 1, k2 + 1, k2);
            }


        }

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
}
