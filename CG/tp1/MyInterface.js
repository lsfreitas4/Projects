import {CGFinterface, dat} from '../lib/CGF.js';

/**
* MyInterface
* @constructor
*/
export class MyInterface extends CGFinterface {
    constructor() {
        super();
    }

    init(application) {
        // call CGFinterface init
        super.init(application);
        
        // init GUI. For more information on the methods, check:
        // https://github.com/dataarts/dat.gui/blob/master/API.md
        this.gui = new dat.GUI();

        //Checkbox element in GUI
        this.gui.add(this.scene, 'displayAxis').name('Display Axis');
        
        this.gui.add(this.scene, 'displayDiamond').name('Display Diamond');

        this.gui.add(this.scene, 'displayTriangle').name('Display Triangle');

        this.gui.add(this.scene, 'displayParallelogram').name('Display Parall');

        this.gui.add(this.scene, 'displayTriangleSmall').name('Display TriangleS');

        this.gui.add(this.scene, 'displayTriangleBig').name('Display TriangleB');

        //Slider element in GUI
        this.gui.add(this.scene, 'scaleFactor', 0.1, 5).name('Scale Factor');


        //Checkbox Display Figures

        this.gui.add(this.scene, 'displayDiamond').name('My Diamond');

        this.gui.add(this.scene, 'displayTriangle').name('My Triangle');

        this.gui.add(this.scene, 'displayParallelogram').name('My Parallelogram');

        this.gui.add(this.scene, 'displayTriangleSmall').name('My Small Triangle');

        this.gui.add(this.scene, 'displayTriangleBig').name('My Big Triangle');


        return true;
    }
}