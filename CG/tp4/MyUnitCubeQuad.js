import {CGFobject, CGFappearance, CGFtexture} from '../lib/CGF.js';
/**
 * MyTriangle
 * @constructor
 * @param scene - Reference to MyScene object
 */
export class MyUnitCubeQuad{
    constructor(scene, topo = null, frente = null, direita = null, tras = null, esquerda = null, fundo = null) {
        this.scene = scene;
        this.topoMat     = this.makeMaterial(topo);
        this.frenteMat   = this.makeMaterial(frente);
        this.direitaMat  = this.makeMaterial(direita);
        this.trasMat     = this.makeMaterial(tras);
        this.esquerdaMat = this.makeMaterial(esquerda);
        this.fundoMat    = this.makeMaterial(fundo);
    }

    makeMaterial(texture) {
        if (!texture) return null;
        const mat = new CGFappearance(this.scene);
        mat.setAmbient(0.1, 0.1, 0.1, 1);
        mat.setDiffuse(0.9, 0.9, 0.9, 1);
        mat.setSpecular(0.1, 0.1, 0.1, 1);
        mat.setShininess(10.0);
        mat.setTexture(texture);
        mat.setTextureWrap('REPEAT', 'REPEAT');
        return mat;
    }

    applyNearest(mat) {
        if (!mat) return;
        mat.apply();
        this.scene.gl.texParameteri(
            this.scene.gl.TEXTURE_2D,
            this.scene.gl.TEXTURE_MAG_FILTER,
            this.scene.gl.NEAREST
        );
    }

    display(){
        
        // Base Cima

        this.scene.pushMatrix();
        
        this.scene.rotate(-Math.PI/2, 1, 0, 0);
        if (this.topoMat) {
            this.topoMat.apply();
            this.applyNearest(this.topoMat);
        }
        this.scene.quad.display();

        this.scene.popMatrix();
        
        // Frente
        
        this.scene.pushMatrix();

        this.scene.translate(0, -0.5, 0.5)
        if (this.frenteMat) {
            this.frenteMat.apply();
            this.applyNearest(this.frenteMat);
        }
        this.scene.quad.display();

        this.scene.popMatrix();


        // Direita
        
        this.scene.pushMatrix();

        this.scene.rotate(Math.PI/2, 0, 1, 0);
        this.scene.translate(0, -0.5, 0.5)
        if (this.direitaMat) {
            this.direitaMat.apply();
            this.applyNearest(this.direitaMat);
        }
        this.scene.quad.display();

        this.scene.popMatrix();

        // Tras
        
        this.scene.pushMatrix();

        this.scene.rotate(Math.PI, 1, 0, 0);
        this.scene.translate(0, 0.5, 0.5)
        this.scene.rotate(Math.PI, 0, 0, 1);
        if (this.trasMat) {
            this.trasMat.apply();
            this.applyNearest(this.trasMat);
        }
        this.scene.quad.display();

        this.scene.popMatrix();
        
        // Esquerda
        
        this.scene.pushMatrix();

        this.scene.rotate(-Math.PI/2, 0, 1, 0);
        this.scene.translate(0, -0.5, 0.5);

        if (this.esquerdaMat) {
            this.esquerdaMat.apply();
            this.applyNearest(this.esquerdaMat);
        }
        this.scene.quad.display();

        this.scene.popMatrix();
        
        // Fundo
        
        this.scene.pushMatrix();

        this.scene.rotate(Math.PI/2, 1, 0, 0);
        this.scene.translate(0, 0, 1);
        if (this.fundoMat) {
            this.fundoMat.apply();
            this.applyNearest(this.fundoMat);
        }
        this.scene.quad.display();

        this.scene.popMatrix();
    
    }

}
