import { CGFobject, CGFshader } from '../../../lib/CGF.js';
import { MyFlowerPatch } from './MyFlowerPatch.js';


export class MyFlowerField extends CGFobject {
    constructor(scene, terrain, terrainScale = 5.0, patchCount = 60) {
        super(scene);

        this.terrain = terrain;
        this.terrainScale = terrainScale;
        this.patchCount = patchCount;


        this.flowerMin = 20;
        this.flowerMax = 30;
        this.patchRadius = 10.0;
        this.scaleMin = 0.7;
        this.scaleMax = 1.0;

        this.baseLift = 0.05;
        this.edgeMargin = 0.6;
        this.maxPatchDistance = 120;

        this.patchMeshes = null;

        this.shader = new CGFshader(
            scene.gl,
            'shaders/Flora/Flower/flower.vert',
            'shaders/Flora/Flower/flower.frag'
        );
    }

    fract(value) { return value - Math.floor(value); }

    random2(x, z) {
        return this.fract(Math.sin(x * 12.9898 + z * 78.233) * 43758.5453123);
    }

    generatePatches() {
        const terrainHalf = this.terrain.width * 0.5;
        const borderMargin = 2.0;
        const spawnHalf = Math.max(0.0, terrainHalf - borderMargin);

        const gridSize = Math.max(2, Math.round(Math.sqrt(this.patchCount)));
        const step = (spawnHalf * 2.0) / (gridSize - 1);
        let patchIndex = 0;

        for (let gx = 0; gx < gridSize; gx++) {
            for (let gz = 0; gz < gridSize; gz++) {
                const centerX = -spawnHalf + gx * step;
                const centerZ = -spawnHalf + gz * step;

                const jitterX = (this.random2(gx * 11.31, gz * 7.17) - 0.5) * step * 0.35;
                const jitterZ = (this.random2(gx * 5.93, gz * 13.47) - 0.5) * step * 0.35;

                const x = Math.max(-spawnHalf, Math.min(spawnHalf, centerX + jitterX));
                const z = Math.max(-spawnHalf, Math.min(spawnHalf, centerZ + jitterZ));

                const flowerCount = Math.floor(
                    this.flowerMin
                    + this.random2(patchIndex * 4.73, patchIndex * 9.42)
                    * (this.flowerMax - this.flowerMin + 1)
                );

                const flowers = [];
                for (let f = 0; f < flowerCount; f++) {
                    const seedA = patchIndex * 73.17 + f * 13.91;
                    const seedB = patchIndex * 29.51 + f * 37.33;

                    const rr = Math.sqrt(this.random2(seedA, seedB)) * this.patchRadius;
                    const angle = this.random2(seedA + 4.9, seedB + 7.3) * Math.PI * 2.0;

                    const localX = Math.cos(angle) * rr;
                    const localZ = Math.sin(angle) * rr;
                    const rotY = this.random2(seedA + 8.2, seedB + 6.1) * Math.PI * 2.0;
                    const scale = this.scaleMin
                        + this.random2(seedA + 2.6, seedB + 1.4) * (this.scaleMax - this.scaleMin);

                    const worldX = x + localX;
                    const worldZ = z + localZ;
                    if (!this.terrain.isInsideBounds(worldX, worldZ, this.edgeMargin)) continue;
                    if (this.terrain.isTrampledArea(worldX, worldZ)) continue;

                    flowers.push({ worldX, worldZ, rotY, scale });
                }

                if (flowers.length === 0) {
                    patchIndex++;
                    continue;
                }
                const patchMesh = new MyFlowerPatch(this.scene, flowers);
                this.patchMeshes.push({ x, z, radius: this.patchRadius, mesh: patchMesh });
                patchIndex++;
            }
        }
    }

    display() {
        if (!this.patchMeshes) {
            this.patchMeshes = [];
            this.generatePatches();
        }

        const camPos = this.scene.camera.position;
        const camX = camPos[0];
        const camZ = camPos[2];

        const prevShader = this.scene.activeShader;
        this.scene.setActiveShader(this.shader);

        this.shader.setUniformsValues({
            nScale: this.terrainScale,
            uTerrainWidth: this.terrain.width,
            uBaseLift: this.baseLift
        });

        for (const patchData of this.patchMeshes) {
            const dx = patchData.x - camX;
            const dz = patchData.z - camZ;
            const patchDistSq = dx * dx + dz * dz;
            const cullingRadius = this.maxPatchDistance + patchData.radius;

            if (patchDistSq > cullingRadius * cullingRadius) continue;

            patchData.mesh.display();
        }

        this.scene.setActiveShader(prevShader);
    }
}