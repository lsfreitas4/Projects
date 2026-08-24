import { CGFobject, CGFappearance } from '../../../../lib/CGF.js';
import { MyPetals } from './MyPetals.js';
import { MyBase } from './MyBase.js';
import { MyMiddle } from './MyMiddle.js';


export class MyFlower extends CGFobject {

    constructor(scene, options = {}) {
        super(scene);
        this.petalCount = options.petalCount ?? Math.floor(MyFlower.randomBetween(5, 10));
        this.petalWidth = options.petalWidth ?? MyFlower.randomBetween(0.2, 0.4);
        this.petalHeight = options.petalHeight ?? MyFlower.randomBetween(0.4, 1.0);
        this.baseSize = options.baseSize ?? MyFlower.randomBetween(0.2, 0.3);
        this.stemHeight = options.stemHeight ?? MyFlower.randomBetween(0.95, 1.35);

        this.petalColor = options.petalColor ?? MyFlower.randomPetalColor();
        this.baseColor = options.baseColor ?? [0, MyFlower.randomBetween(0.3, 0.9), 0];
        this.middleColor = options.middleColor ?? MyMiddle.randomCenterColor();

        this.initParts();
    }

    static randomBetween(min, max) {
        return min + Math.random() * (max - min);
    }

    static randomPetalColor() {
        const palettes = [
            [1.0, 0.2, 0.2],   // red
            [1.0, 1.0, 0.2],   // yellow
            [0.7, 0.3, 1.0],   // purple
            [1.0, 1.0, 1.0],   // white
        ];
        return palettes[Math.floor(Math.random() * palettes.length)];
    }

    initParts() {
        this.center = new MyBase(this.scene, {
            width: this.baseSize,
            height: this.stemHeight,
            color: this.baseColor
        });

        this.petal = new MyPetals(this.scene, 10, this.petalWidth, this.petalHeight);

        this.middle = new MyMiddle(this.scene, {
            color: this.middleColor
        });
    }


    displayParts(setColor) {
        const angleStep = (2 * Math.PI) / this.petalCount;

        this.scene.pushMatrix();
        setColor(this.baseColor);
        this.center.display();
        this.scene.popMatrix();


        this.scene.pushMatrix();
        this.scene.translate(0, this.center.height, 0);

        for (let i = 0; i < this.petalCount; i++) {
            this.scene.pushMatrix();
            this.scene.rotate(i * angleStep, 0, 1, 0);
            this.scene.translate(this.baseSize / 2, 0, 0);
            setColor(this.petalColor);
            this.petal.display();
            this.scene.popMatrix();
        }

        this.scene.pushMatrix();
        setColor(this.middleColor);
        this.middle.display();
        this.scene.popMatrix();

        this.scene.popMatrix();
    }


    display() {
        this.displayParts(() => { });
    }
}