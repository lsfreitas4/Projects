import {CGFobject, CGFtexture, CGFappearance} from '../../../lib/CGF.js';
import { MyBale } from './MyBale.js';
import { MyArrow } from './Arrow/MyArrow.js';

/**
* @constructor
 * @param scene - Reference to MyScene object
 * @param slices - number of divisions around Z
 * @param stacks - number of divisions along Z
*/
export class MyBales extends CGFobject {
    constructor(scene, terrain, terrainScale = 5.0, count = 30, visible = 5) {
        super(scene);

        this.terrain = terrain;
        this.terrainScale = terrainScale;
        this.count = count;
        this.visibleCount = visible;
 
        this.bale = new MyBale(scene);
        this.arrow = new MyArrow(scene);
        this.bobTime = 0;
        this.bales = [];
        this.initBales();

    }

    update(dt) {
        this.bobTime += dt;
    }

    getNearestBale(wx, wz) {
        let best = null, bestDist = Infinity;
        let shown = 0;
        for (let i = 0; i < this.bales.length && shown < this.visibleCount; i++) {
            const b = this.bales[i];
            if (b.delivered || !b.visible) continue;
            if (b.attached) { shown++; continue; }
            const dx = b.position[0] - wx;
            const dz = b.position[2] - wz;
            const d = dx * dx + dz * dz;
            if (d < bestDist) { bestDist = d; best = b; }
            shown++;
        }
        return best;
    }

    initBales(){
        this.bales = [];
 
        const width = this.terrain.width;
        const half = width * 0.5;

        const baseRadius = 0.32;

            for (let i = 0; i < this.count; i++) {
                const angle = (i / this.count) * Math.PI * 2.0
                            + (this.terrain.random2(i, 7.0) - 0.5) * 0.15;
    
                let wobble = 0.0;
                wobble += 0.030 * Math.sin(angle * 3.0 + 1.7);
                wobble += 0.018 * Math.sin(angle * 7.0 + 0.5);
                wobble += 0.012 * this.terrain.noise2(angle * 2.0, 4.3) * 2.0 - 0.012;
                const ringRadiusUV = baseRadius + wobble;
    
                const u = 0.5 + Math.cos(angle) * ringRadiusUV;
                const v = 0.5 + Math.sin(angle) * ringRadiusUV;
                const x = (u - 0.5) * width;
                const z = (v - 0.5) * width;
    
                const y = this.terrain.getHeightAt(x, z, this.terrainScale);
    
                this.bales.push({
                    position: [x, y, z],
                    attached: false,
                    visible: true,
                    delivered: false
                });
            }  

            for (let i = this.bales.length - 1; i > 0; i--) {
                const r = this.terrain.random2(i, 13.0);
                const j = Math.floor(r * (i + 1));
                [this.bales[i], this.bales[j]] = [this.bales[j], this.bales[i]];
            }


    }

    getActiveBales() {
        const active = [];
        let shown = 0;
        for (let i = 0; i < this.bales.length && shown < this.visibleCount; i++) {
            const b = this.bales[i];
            if (b.delivered || !b.visible) continue;
            shown++;
            if (b.attached) continue;
            active.push(b);
        }
        return active;
    }

    display(wagonX = 0, wagonZ = 0){
            let shown = 0;
            for (let i = 0; i < this.bales.length && shown < this.visibleCount; i++) {
                const b = this.bales[i];
                if (b.delivered || !b.visible) continue;
                if (b.attached) { shown++; continue; }

                this.scene.pushMatrix();
                this.scene.translate(b.position[0], b.position[1], b.position[2]);
                this.bale.display();
                this.scene.popMatrix();
                shown++;
            }

            const target = this.getNearestBale(wagonX, wagonZ);
            if (target) {
                const bob = Math.sin(this.bobTime * 3.0) * 0.4;
                const arrowHeight = 4.0;                       
                this.scene.pushMatrix();
                this.scene.translate(
                    target.position[0],
                    target.position[1] + arrowHeight + bob,
                    target.position[2]
                );
                this.arrow.display();
                this.scene.popMatrix();
            }

    }
}
