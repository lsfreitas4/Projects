
import { CGFobject } from '../../../../lib/CGF.js';
 
/**
 * MyRectangle
 * @constructor
 * @param scene  - Reference to the scene
 * @param width  - size along X (default 1)
 * @param height - size along Y (default 1)
 */
export class MyRectangle extends CGFobject {
    constructor(scene, width = 1, height = 2) {
        super(scene);
        this.width = width;
        this.height = height;
        this.initBuffers();
    }
 
    initBuffers() {
        const hw = this.width * 0.5;
        const hh = this.height * 0.5;
 

        this.vertices = [
            -hw, -hh, 0,
             hw, -hh, 0,
             hw,  hh, 0,
            -hw,  hh, 0,

            -hw, -hh, 0,
             hw, -hh, 0,   
             hw,  hh, 0,   
            -hw,  hh, 0    

        ];
 
        // Two triangles, facing +Z.
        this.indices = [
            0, 1, 2,
            0, 2, 3,

            6, 5, 4,
            7, 6, 4

        ];
 
        // All normals point along +Z.
        this.normals = [
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,
            0, 0, 1,

            0, 0, -1,
            0, 0, -1,
            0, 0, -1,
            0, 0, -1

        ];
 

        this.texCoords = [
            0, 1,   
            1, 1,   
            1, 0,   
            0, 0,

            1, 1,
            0, 1,
            0, 0,
            1, 0

        ];
 
        this.primitiveType = this.scene.gl.TRIANGLES;
        this.initGLBuffers();
    }
 
}
