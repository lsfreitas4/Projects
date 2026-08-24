import { CGFobject } from '../../../lib/CGF.js';
import { MyUnitCube } from './MyUnitCube.js';

export class MyBarn {
    constructor(scene) {
        this.scene = scene;
        this.baseCube = new MyUnitCube(scene);
        this.initRoofPrimitive();
    }

    initRoofPrimitive() {
        this.roofPrimitive = new CGFobject(this.scene);

        const sl = 1 / Math.sqrt(1.25);
        const nx = sl;
        const ny = 0.5 * sl;

        this.roofPrimitive.vertices = [

            -0.5, 0, 0.5, 0.5, 0, 0.5, 0.0, 1.0, 0.5,

            -0.5, 0, -0.5, 0.5, 0, -0.5, 0.0, 1.0, -0.5,

            -0.5, 0, 0.5, 0.0, 1.0, 0.5, 0.0, 1.0, -0.5, -0.5, 0, -0.5,

            0.5, 0, 0.5, 0.0, 1.0, 0.5, 0.0, 1.0, -0.5, 0.5, 0, -0.5
        ];

        this.roofPrimitive.indices = [
            0, 1, 2,
            4, 3, 5,
            6, 7, 8, 6, 8, 9,
            10, 12, 11, 10, 13, 12
        ];

        this.roofPrimitive.normals = [
            0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, -1, 0, 0, -1, 0, 0, -1,
            -nx, ny, 0, -nx, ny, 0, -nx, ny, 0, -nx, ny, 0,
            nx, ny, 0, nx, ny, 0, nx, ny, 0, nx, ny, 0
        ];

        this.roofPrimitive.texCoords = [
            0, 0, 1, 0, 0.5, 1,
            0, 0, 1, 0, 0.5, 1,
            0, 0, 0, 1, 1, 1, 1, 0,
            0, 0, 0, 1, 1, 1, 1, 0
        ];

        this.roofPrimitive.primitiveType = this.scene.gl.TRIANGLES;
        this.roofPrimitive.initGLBuffers();

    }

    display(barnShader, wallTex, roofTex, windowTex, doorTex) {

        // Cubo Principal
        this.scene.pushMatrix();
        this.scene.scale(10, 8, 14);
        this.scene.translate(0, 0.5, 0);
        wallTex.bind(0);
        this.baseCube.display();
        this.scene.popMatrix();

        // Porta
        this.scene.pushMatrix();

        this.scene.pushMatrix();
        this.scene.translate(-2.2, 3.0, 7.02);
        this.scene.scale(0.4, 6.0, 0.2);
        this.baseCube.display();
        this.scene.popMatrix();

        this.scene.pushMatrix();
        this.scene.translate(2.2, 3.0, 7.02);
        this.scene.scale(0.4, 6.0, 0.2);
        this.baseCube.display();
        this.scene.popMatrix();

        this.scene.pushMatrix();
        this.scene.translate(0, 6.2, 7.02);
        this.scene.scale(4.8, 0.4, 0.2);
        this.baseCube.display();
        this.scene.popMatrix();

        this.scene.popMatrix();

        this.scene.pushMatrix();
        doorTex.bind(0);

        this.scene.pushMatrix();
        this.scene.scale(4.0, 6.0, 1);
        this.scene.translate(0, 0.5, 6.6);
        this.baseCube.display();
        this.scene.popMatrix();

        this.scene.popMatrix();


        //Janela
        this.scene.pushMatrix();

        windowTex.bind(0);
        this.scene.pushMatrix();
        this.scene.translate(0, 9.7, 7.47);
        this.scene.scale(2.4, 2.4, 0.1);
        this.baseCube.display();
        this.scene.popMatrix();

        this.scene.popMatrix();


        this.scene.pushMatrix();
        roofTex.bind(0);
        this.scene.pushMatrix();
        this.scene.translate(0, 8, 0);
        this.scene.scale(11, 4.4, 15);
        this.roofPrimitive.display();
        this.scene.popMatrix();
        this.scene.popMatrix();
    }
}