import {CGFobject, CGFtexture, CGFappearance, CGFshader} from '../../../../lib/CGF.js';
import { MyWheelsTyre } from './MyWheelsTyre.js';
import { MyRing } from './MyRing.js';
import { MyScrew } from './MyScrew.js';

export class MyWheels extends CGFobject {

    constructor(scene, angle = 0){
        super(scene);
        this.initParts();
        this.initMaterials();
        this.angle = angle;
    }

    initParts() {
        this.tyre = new MyWheelsTyre(this.scene);
        this.ring = new MyRing(this.scene);
        this.screw = new MyScrew(this.scene);
    }

    initMaterials() {
        this.tyreTexture = new CGFtexture(this.scene, 'textures/Wagon/Wheels.jpg');
        this.tyreMaterial = new CGFappearance(this.scene);
        this.tyreMaterial.setAmbient(0.2, 0.2, 0.2, 1);
        this.tyreMaterial.setDiffuse(0.4, 0.4, 0.4, 1);
        this.tyreMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.tyreMaterial.setShininess(10);
        this.tyreMaterial.setTexture(this.tyreTexture);
        this.tyreMaterial.setTextureWrap('REPEAT', 'REPEAT');
    }

    display() {
        this.scene.pushMatrix();
        this.scene.rotate(this.angle, 0, 0 ,1);
        this.tyreMaterial.apply();
        this.tyre.display();

        
        this.scene.pushMatrix();
            this.scene.scale(0.3, 0.9, 0.3);
            this.scene.translate(0, -0.9, 0.9);
            this.tyreMaterial.apply();
            this.ring.display();
        this.scene.popMatrix();

        this.scene.pushMatrix();
            this.scene.rotate(Math.PI/2, 0, 0, 1);
            this.scene.scale(0.3, 0.9, 0.3);
            this.scene.translate(0, -0.9, 0.9);
            this.tyreMaterial.apply();
            this.ring.display();
        this.scene.popMatrix();

        this.scene.pushMatrix();
            this.scene.rotate(Math.PI/4, 0, 0, 1);
            this.scene.scale(0.3, 0.9, 0.3);
            this.scene.translate(0, -0.9, 0.9);
            this.tyreMaterial.apply();
            this.ring.display();
        this.scene.popMatrix();

        this.scene.pushMatrix();
            this.scene.rotate(Math.PI/2 + Math.PI/4, 0, 0, 1);
            this.scene.scale(0.3, 0.9, 0.3);
            this.scene.translate(0, -0.9, 0.9);
            this.tyreMaterial.apply();
            this.ring.display();
        this.scene.popMatrix();
        
        this.scene.pushMatrix();
            this.scene.translate(0, 0, 0.3);
            this.scene.rotate(Math.PI/2, 1, 0, 0);
            this.tyreMaterial.apply();
            this.screw.display();
        this.scene.popMatrix();

        this.scene.popMatrix();
    }

    enableNormalViz() {
        this.tyre.enableNormalViz();
        this.ring.enableNormalViz();
        this.screw.enableNormalViz();
    }

    disableNormalViz() {
        this.tyre.disableNormalViz();
        this.ring.disableNormalViz();
        this.screw.disableNormalViz();
    }


}