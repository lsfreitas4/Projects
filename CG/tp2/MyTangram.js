import {CGFobject} from '../lib/CGF.js';
/**
 * MyTriangle
 * @constructor
 * @param scene - Reference to MyScene object
 */
export class MyTangram {
    constructor(scene) {
        this.scene = scene;
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

        this.scene.diamond.display();

        this.scene.popMatrix();

        
        // Triangle Roxo

        this.scene.pushMatrix();

        this.scene.scale(0.7, 0.7, 0.7);
        this.scene.rotate(-Math.PI/2, 0, 0, 1);
        this.scene.translate(2,-2, 0)
        this.scene.triangle.display();

        this.scene.popMatrix();


        //Triangle Vermelho

        this.scene.pushMatrix();

        this.scene.scale(0.7, 0.7, 0.7);
        this.scene.rotate(Math.PI/2, 0, 0, 1);
        this.scene.translate(0, 2, 0)
    
        
        this.scene.triangle.display();
        
        this.scene.popMatrix();


        //Diamond Amarelo
        this.scene.pushMatrix();

        this.scene.scale(1, 1, 1)
        this.scene.rotate(Math.PI, 1, 0, 0);
        this.scene.rotate(Math.PI/4, 0, 0, 1);
        this.scene.translate(0, -1, 0);

        this.scene.parallelogram.display();

        this.scene.popMatrix();


        //Triangle Azul

        this.scene.pushMatrix();

        this.scene.translate(0, 1.7, 0);
        this.scene.rotate(Math.PI/2, 0, 0, 1);


        this.scene.triangle.display();

        this.scene.popMatrix();



        //Triangle Laranja

        this.scene.pushMatrix();

        this.scene.translate(0, 1.7, 0);
        this.scene.rotate(Math.PI+ Math.PI/2, 0, 0, 1);


        this.scene.triangle.display();

        this.scene.popMatrix();


        //Triangle Rosa

        this.scene.pushMatrix();

        this.scene.translate(0, 2.7, 0);
        
        this.scene.triangleSmall.display();

        this.scene.popMatrix();
    }
}