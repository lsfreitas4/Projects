import {CGFobject} from '../lib/CGF.js';
/**
 * MyDiamond
 * @constructor
 * @param scene - Reference to MyScene object
 */
export class MyUnitCube extends CGFobject {
    constructor(scene) {
        super(scene);
        this.initBuffers();
    }
    
    initBuffers() {
        this.vertices = [
            0.5, -0.5, 0.5, // A
            0.5, -0.5, -0.5, // B
            -0.5, -0.5, 0.5, // C
            -0.5, -0.5, -0.5, // D

            0.5, 0.5, 0.5, // E
            0.5, 0.5, -0.5, // F
            -0.5, 0.5, 0.5, // G
            -0.5, 0.5, -0.5, // H


        ];

        //Counter-clockwise reference of vertices
        this.indices = [
            // Base Baixo
            0, 1, 2, // ABC
            2, 1, 0, // CBA
            1, 2, 3, // BCD
            3, 2, 1, // DCB


            // 1 Lado
            0, 1, 4, // ABE
            4, 1, 0, // EBA
            1, 4, 5, // BEF
            5, 4, 1, // FEB

            // 2 Lado

            1, 5, 7, // BFH
            7, 5, 1, // HFB
            1, 3, 7, // BDH
            7, 3, 1, // HDB


            // 3 Lado

            2, 3, 7, // CDH
            7, 3, 2, // HDC
            2, 6, 7, // CGH
            7, 6, 2, // HGC

            // 4 Lado 

            2, 4, 6, // CEG
            6, 4, 2, // GEC
            0, 2, 4, // ACE
            4, 2, 0, // ECA

            // Base Cima

            4, 5, 6, // EFG
            6, 5, 4, // GFE
            5, 6, 7, // FGH
            7, 6, 5, // HGF


        ];

        //The defined indices (and corresponding vertices)
        //will be read in groups of three to draw triangles
        this.primitiveType = this.scene.gl.TRIANGLES;

        this.initGLBuffers();
    }
}