import {CGFobject} from '../lib/CGF.js';
/**
 * MyTriangle
 * @constructor
 * @param scene - Reference to MyScene object
 */
export class MyUnitCubeQuad{
    constructor(scene) {
        this.scene = scene;
    }

    display(){

        // Base Cima

        this.scene.pushMatrix();

        this.scene.quad.display();

        this.scene.popMatrix();
        
        // Lado 1 

        this.scene.pushMatrix();

        this.scene.rotate(Math.PI/2, 1, 0, 0);
        this.scene.translate(0, -0.5, 0.5)

        this.scene.quad.display();

        this.scene.popMatrix();


        // Lado 2

        this.scene.pushMatrix();

        this.scene.rotate(Math.PI/2, 0, 0, 1);
        this.scene.translate(-0.5, -0.5, 0)

        this.scene.quad.display();

        this.scene.popMatrix();

        // Lado 3

        this.scene.pushMatrix();

        this.scene.rotate(Math.PI/2, 0, 0, 1);
        this.scene.translate(-0.5, 0.5, 0)

        this.scene.quad.display();

        this.scene.popMatrix();
        
        // Lado 4

        this.scene.pushMatrix();

        this.scene.rotate(Math.PI/2, 1, 0, 0);
        this.scene.translate(0, 0.5, 0.5)

        this.scene.quad.display();

        this.scene.popMatrix();

        // Lado 4

        this.scene.pushMatrix();

        this.scene.translate(0, -1, 0)

        this.scene.quad.display();

        this.scene.popMatrix();
        
    }

}
