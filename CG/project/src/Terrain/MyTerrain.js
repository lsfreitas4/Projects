import { CGFobject } from '../../../lib/CGF.js';

/**
 * MyTerrain - Circular terrain grid
 * @constructor
 * @param scene  - Reference to MyScene
 * @param nrDivs - Number of grid divisions
 * @param width  - Total diameter of the terrain
 */
export class MyTerrain extends CGFobject {
    constructor(scene, nrDivs = 100, width = 210) {
        super(scene);
        this.nrDivs = nrDivs;
        this.width = width;
        this.radius = this.width * 0.5;
        this.step = this.width / this.nrDivs;

        this.roadHeight = 0.15;

        this.initBuffers();
    }

    initBuffers() {
        this.vertices = [];
        this.indices = [];
        this.normals = [];
        this.texCoords = [];

        const step = this.width / this.nrDivs;
        const radius = this.width / 2;

        const vertexIndex = [];

        for (let j = 0; j <= this.nrDivs; j++) {
            vertexIndex[j] = [];
            for (let i = 0; i <= this.nrDivs; i++) {
                const x = i * step - radius;
                const z = j * step - radius;

                if (x * x + z * z <= radius * radius) {
                    vertexIndex[j][i] = this.vertices.length / 3;
                    this.vertices.push(x, 0, z);
                    this.normals.push(0, 1, 0);
                    this.texCoords.push(x / this.width + 0.5, z / this.width + 0.5);
                } else {
                    vertexIndex[j][i] = -1;
                }
            }
        }

        for (let j = 0; j < this.nrDivs; j++) {
            for (let i = 0; i < this.nrDivs; i++) {
                const v00 = vertexIndex[j][i];
                const v10 = vertexIndex[j][i + 1];
                const v01 = vertexIndex[j + 1][i];
                const v11 = vertexIndex[j + 1][i + 1];

                if (v00 === -1 || v10 === -1 || v01 === -1 || v11 === -1) continue;

                // Triangle 1
                this.indices.push(v00, v01, v10);
                // Triangle 2
                this.indices.push(v01, v11, v10);
            }
        }

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }

    fract(value) {
        return value - Math.floor(value);
    }

    random2(stx, sty) {
        return this.fract(Math.sin(stx * 12.9898 + sty * 78.233) * 43758.5453123);
    }

    noise2(stx, sty) {
        const ix = Math.floor(stx);
        const iy = Math.floor(sty);
        const fx = this.fract(stx);
        const fy = this.fract(sty);

        const a = this.random2(ix, iy);
        const b = this.random2(ix + 1.0, iy);
        const c = this.random2(ix, iy + 1.0);
        const d = this.random2(ix + 1.0, iy + 1.0);

        const ux = fx * fx * (3.0 - 2.0 * fx);
        const uy = fy * fy * (3.0 - 2.0 * fy);

        return a * (1.0 - ux) * (1.0 - uy)
            + b * ux * (1.0 - uy)
            + c * (1.0 - ux) * uy
            + d * ux * uy;
    }

    fbm2(stx, sty) {
        let value = 0.0;
        let amp = 0.5;
        let freq = 1.0;
        for (let i = 0; i < 4; i++) {
            value += amp * this.noise2(stx * freq, sty * freq);
            freq *= 2.0;
            amp *= 0.5;
        }
        return value;
    }

    clamp01(value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    smoothstep(edge0, edge1, x) {
        const t = this.clamp01((x - edge0) / (edge1 - edge0));
        return t * t * (3.0 - 2.0 * t);
    }

    getTextureCoordsAt(x, z) {
        return {
            u: this.clamp01((x + this.width * 0.5) / this.width),
            v: this.clamp01((z + this.width * 0.5) / this.width)
        };
    }


    roadMask(u, v) {
        const px = u - 0.5;
        const py = v - 0.5;
        const dist = Math.hypot(px, py);
        const angle = Math.atan2(py, px);

        const baseRadius = 0.32;
        let wobble = 0.0;
        wobble += 0.030 * Math.sin(angle * 3.0 + 1.7);
        wobble += 0.018 * Math.sin(angle * 7.0 + 0.5);
        wobble += 0.012 * this.noise2(angle * 2.0, 4.3) * 2.0 - 0.012;
        const ringRadius = baseRadius + wobble;

        const halfWidth = 0.060 + 0.015 * Math.sin(angle * 5.0 + 2.1);
        const feather = 0.025;

        const d = Math.abs(dist - ringRadius);
        const ring = 1.0 - this.smoothstep(halfWidth, halfWidth + feather, d);

        return this.clamp01(ring);
    }

    roadHeightAt(u, v) {
        const base = 0.15;
        return base;
    }

    getTrampledMaskAt(x, z) {
        const coords = this.getTextureCoordsAt(x, z);
        return this.roadMask(coords.u, coords.v);
    }

    isInsideBounds(x, z, margin = 0.0) {
        const radius = Math.max(0.0, this.width * 0.5 - margin);
        return x * x + z * z <= radius * radius;
    }

    isTrampledArea(x, z, threshold = 0.5) {
        return this.getTrampledMaskAt(x, z) >= threshold;
    }

    isRoadEdge(x, z, low = 0.05, high = 0.45) {
        const m = this.getTrampledMaskAt(x, z);
        return m >= low && m < high;
    }

    getHeightAt(x, z, nScale = 5.0) {
        const u = this.clamp01((x + this.radius) / this.width);
        const v = this.clamp01((z + this.radius) / this.width);

        const gridX = u * this.nrDivs;
        const gridZ = v * this.nrDivs;
        const cellX = Math.min(this.nrDivs - 1, Math.floor(gridX));
        const cellZ = Math.min(this.nrDivs - 1, Math.floor(gridZ));
        const tx = gridX - cellX;
        const tz = gridZ - cellZ;

        const vertexHeight = (i, j) => {
            const vx = i * this.step - this.radius;
            const vz = j * this.step - this.radius;
            if (vx * vx + vz * vz > this.radius * this.radius) return 0.0;
            const tu = vx / this.width + 0.5;
            const tv = vz / this.width + 0.5;
            const prairie = this.fbm2(tu * 4.0, tv * 4.0);
            const road = this.roadMask(tu, tv);
            const roadH = this.roadHeightAt(tu, tv);
            const elevation = prairie * (1.0 - road) + roadH * road;
            return elevation * nScale;
        };

        const h00 = vertexHeight(cellX, cellZ);
        const h10 = vertexHeight(cellX + 1, cellZ);
        const h01 = vertexHeight(cellX, cellZ + 1);
        const h11 = vertexHeight(cellX + 1, cellZ + 1);

        if (tx + tz <= 1.0) {
            return h00 * (1.0 - tx - tz) + h01 * tz + h10 * tx;
        }

        const w00 = 1.0 - tx;
        const w11 = tx + tz - 1.0;
        const w10 = 1.0 - tz;
        return h01 * w00 + h11 * w11 + h10 * w10;
    }
}
