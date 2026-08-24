import { CGFobject, CGFtexture, CGFappearance, CGFaxis, CGFcamera } from '../../../../lib/CGF.js';
import { MyWheels } from './Wheels/MyWheels.js';
import { MyBed } from './Bed/MyBed.js';
import { MyCover } from './Cover/MyCover.js';
import { CGFobjModel } from "../../../../lib/extra/CGFobjModel.js";

export class MyWagon extends CGFobject {

    constructor(scene) {
        super(scene);

        this.myPosition = vec3.fromValues(0, 0, -75);
        this.yaw = 0

        this.speed = 0;
        this.maxSpeed = 5;
        this.acceleration = 6;
        this.brakeDecel = 10;
        this.friction = 6;
        this.turnSpeed = 1.6;

        this.steerAngle = 0;
        this.maxSteer = 0.5;
        this.steerSpeed = 4.0;
        this.steerReturn = 4.0;

        this.confineToRoad = true;
        this.roadThreshold = 0.3;
        this.baleBlockRadius = 2.5;
        this.rockPad = 0.3;

        this.bodyFront = 14;
        this.bodyRear = 1;
        this.bodyHalfWidth = 1;

        this.initParts();
        this.initMaterials();

    }

    initParts() {
        this.wheelsLeft = new MyWheels(this.scene);
        this.wheelsRight = new MyWheels(this.scene);
        this.bed = new MyBed(this.scene);
        this.cover = new MyCover(this.scene);
        this.horse = new CGFobjModel(this.scene, "Models/horse.obj");
    }

    initMaterials() {
        this.horseMaterial = new CGFappearance(this.scene);
        this.horseMaterial.setAmbient(0.25, 0.15, 0.10, 1);
        this.horseMaterial.setDiffuse(0.45, 0.30, 0.20, 1);
        this.horseMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.horseMaterial.setShininess(10);
    }

    update(dt) {
        if (!dt || dt > 0.1) dt = 0.016;

        const gui = this.scene.gui;
        const fwd = gui.isKeyPressed("KeyW");
        const back = gui.isKeyPressed("KeyS");
        const left = gui.isKeyPressed("KeyA");
        const right = gui.isKeyPressed("KeyD");

        if (fwd && !back) {
            this.speed += this.acceleration * dt;
        } else if (back && !fwd) {
            this.speed -= this.brakeDecel * dt;
        } else {
            this.speed -= this.friction * dt;
        }

        if (this.speed < 0) this.speed = 0;
        if (this.speed > this.maxSpeed) this.speed = this.maxSpeed;

        const speedFactor = Math.min(1, this.speed / 3);

        let turnInput = 0;
        if (left && !right) {
            this.yaw += this.turnSpeed * dt * speedFactor;
            turnInput = -1;
        }
        if (right && !left) {
            this.yaw -= this.turnSpeed * dt * speedFactor;
            turnInput = 1;
        }

        const targetSteer = -turnInput * this.maxSteer;
        if (turnInput !== 0) {
            this.steerAngle += (targetSteer - this.steerAngle) * Math.min(1, this.steerSpeed * dt);
        } else {
            this.steerAngle += (0 - this.steerAngle) * Math.min(1, this.steerReturn * dt);
        }

        const dx = Math.sin(this.yaw) * this.speed * dt;
        const dz = Math.cos(this.yaw) * this.speed * dt;
        this.resolveMovement(dx, dz);

        const wheelRadius = 1.0;
        const trackWidth = 2.0;
        const baseAngular = this.speed * dt / wheelRadius;

        const turnRate = this.turnSpeed * speedFactor * turnInput;
        const leftLinearAdjust = -turnRate * trackWidth * dt / wheelRadius;
        const rightLinearAdjust = turnRate * trackWidth * dt / wheelRadius;

        this.wheelsLeft.angle -= baseAngular + leftLinearAdjust;
        this.wheelsRight.angle += baseAngular + rightLinearAdjust;
    }

    bodyPointsLocal() {
        const W = this.bodyHalfWidth;
        const F = this.bodyFront;
        const R = this.bodyRear;
        return [
            [0, 0],
            [0, F], [0, -R],
            [W, F], [-W, F], [W, -R], [-W, -R],
            [W, 0], [-W, 0],
            [W, F * 0.5], [-W, F * 0.5],
            [W, -R * 0.5], [-W, -R * 0.5],
            [W * 0.5, F], [-W * 0.5, F],
            [W * 0.5, -R], [-W * 0.5, -R]
        ];
    }
 

