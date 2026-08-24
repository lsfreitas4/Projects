import {CGFappearance} from '../lib/CGF.js';
import {MyDiamond} from './MyDiamond.js';
import {MyParallelogram} from './MyParallelogram.js';
import {MyTriangle} from './MyTriangle.js';
import {MyTriangleSmall} from './MyTriangleSmall.js';
/**
 * MyTriangle
 * @constructor
 * @param scene - Reference to MyScene object
 */
export class MyTangram {
    constructor(scene) {
        this.scene = scene;

        this.diamond = new MyDiamond(scene);
        this.parallelogram = new MyParallelogram(scene);
        this.triangle = new MyTriangle(scene);
        this.triangleSmall = new MyTriangleSmall(scene);

        this.initMaterials();
    }

    initMaterials() {
        this.greenMaterial = new CGFappearance(this.scene);
        this.greenMaterial.setAmbient(0.08, 0.20, 0.08, 1.0);
        this.greenMaterial.setDiffuse(0.15, 0.55, 0.20, 1.0);
        this.greenMaterial.setSpecular(0.95, 0.95, 0.95, 1.0);
        this.greenMaterial.setShininess(120.0);

        this.purpleMaterial = new CGFappearance(this.scene);
        this.purpleMaterial.setAmbient(0.16, 0.08, 0.20, 1.0);
        this.purpleMaterial.setDiffuse(0.55, 0.20, 0.70, 1.0);
        this.purpleMaterial.setSpecular(0.95, 0.95, 0.95, 1.0);
        this.purpleMaterial.setShininess(120.0);

        this.redMaterial = new CGFappearance(this.scene);
        this.redMaterial.setAmbient(0.20, 0.08, 0.08, 1.0);
        this.redMaterial.setDiffuse(0.75, 0.18, 0.15, 1.0);
        this.redMaterial.setSpecular(0.95, 0.95, 0.95, 1.0);
        this.redMaterial.setShininess(120.0);

        this.yellowMaterial = new CGFappearance(this.scene);
        this.yellowMaterial.setAmbient(0.20, 0.18, 0.06, 1.0);
        this.yellowMaterial.setDiffuse(0.95, 0.82, 0.20, 1.0);
        this.yellowMaterial.setSpecular(0.95, 0.95, 0.95, 1.0);
        this.yellowMaterial.setShininess(120.0);

        this.blueMaterial = new CGFappearance(this.scene);
        this.blueMaterial.setAmbient(0.07, 0.12, 0.20, 1.0);
        this.blueMaterial.setDiffuse(0.20, 0.40, 0.85, 1.0);
        this.blueMaterial.setSpecular(0.95, 0.95, 0.95, 1.0);
        this.blueMaterial.setShininess(120.0);

        this.orangeMaterial = new CGFappearance(this.scene);
        this.orangeMaterial.setAmbient(0.20, 0.12, 0.06, 1.0);
        this.orangeMaterial.setDiffuse(0.95, 0.52, 0.18, 1.0);
        this.orangeMaterial.setSpecular(0.95, 0.95, 0.95, 1.0);
        this.orangeMaterial.setShininess(120.0);

        this.pinkMaterial = new CGFappearance(this.scene);
        this.pinkMaterial.setAmbient(0.20, 0.10, 0.14, 1.0);
        this.pinkMaterial.setDiffuse(0.92, 0.45, 0.62, 1.0);
        this.pinkMaterial.setSpecular(0.95, 0.95, 0.95, 1.0);
        this.pinkMaterial.setShininess(120.0);
    }

    updateBuffers(complexity) {
        // Tangram não depende de complexidade
    }

    enableNormalViz() {
        this.diamond.enableNormalViz();
        this.parallelogram.enableNormalViz();
        this.triangle.enableNormalViz();
        this.triangleSmall.enableNormalViz();
    }

    disableNormalViz() {
        this.diamond.disableNormalViz();
        this.parallelogram.disableNormalViz();
        this.triangle.disableNormalViz();
        this.triangleSmall.disableNormalViz();
    }


    display() {
        this.scene.pushMatrix();

        var m = [
            Math.cos(Math.PI/4), Math.sin(Math.PI/4), 0.0, 0.0,
            -Math.sin(Math.PI/4), Math.cos(Math.PI/4), 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0
        ];

        this.scene.multMatrix(m);

        this.scene.customMaterial.apply();
        this.diamond.display();

        this.scene.popMatrix();

        
        // Triangle Roxo

        this.scene.pushMatrix();

        this.scene.scale(0.7, 0.7, 0.7);
        this.scene.rotate(-Math.PI/2, 0, 0, 1);
        this.scene.translate(2,-2, 0)
        this.purpleMaterial.apply();
        this.triangle.display();

        this.scene.popMatrix();


        //Triangle Vermelho

        this.scene.pushMatrix();

        this.scene.scale(0.7, 0.7, 0.7);
        this.scene.rotate(Math.PI/2, 0, 0, 1);
        this.scene.translate(0, 2, 0)
    
        
        this.redMaterial.apply();
        this.triangle.display();
        
        this.scene.popMatrix();


        //Diamond Amarelo
        this.scene.pushMatrix();

        this.scene.scale(1, 1, 1)
        this.scene.rotate(Math.PI, 1, 0, 0);
        this.scene.rotate(Math.PI/4, 0, 0, 1);
        this.scene.translate(0, -1, 0);

        this.yellowMaterial.apply();
        this.parallelogram.display();

        this.scene.popMatrix();


        //Triangle Azul

        this.scene.pushMatrix();

        this.scene.translate(0, 1.7, 0);
        this.scene.rotate(Math.PI/2, 0, 0, 1);


        this.blueMaterial.apply();
        this.triangle.display();

        this.scene.popMatrix();



        //Triangle Laranja

        this.scene.pushMatrix();

        this.scene.translate(0, 1.7, 0);
        this.scene.rotate(Math.PI+ Math.PI/2, 0, 0, 1);


        this.orangeMaterial.apply();
        this.triangle.display();

        this.scene.popMatrix();


        //Triangle Rosa

        this.scene.pushMatrix();

        this.scene.translate(0, 2.7, 0);
        
        this.pinkMaterial.apply();
        this.triangleSmall.display();

        this.scene.popMatrix();
    }
}