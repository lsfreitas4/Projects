
import { CGFobject, CGFappearance, CGFtexture } from '../../../../lib/CGF.js';
import { MyRectangle } from './MyRectangle.js';
import { MyTriangle } from './MyTriangle.js';


export class MyArrow extends CGFobject {
    constructor(scene, width = 1, height = 1) {
        super(scene);

        this.initParts();
        this.initMaterial();
    }
 
    initParts(){

        this.tringle = new MyTriangle(this.scene);
        this.rect = new MyRectangle(this.scene);

    }

    initMaterial(){

        this.arrowTexture = new CGFtexture(this.scene, 'textures/Bale/green.png');
        this.arrowMaterial = new CGFappearance(this.scene);
        this.arrowMaterial.setAmbient(0.9, 0.9, 0.9, 1);
        this.arrowMaterial.setDiffuse(0.5, 0.5, 0.5, 1);
        this.arrowMaterial.setSpecular(0.0, 0.0, 0.0, 1);
        this.arrowMaterial.setShininess(10);
        this.arrowMaterial.setTexture(this.arrowTexture);
        this.arrowMaterial.setTextureWrap('REPEAT', 'REPEAT');
    }

    display(){
        this.scene.pushMatrix();
            this.arrowMaterial.apply();

            this.scene.pushMatrix();

                this.scene.translate(0, 0.3, 0);
                this.rect.display();

            this.scene.popMatrix();

            this.scene.pushMatrix();

                this.scene.rotate(Math.PI/4, 0, 0, 1);
                this.tringle.display();

            this.scene.popMatrix();

        this.scene.popMatrix();

    }

}
