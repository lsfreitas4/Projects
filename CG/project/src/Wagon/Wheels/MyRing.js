import {CGFobject} from '../../../../lib/CGF.js';

export class MyRing extends CGFobject {

    constructor(scene){
        super(scene);

        this.initBuffers();
    }

    initBuffers(){
        this.vertices = [
            // Frente (+Z)
            -0.5, 0, 0.5,
            0.5, 0, 0.5,
            0.5, 2, 0.5,
            -0.5, 2, 0.5,

            // Trás (-Z)
            0.5, 0, -0.5,
            -0.5, 0, -0.5,
            -0.5, 2, -0.5,
            0.5, 2, -0.5,

            // Esquerda (-X)
            -0.5, 0, -0.5,
            -0.5, 0, 0.5,
            -0.5, 2, 0.5,
            -0.5, 2, -0.5,

            // Direita (+X)
            0.5, 0, 0.5,
            0.5, 0, -0.5,
            0.5, 2, -0.5,
            0.5, 2, 0.5,

            // Topo (+Y)
            -0.5, 2, 0.5,
            0.5, 2, 0.5,
            0.5, 2, -0.5,
            -0.5, 2, -0.5,

            // Base (-Y)
            -0.5, 0, -0.5,
            0.5, 0, -0.5,
            0.5, 0, 0.5,
            -0.5, 0, 0.5
        ];

        //Counter-clockwise reference of vertices
        this.indices = [
            0, 1, 2,
            0, 2, 3,

            4, 5, 6,
            4, 6, 7,

            8, 9, 10,
            8, 10, 11,

            12, 13, 14,
            12, 14, 15,

            16, 17, 18,
            16, 18, 19,

            20, 21, 22,
            20, 22, 23,
        ];

        this.normals = [
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,

            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1,

            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,
            -1, 0, 0,

            1, 0, 0,
            1, 0, 0,
            1, 0, 0,
            1, 0, 0,

            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,

            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
            0, -1, 0,
        ];

        this.texCoords = [
            // Frente
            0, 1,  1, 1,  1, 0,  0, 0,
            // Trás
            0, 1,  1, 1,  1, 0,  0, 0,
            // Esquerda
            0, 1,  1, 1,  1, 0,  0, 0,
            // Direita
            0, 1,  1, 1,  1, 0,  0, 0,
            // Topo
            0, 1,  1, 1,  1, 0,  0, 0,
            // Base
            0, 1,  1, 1,  1, 0,  0, 0,
        ]

        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();


    }


}