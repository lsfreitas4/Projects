import {CGFobject, CGFtexture, CGFappearance} from '../../../../lib/CGF.js';
import {MyPlanks} from './MyPlanks.js'

export class MyBed extends CGFobject {

    constructor(scene){
        super(scene);
        this.initParts();
        this.initMaterials();

    }

    initParts() {
        this.lowerPlank = new MyPlanks(this.scene);
        this.lateralPlanks = new MyPlanks(this.scene);
        this.WheelsLig = new MyPlanks(this.scene);
        this.sit = new MyPlanks(this.scene);
        this.tongue = new MyPlanks(this.scene);
    }

    initMaterials() {
        this.plankTexture = new CGFtexture(this.scene, 'textures/Wagon/Wheels.jpg');
        this.plankMaterial = new CGFappearance(this.scene);
        this.plankMaterial.setAmbient(0.2, 0.2, 0.2, 1);
        this.plankMaterial.setDiffuse(0.4, 0.4, 0.4, 1);
        this.plankMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.plankMaterial.setShininess(10);
        this.plankMaterial.setTexture(this.plankTexture);
        this.plankMaterial.setTextureWrap('REPEAT', 'REPEAT');
    }

    display(steerAngle = 0) {
        this.scene.pushMatrix();
            this.scene.scale(4, 0.2, 6);
            this.plankMaterial.apply();
            this.lowerPlank.display();
        this.scene.popMatrix();

        //Lado Esquerdo
        this.scene.pushMatrix();
            this.scene.scale(0.2, 1, 6);
            this.scene.translate(-10, 0, 0);
            this.plankMaterial.apply();
            this.lateralPlanks.display();
        this.scene.popMatrix();
        
        //Lado Direito
        this.scene.pushMatrix();
            this.scene.scale(0.2, 1, 6);
            this.scene.translate(10, 0, 0);
            this.plankMaterial.apply();
            this.lateralPlanks.display();
        this.scene.popMatrix();
        
        //Tras
        this.scene.pushMatrix();
            this.scene.scale(4, 1, 0.2);
            this.scene.translate(0, 0, -14.5);
            this.plankMaterial.apply();
            this.lateralPlanks.display();
        this.scene.popMatrix();

        //Frente
        this.scene.pushMatrix();
            this.scene.scale(4, 0.7, 0.2);
            this.scene.translate(0, 0, 14.5);
            this.plankMaterial.apply();
            this.lateralPlanks.display();
        this.scene.popMatrix();

        //Frente
        this.scene.pushMatrix();
            this.scene.scale(4.5, 0.3, 0.5);
            this.scene.translate(0, -1.2, 4);
            this.plankMaterial.apply();
            this.WheelsLig.display();
        this.scene.popMatrix();


        //Tras
        this.scene.pushMatrix();
            this.scene.scale(4.5, 0.3, 0.5);
            this.scene.translate(0, -1.2, -4);
            this.plankMaterial.apply();
            this.WheelsLig.display();
        this.scene.popMatrix();

        //Chair
        this.scene.pushMatrix();
            this.scene.scale(4, 0.2, 0.7);
            this.scene.translate(0, 1, 4.5);
            this.plankMaterial.apply();
            this.sit.display();
        this.scene.popMatrix();

            this.scene.pushMatrix();
            this.scene.translate(0, 0, 2);
            this.scene.rotate(steerAngle, 0, 1, 0);
            this.scene.translate(0, 0, -2);
                //Tongue

                this.scene.pushMatrix();
                    this.scene.scale(1, 0.2, 5);
                    this.scene.translate(0, -1.2, 0.9);
                    this.plankMaterial.apply();
                    this.tongue.display();
                this.scene.popMatrix();

                //Tongue
                this.scene.pushMatrix();
                    this.scene.scale(4, 0.2, 0.7);
                    this.scene.translate(0, 0.8, 9.7);
                    this.plankMaterial.apply();
                    this.tongue.display();
                this.scene.popMatrix();

                //Tongue
                this.scene.pushMatrix();
                    this.scene.scale(0.5, 0.2, 2.5);
                    this.scene.translate(3.5, 0.8, 3.2);
                    this.plankMaterial.apply();
                    this.tongue.display();
                this.scene.popMatrix();

        this.scene.popMatrix();
    }  

    enableNormalViz() {
        this.lowerPlank.enableNormalViz();
        this.lateralPlanks.enableNormalViz();
        this.WheelsLig.enableNormalViz();
        this.sit.enableNormalViz();
        this.tongue.enableNormalViz();
    }

    disableNormalViz() {
        this.lowerPlank.disableNormalViz();
        this.lateralPlanks.disableNormalViz();
        this.WheelsLig.disableNormalViz();
        this.sit.disableNormalViz();
        this.tongue.disableNormalViz();
    }


}