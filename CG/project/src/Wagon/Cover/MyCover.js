import {CGFobject, CGFtexture, CGFappearance} from '../../../../lib/CGF.js';
import { MyCoverOuter } from './MyCoverOuter.js';
import { MyCoverCap } from './MyCoverCap.js';

export class MyCover extends CGFobject {

    constructor(scene){
        super(scene);
        
        this.outerRadius = 2.1;
        this.innerRadius = 1.9;
        this.width = 6;

        this.initParts();
        this.initMaterials();

    }

    initParts() {
        this.coverOuter = new MyCoverOuter(this.scene, this.outerRadius);
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

    initMaterials() {
            this.cottonTexture = new CGFtexture(this.scene, 'textures/Wagon/Cotton.jpg');
            this.coverMaterial = new CGFappearance(this.scene);
            this.coverMaterial.setAmbient(0.2, 0.2, 0.2, 1);
            this.coverMaterial.setDiffuse(0.4, 0.4, 0.4, 1);
            this.coverMaterial.setSpecular(0.1, 0.1, 0.1, 1);
            this.coverMaterial.setShininess(10);
            this.coverMaterial.setTexture(this.cottonTexture);
            this.coverMaterial.setTextureWrap('REPEAT', 'REPEAT');
        }

    display(){

        const gl = this.scene.gl;
        gl.disable(gl.CULL_FACE);

        this.coverMaterial.apply();
        this.coverOuter.display();
        this.coverInner.display();
        this.CapBack.display();
        this.CapFront.display();

        gl.enable(gl.CULL_FACE);
    }

    enableNormalViz() {
        this.coverOuter.enableNormalViz();
        this.coverInner.enableNormalViz();
        this.CapBack.enableNormalViz();
        this.CapFront.enableNormalViz();
    }

    disableNormalViz() {
        this.coverOuter.disableNormalViz();
        this.coverInner.disableNormalViz();
        this.CapBack.disableNormalViz();
        this.CapFront.disableNormalViz();
    }


}