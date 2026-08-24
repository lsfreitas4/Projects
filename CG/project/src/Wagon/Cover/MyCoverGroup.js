import {CGFobject} from '../../../../lib/CGF.js';
import { MyCoverOuter } from './MyCoverOuter.js';
import { MyCoverCap } from './MyCoverCap.js';

export class MyCoverGroup extends CGFobject {

    constructor(scene){
        super(scene);
        
        this.outerRadius = 1.0;
        this.innerRadius = 0.8;
        this.width = 0.5;

        this.initParts();

    }

    initParts() {
        this.coverOuter = new MyCoverOuter(this.scene);
        this.coverInner = new MyCoverOuter(this.scene, this.innerRadius);

        this.CapBack = new MyCoverCap(this.scene, {
            innerRadius: this.innerRadius,
            outerRadius: this.outerRadius,
            z: 0,
            facing: -1
        });
        this.CapFront = new MyCoverCap(this.scene, {
            innerRadius: this.innerRadius,
            outerRadius: this.outerRadius,
            z: this.width,
            facing: +1
        });

    }

    display(){

        const gl = this.scene.gl;
        gl.disable(gl.CULL_FACE);

        this.coverOuter.display();
        this.coverInner.display();
        this.CapBack.display();
        this.CapFront.display();

        gl.enable(gl.CULL_FACE);
    }


}