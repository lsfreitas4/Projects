import {CGFappearance, CGFtexture} from '../lib/CGF.js';
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
        this.trianglePurple = new MyTriangle(scene);
        this.triangleRed = new MyTriangle(scene);
        this.triangleBlue = new MyTriangle(scene);
        this.triangleOrange = new MyTriangle(scene);
        this.triangleSmall = new MyTriangleSmall(scene);

        this.initMaterials();
    }

    initMaterials(){
        this.tangramTexture = new CGFtexture(this.scene, 'images/tangram.png');

        this.greenMaterial = new CGFappearance(this.scene);
        this.greenMaterial.setAmbient(0.1, 0.1, 0.1, 1);
        this.greenMaterial.setDiffuse(0.9, 0.9, 0.9, 1);
        this.greenMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.greenMaterial.setShininess(10.0);
        this.greenMaterial.setTexture(this.tangramTexture);
        this.greenMaterial.setTextureWrap('REPEAT', 'REPEAT');

        this.purpleMaterial = new CGFappearance(this.scene);
        this.purpleMaterial.setAmbient(0.1, 0.1, 0.1, 1);
        this.purpleMaterial.setDiffuse(0.9, 0.9, 0.9, 1);
        this.purpleMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.purpleMaterial.setShininess(10.0);
        this.purpleMaterial.setTexture(this.tangramTexture);
        this.purpleMaterial.setTextureWrap('REPEAT', 'REPEAT');

        this.redMaterial = new CGFappearance(this.scene);
        this.redMaterial.setAmbient(0.1, 0.1, 0.1, 1);
        this.redMaterial.setDiffuse(0.9, 0.9, 0.9, 1);
        this.redMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.redMaterial.setShininess(10.0);
        this.redMaterial.setTexture(this.tangramTexture);
        this.redMaterial.setTextureWrap('REPEAT', 'REPEAT');

        this.yellowMaterial = new CGFappearance(this.scene);
        this.yellowMaterial.setAmbient(0.1, 0.1, 0.1, 1);
        this.yellowMaterial.setDiffuse(0.9, 0.9, 0.9, 1);
        this.yellowMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.yellowMaterial.setShininess(10.0);
        this.yellowMaterial.setTexture(this.tangramTexture);
        this.yellowMaterial.setTextureWrap('REPEAT', 'REPEAT');

        this.blueMaterial = new CGFappearance(this.scene);
        this.blueMaterial.setAmbient(0.1, 0.1, 0.1, 1);
        this.blueMaterial.setDiffuse(0.9, 0.9, 0.9, 1);
        this.blueMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.blueMaterial.setShininess(10.0);
        this.blueMaterial.setTexture(this.tangramTexture);
        this.blueMaterial.setTextureWrap('REPEAT', 'REPEAT');

        this.orangeMaterial = new CGFappearance(this.scene);
        this.orangeMaterial.setAmbient(0.1, 0.1, 0.1, 1);
        this.orangeMaterial.setDiffuse(0.9, 0.9, 0.9, 1);
        this.orangeMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.orangeMaterial.setShininess(10.0);
        this.orangeMaterial.setTexture(this.tangramTexture);
        this.orangeMaterial.setTextureWrap('REPEAT', 'REPEAT');

        this.pinkMaterial = new CGFappearance(this.scene);
        this.pinkMaterial.setAmbient(0.1, 0.1, 0.1, 1);
        this.pinkMaterial.setDiffuse(0.9, 0.9, 0.9, 1);
        this.pinkMaterial.setSpecular(0.1, 0.1, 0.1, 1);
        this.pinkMaterial.setShininess(10.0);
        this.pinkMaterial.setTexture(this.tangramTexture);
        this.pinkMaterial.setTextureWrap('REPEAT', 'REPEAT');


    }

    updateBuffers(complexity) {
        // Tangram não depende de complexidade
    }


    enableNormalViz() {
        this.diamond.enableNormalViz();
        this.parallelogram.enableNormalViz();
        this.trianglePurple.enableNormalViz();
        this.triangleRed.enableNormalViz();
        this.triangleBlue.enableNormalViz();
        this.triangleOrange.enableNormalViz();
        this.triangleSmall.enableNormalViz();
    }


    disableNormalViz() {
        this.diamond.disableNormalViz();
        this.parallelogram.disableNormalViz();
        this.trianglePurple.disableNormalViz();
        this.triangleRed.disableNormalViz();
        this.triangleBlue.disableNormalViz();
        this.triangleOrange.disableNormalViz();
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
        this.greenMaterial.apply();
        this.diamond.display();

        this.scene.popMatrix();

        
        // Triangle Roxo

        this.scene.pushMatrix();

        this.scene.scale(0.7, 0.7, 0.7);
        this.scene.rotate(-Math.PI/2, 0, 0, 1);
        this.scene.translate(2,-2, 0)
        this.purpleMaterial.apply();
        this.trianglePurple.updateTexCoords([
            0.0, 0.0,
            0.25, 0.25,
            0.0, 0.5,

            0.0, 0.0,
            0.25, 0.25,
            0.0, 0.5,

        ]);
        this.trianglePurple.display();

        this.scene.popMatrix();


        //Triangle Vermelho

        this.scene.pushMatrix();

        this.scene.scale(0.7, 0.7, 0.7);
        this.scene.rotate(Math.PI/2, 0, 0, 1);
        this.scene.translate(0, 2, 0)
    
        
        this.redMaterial.apply();
        this.triangleRed.updateTexCoords([
            0.5, 0.5,
            0.25, 0.75,
            0.75, 0.75,

            0.5, 0.5,
            0.25, 0.75,
            0.75, 0.75,
        ]);
        this.triangleRed.display();
        
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
        this.triangleBlue.updateTexCoords([
            0.0, 0.0,
            0.5, 0.5,
            1.0, 0.0,

            0.0, 0.0,
            0.5, 0.5,
            1.0, 0.0,
        ]);
        this.triangleBlue.display();

        this.scene.popMatrix();



        //Triangle Laranja

        this.scene.pushMatrix();

        this.scene.translate(0, 1.7, 0);
        this.scene.rotate(Math.PI+ Math.PI/2, 0, 0, 1);


        this.orangeMaterial.apply();
        this.triangleOrange.updateTexCoords([
            1.0, 0.0,
            0.5, 0.5,
            1.0, 1.0,

            1.0, 0.0,
            0.5, 0.5,
            1.0, 1.0,

        ]);
        this.triangleOrange.display();

        this.scene.popMatrix();


        //Triangle Rosa

        this.scene.pushMatrix();

        this.scene.translate(0, 2.7, 0);
        
        this.pinkMaterial.apply();
        this.triangleSmall.display();

        this.scene.popMatrix();
    }

}