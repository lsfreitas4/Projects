import { CGFobject } from '../../../lib/CGF.js';
import { MyFlower } from './Flower/MyFlower.js';


export class MyFlowerPatch extends CGFobject {
    constructor(scene, flowerDescriptors) {
        super(scene);
        this.flowerDescriptors = flowerDescriptors;
        this.flowers = flowerDescriptors.map(() => new MyFlower(scene));
    }

    display() {
        const shader = this.scene.activeShader;
        if (!shader || !shader.setUniformsValues) return;

        const setColor = (rgb) => {
            shader.setUniformsValues({
                uOverrideColor: [rgb[0], rgb[1], rgb[2], 1.0]
            });
        };

        for (let i = 0; i < this.flowerDescriptors.length; i++) {
            const f = this.flowerDescriptors[i];


            shader.setUniformsValues({
                uFlowerWorldXZ: [f.worldX, f.worldZ]
            });

            this.scene.pushMatrix();

            this.scene.translate(f.worldX, 0, f.worldZ);
            this.scene.rotate(f.rotY, 0, 1, 0);
            this.scene.scale(f.scale, f.scale, f.scale);
            this.flowers[i].displayParts(setColor);
            this.scene.popMatrix();
        }
    }
}