    bodyPointsWorld(cx, cz) {
        const cos = Math.cos(this.yaw);
        const sin = Math.sin(this.yaw);
        const pts = [];
        for (const [lx, lz] of this.bodyPointsLocal()) {
            const ox = lx * cos + lz * sin;
            const oz = -lx * sin + lz * cos;
            pts.push([cx + ox, cz + oz]);
        }
        return pts;
    }
 
    pointInBale(px, pz) {
        const scene = this.scene;
        if (!scene || !scene.bales || typeof scene.bales.getActiveBales !== 'function') return false;
        const r2 = this.baleBlockRadius * this.baleBlockRadius;
        for (const b of scene.bales.getActiveBales()) {
            const ddx = b.position[0] - px;
            const ddz = b.position[2] - pz;
            if (ddx * ddx + ddz * ddz < r2) return true;
        }
        return false;
    }
 
    pointInRock(px, pz) {
        const scene = this.scene;
        if (!scene || !scene.rockField || !scene.rockField.rocks) return false;
        for (const r of scene.rockField.rocks) {
            const footprint = Math.max(r.sx || 1, r.sz || 1) + this.rockPad;
            const ddx = r.x - px;
            const ddz = r.z - pz;
            if (ddx * ddx + ddz * ddz < footprint * footprint) return true;
        }
        return false;
    }
 
    roadMaskAt(x, z) {
        const t = this.scene ? this.scene.terrain : null;
        return t ? t.getTrampledMaskAt(x, z) : 1.0;
    }
 
    canBeAt(x, z, currentMask) {
        const pts = this.bodyPointsWorld(x, z);
        for (const [px, pz] of pts) {
            if (this.pointInBale(px, pz)) return false;
            if (this.pointInRock(px, pz)) return false;
            if (this.confineToRoad) {
                const m = this.roadMaskAt(px, pz);
                if (m < this.roadThreshold && m <= currentMask) return false;
            }
        }
        return true;
    }
 
    resolveMovement(dx, dz) {
        const x0 = this.myPosition[0];
        const z0 = this.myPosition[2];
        const currentMask = this.roadMaskAt(x0, z0);
 
        if (this.canBeAt(x0 + dx, z0 + dz, currentMask)) {
            this.myPosition[0] = x0 + dx;
            this.myPosition[2] = z0 + dz;
        } else if (this.canBeAt(x0 + dx, z0, currentMask)) {
            this.myPosition[0] = x0 + dx;   
            this.speed *= 0.6;
        } else if (this.canBeAt(x0, z0 + dz, currentMask)) {
            this.myPosition[2] = z0 + dz;   
            this.speed *= 0.6;
        } else {
            this.speed = 0;                
        }
    }





    display() {

        this.bed.display(this.steerAngle);

        //Cover
        this.scene.pushMatrix();
        this.scene.translate(0, 2, -3);
        this.cover.display();
        this.scene.popMatrix();

        // Esquerdo Tras
        this.scene.pushMatrix();
        this.scene.rotate(-Math.PI / 2, 0, 1, 0);
        this.scene.translate(-2, -0.2, 2.15);
        this.wheelsLeft.display();
        this.scene.popMatrix();

        // Esquerdo Frente
        this.scene.pushMatrix();
        this.scene.rotate(-Math.PI / 2, 0, 1, 0);
        this.scene.translate(2, -0.2, 2.15);
        this.wheelsLeft.display();
        this.scene.popMatrix();

        // Direito Tras
        this.scene.pushMatrix();
        this.scene.rotate(Math.PI / 2, 0, 1, 0);
        this.scene.translate(2, -0.2, 2.15);
        this.wheelsRight.display();
        this.scene.popMatrix();

        // Direito Frente
        this.scene.pushMatrix();
        this.scene.rotate(Math.PI / 2, 0, 1, 0);
        this.scene.translate(-2, -0.2, 2.15);
        this.wheelsRight.display();
        this.scene.popMatrix();


        this.scene.pushMatrix();

        this.scene.translate(0, 0, 2);
        this.scene.rotate(this.steerAngle, 0, 1, 0);
        this.scene.translate(0, 0, -2);


        this.scene.translate(0, -1.2, 0.5);
        this.scene.rotate(-Math.PI / 2, 1, 0, 0);
        this.scene.translate(0, -9.8, 0);
        this.scene.scale(0.004, 0.004, 0.004);
        this.horseMaterial.apply();
        this.horse.display();
        this.scene.popMatrix();

    }

    enableNormalViz() {
        this.wheelsLeft.enableNormalViz();
        this.wheelsRight.enableNormalViz();
        this.bed.enableNormalViz();
        this.cover.enableNormalViz();
    }

    disableNormalViz() {
        this.wheelsLeft.disableNormalViz();
        this.wheelsRight.disableNormalViz();
        this.bed.disableNormalViz();
        this.cover.disableNormalViz();
    }

}