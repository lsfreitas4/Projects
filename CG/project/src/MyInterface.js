import { CGFinterface, dat } from '../../lib/CGF.js';

export class MyInterface extends CGFinterface {
    constructor() {
        super();

        this.gameStats = {
            currentHealthPoints: 100,
            instantaneousDamage: 0,
            healthRestoredTotal: 0,
            totalBalesDropped: 0,
            score: 0
        };
    }

    updateGameStats(stats) {
        Object.assign(this.gameStats, stats);
    }

    initKeys() {
            // create reference from the scene to the GUI
            this.scene.gui=this;

            // disable the processKeyboard function
            this.processKeyboard=function(){};

            // create a named array to store which keys are being pressed
            this.activeKeys={};
    }

    processKeyDown(event) {
            // called when a key is pressed down
            // mark it as active in the array
            this.activeKeys[event.code]=true;

    }

    processKeyUp(event) {
            // called when a key is released, mark it as inactive in the array
            this.activeKeys[event.code]=false;

    }

    isKeyPressed(keyCode) {
            // returns true if a key is marked as pressed, false otherwise
            return this.activeKeys[keyCode] || false;

    }

    init(application) {
        super.init(application);
        this.gui = new dat.GUI();

        const statsFolder = this.gui.addFolder('Game Stats');
        const controllers = [
            statsFolder.add(this.gameStats, 'currentHealthPoints', 0, 100).name('Current HP').listen(),
            statsFolder.add(this.gameStats, 'instantaneousDamage', 0, 200).name('Total Damage').listen(),
            statsFolder.add(this.gameStats, 'healthRestoredTotal', 0, 5000).name('HP Restored Total').listen(),
            statsFolder.add(this.gameStats, 'totalBalesDropped', 0, 100).name('Bales Delivered').listen(),
            statsFolder.add(this.gameStats, 'score', 0, 3600).name('Score (s)').listen()
        ];

        controllers.forEach(controller => {
            controller.domElement.style.pointerEvents = 'none';
        });

        statsFolder.open();


        this.initKeys();
        return true;
    }
}