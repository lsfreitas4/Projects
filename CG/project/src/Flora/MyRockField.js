import { CGFobject, CGFshader } from '../../../lib/CGF.js';
import { MyRock } from './MyRock.js';


export class MyRockField extends CGFobject {
    constructor(scene, terrain, terrainScale = 5.0, rockCount = 40) {
        super(scene);
        this.terrain = terrain;
        this.terrainScale = terrainScale;
        this.rockCount = rockCount;

        this.maxDistance = 130;
        this.rockMesh = new MyRock(scene);

        this.shader = new CGFshader(
            scene.gl,
            'shaders/Enviroment/Rock/rock.vert',
            'shaders/Enviroment/Rock/rock.frag'
        );

        this.roadRockChance = 0.2;

        this.rocks = [];
        this.generateRocks();
    }

    fract(v) { return v - Math.floor(v); }
    random(x, z) { return this.fract(Math.sin(x * 12.9898 + z * 78.233) * 43758.5453123); }

    generateRocks() {
        const halfW = this.terrain.width * 0.5;
        const spawnRange = halfW - 5.0;

        for (let i = 0; i < this.rockCount; i++) {
            let seedX = this.random(i * 15.3, i * 37.1);
            let seedZ = this.random(i * 73.4, i * 91.5);

            let x = -spawnRange + seedX * (spawnRange * 2);
            let z = -spawnRange + seedZ * (spawnRange * 2);

            if (!this.terrain.isInsideBounds(x, z, 2.0)) continue;
                if (this.terrain.isTrampledArea(x, z)) {
                const roadRoll = this.random(i * 8.71, i * 3.37);
                if (roadRoll > this.roadRockChance) continue;
            }


            let scaleX = 0.6 + this.random(i * 5.1, x) * 1.5;
            let scaleY = 0.4 + this.random(z, i * 2.3) * 1.0;
            let scaleZ = 0.6 + this.random(x, z) * 1.5;
            let rotation = this.random(i * 4.4, seedX) * Math.PI * 2.0;

            this.rocks.push({
                x: x, z: z,
                sx: scaleX, sy: scaleY, sz: scaleZ,
                rotY: rotation,
                seed: seedX * 10.0
            });
        }
    }

    display() {
        const gl = this.scene.gl;
        gl.disable(gl.CULL_FACE);


        const camPos = this.scene.camera.position;
        const cx = camPos[0];
        const cz = camPos[2];

        const prevShader = this.scene.activeShader;
        this.scene.setActiveShader(this.shader);

        this.shader.setUniformsValues({
            uTerrainScale: this.terrainScale,
            uTerrainWidth: this.terrain.width
        });

        for (const r of this.rocks) {
            let dx = r.x - cx;
            let dz = r.z - cz;
            if (dx * dx + dz * dz > this.maxDistance * this.maxDistance) continue;

            this.scene.pushMatrix();

            this.scene.translate(r.x, 0, r.z);
            this.scene.rotate(r.rotY, 0, 1, 0);
            this.scene.scale(r.sx, r.sy, r.sz);

            this.shader.setUniformsValues({
                uRockSeed: r.seed,
                uRockWorldXZ: [r.x, r.z]
            });

            this.rockMesh.display();
            this.scene.popMatrix();
        }

        this.scene.setActiveShader(prevShader);
        gl.enable(gl.CULL_FACE);
    }
}