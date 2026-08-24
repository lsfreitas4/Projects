import {CGFobject} from '../../../../lib/CGF.js';
import { MyWheelsOuter } from './MyWheelOuter.js';
import { MyWheelsCap } from './MyWheelsCap.js';

export class MyWheelsTyre extends CGFobject {

    constructor(scene){
        super(scene);
        
        this.outerRadius = 1.0;
        this.innerRadius = 0.8;
        this.width = 0.5;

        this.initParts();

    }

    initParts() {
        this.TyreOuter = new MyWheelsOuter(this.scene);
        this.TyreInner = new MyWheelsOuter(this.scene, this.innerRadius);

        this.CapBack = new MyWheelsCap(this.scene, {
            innerRadius: this.innerRadius,
            outerRadius: this.outerRadius,
            z: 0,
            facing: -1
        });
        this.CapFront = new MyWheelsCap(this.scene, {
            innerRadius: this.innerRadius,
            outerRadius: this.outerRadius,
            z: this.width,
            facing: +1
        });

    }

    display(){

        const gl = this.scene.gl;
        gl.disable(gl.CULL_FACE);

        this.TyreOuter.display();
        this.TyreInner.display();
        this.CapBack.display();
        this.CapFront.display();

        gl.enable(gl.CULL_FACE);
    }

    enableNormalViz() {
        this.TyreOuter.enableNormalViz();
        this.TyreInner.enableNormalViz();
        this.CapBack.enableNormalViz();
        this.CapFront.enableNormalViz();
    }

    disableNormalViz() {
        this.TyreOuter.disableNormalViz();
        this.TyreInner.disableNormalViz();
        this.CapBack.disableNormalViz();
        this.CapFront.disableNormalViz();
    }


}