import { CGFobject, CGFshader ,CGFtexture} from '../../../lib/CGF.js';
import { MyUnitCube } from './MyUnitCube.js';
import { MyCircle } from './MyCircle.js';

export class MyBaleArea {
    constructor(scene, x, z, radius = 6.0) {
        this.scene = scene;
        this.x = x;
        this.z = z;
        this.radius = radius;

        this.circle = new MyCircle(scene, 32);
        this.baseCube = new MyUnitCube(scene);

        this.areaTexture = new CGFtexture(scene, "textures/BaleArea/orangeZone.jpg");
        this.activeTexture = new CGFtexture(scene, "textures/BaleArea/greenZone.png");


        this.shader = new CGFshader(
            scene.gl,
            'shaders/Enviroment/Barn/baleArea.vert?v=5',
            'shaders/Enviroment/Barn/baleArea.frag?v=5'
        );
    }

    checkWagonIntersection(wagonX, wagonZ) {
        let dx = wagonX - this.x;
        let dz = wagonZ - this.z;
        let distance = Math.sqrt(dx * dx + dz * dz);
        
        return distance <= this.radius;
    }

    display(wagonPosition, yHeight = 0, hasBale = false) {
        let isIntersecting = this.checkWagonIntersection(wagonPosition.x, wagonPosition.z);
        let isActive = hasBale && isIntersecting;

        const prevShader = this.scene.activeShader;
        this.scene.setActiveShader(this.shader);
        
        this.shader.setUniformsValues({
            uIsIntersecting: isIntersecting ? 1.0 : 0.0,
            uHasBale: (hasBale && isIntersecting) ? 1.0 : 0.0,
            uCenterXZ: [this.x, this.z],
            uRadius: this.radius
        });

        this.scene.pushMatrix();
        
        this.scene.translate(this.x, yHeight + 0.05, this.z);
        this.scene.rotate(-Math.PI / 2, 1, 0, 0);
        this.scene.scale(this.radius * 2, this.radius * 2, 1);
        
        const textureToBind = isActive ? this.activeTexture : this.areaTexture;
        textureToBind.bind(0);

        this.circle.display();
        this.scene.popMatrix();

        this.scene.setActiveShader(prevShader);
    }
}