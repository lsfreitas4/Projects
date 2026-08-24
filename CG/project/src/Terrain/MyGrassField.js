import { CGFobject, CGFshader } from '../../../lib/CGF.js';
import { MyGrassPatch } from './MyGrassPatch.js';


export class MyGrassField extends CGFobject {
    constructor(scene, terrain, terrainScale = 5.0, patchCount = 130) {
        super(scene);

        this.terrain = terrain;
        this.terrainScale = terrainScale;
        this.patchCount = patchCount;

        this.denseThreshold = 0.45;
        this.deadThreshold = 0.70;

        this.baseLift = 0.08;
        this.maxPatchDistance = 120;
        this.edgeMargin = 0.6;
        this.windStrength = 0.4;
        this.windSpeed = 1.5;
        this.startTime = Date.now();

        this.shader = new CGFshader(
            scene.gl,
            'shaders/Flora/Grass/grass.vert',
            'shaders/Flora/Grass/grass.frag'
        );

        this.patches = [];
        this.patchMeshes = [];
        this.generatePatches();
    }

    fract(value) { return value - Math.floor(value); }

    random2(x, z) {
        return this.fract(Math.sin(x * 12.9898 + z * 78.233) * 43758.5453123);
    }

    noise2(x, z) {
        const ix = Math.floor(x);
        const iz = Math.floor(z);
        const fx = this.fract(x);
        const fz = this.fract(z);

        const a = this.random2(ix, iz);
        const b = this.random2(ix + 1.0, iz);
        const c = this.random2(ix, iz + 1.0);
        const d = this.random2(ix + 1.0, iz + 1.0);

        const ux = fx * fx * (3.0 - 2.0 * fx);
        const uz = fz * fz * (3.0 - 2.0 * fz);

        return a * (1.0 - ux) * (1.0 - uz)
            + b * ux * (1.0 - uz)
            + c * (1.0 - ux) * uz
            + d * ux * uz;
    }

    patchMask(worldX, worldZ) {
        const broad = this.noise2(worldX * 0.07, worldZ * 0.07);
        const detail = this.noise2(worldX * 0.28, worldZ * 0.28);
        return Math.min(1.0, Math.max(0.0, broad * 0.75 + detail * 0.25));
    }

    getPatchDensity(worldX, worldZ) {
        const score = this.patchMask(worldX, worldZ);
        if (score >= this.deadThreshold) return 'dead';
        if (score < this.denseThreshold) return 'dense';
        return 'normal';
    }

    generatePatches() {
        const terrainHalf = this.terrain.width * 0.5;
        const borderMargin = 2.0;
        const spawnHalf = Math.max(0.0, terrainHalf - borderMargin);
        this.patches = [];

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

                const density = this.getPatchDensity(x, z);

                let bladeMin = 300;
                let bladeMax = 500;
                let radius = 10.0;
                let scaleMin = 0.8;
                let scaleMax = 1.05;

                if (density === 'dead') {
                    bladeMin = 100;
                    bladeMax = 300;
                    radius = 10.0;
                    scaleMin = 0.55;
                    scaleMax = 0.85;
                } else if (density === 'dense') {
                    bladeMin = 500;
                    bladeMax = 800;
                    radius = 10.0;
                    scaleMin = 0.95;
                    scaleMax = 1.35;
                }

                const bladeCount = Math.floor(
                    bladeMin + this.random2(patchIndex * 4.73, patchIndex * 9.42) * (bladeMax - bladeMin + 1)
                );
                const blades = [];

                for (let b = 0; b < bladeCount; b++) {
                    const seedA = patchIndex * 73.17 + b * 13.91;
                    const seedB = patchIndex * 29.51 + b * 37.33;

                    const rr = Math.sqrt(this.random2(seedA, seedB)) * radius;
                    const angle = this.random2(seedA + 4.9, seedB + 7.3) * Math.PI * 2.0;

                    const localX = Math.cos(angle) * rr;
                    const localZ = Math.sin(angle) * rr;
                    const rotY = this.random2(seedA + 8.2, seedB + 6.1) * Math.PI * 2.0;
                    const scale = scaleMin + this.random2(seedA + 2.6, seedB + 1.4) * (scaleMax - scaleMin);

                    const worldX = x + localX;
                    const worldZ = z + localZ;
                    if (!this.terrain.isInsideBounds(worldX, worldZ, this.edgeMargin)) continue;
                    if (this.terrain.isTrampledArea(worldX, worldZ)) continue;

                    blades.push({ worldX, worldZ, rotY, scale });
                }

                const lodBlades = this.applyLodToBlades(x, z, blades);
                if (lodBlades.length === 0) {
                    patchIndex++;
                    continue;
                }

                this.patches.push({ x, z, density, radius, blades });
                const patchMesh = new MyGrassPatch(this.scene, lodBlades);
                this.patchMeshes.push({ x, z, radius, mesh: patchMesh });

                patchIndex++;
            }
        }
    }

    applyLodToBlades(patchX, patchZ, blades) {
        const step = Math.max(1, Math.floor(blades.length / 200));
        const lodBlades = [];
        for (let i = 0; i < blades.length; i += step) {
            lodBlades.push(blades[i]);
        }
        return lodBlades;
    }

    display() {
        const gl = this.scene.gl;
        gl.disable(gl.CULL_FACE);

        const camPos = this.scene.camera.position;
        const camX = camPos[0];
        const camZ = camPos[2];

        const prevShader = this.scene.activeShader;
        this.scene.setActiveShader(this.shader);

        const timeFactor = (Date.now() - this.startTime) / 1000;

        this.shader.setUniformsValues({
            nScale: this.terrainScale,
            uTerrainWidth: this.terrain.width,
            uBaseLift: this.baseLift,
            uDenseThreshold: this.denseThreshold,
            uDeadThreshold: this.deadThreshold,
            timeFactor,
            uWindStrength: this.windStrength,
            uWindSpeed: this.windSpeed
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
        gl.enable(gl.CULL_FACE);
    }
}