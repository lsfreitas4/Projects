import { CGFscene, CGFcamera, CGFaxis, CGFappearance, CGFtexture, CGFshader } from "../../lib/CGF.js";
import { CGFobjModel } from "../../lib/extra/CGFobjModel.js";
import { MySun } from "./Enviroment/MySun.js";
import { MySky } from "./Enviroment/MySky.js";
import { MyTerrain } from "./Terrain/MyTerrain.js";
import { MyGrassField } from "./Terrain/MyGrassField.js";
import { MyFlowerField } from "./Flora/MyFlowerField.js";
import { MyRockField } from "./Flora/MyRockField.js";
import { MyWagon } from "./Wagon/MyWagon.js";
import { MyBarn } from "./Barn/MyBarn.js";
import { MyBaleArea } from "./Barn/MyBaleArea.js";
import { MyBales } from "./Bales/MyBales.js";
import { MyBale } from "./Bales/MyBale.js";

function getStringFromUrl(url) {
    var xmlHttpReq = new XMLHttpRequest();
    xmlHttpReq.open("GET", url, false);
    xmlHttpReq.send();
    return xmlHttpReq.responseText;
}


export class ShaderScene extends CGFscene {
    constructor() {
        super();
        this.appearance = null;
        this.scaleFactor = 1.0;
        this.elapsedTime = 0;
        // Fator de elevação das colinas
        this.terrainScale = 5.0;
        this.grassPatchCount = 1500;

        this.lastT = 0;
        this.pPressedLast = false;
        this.lPressedLast = false;
        this.carriedBales = [];
        this.maxCarried = 2;

        this.maxHealthPoints = 100;
        this.currentHealthPoints = this.maxHealthPoints;
        this.instantaneousDamage = 0;
        this.instantaneousHealthRestored = 0;
        this.totalBalesDropped = 0;
        this.healthRestoredTotal = 0;
        this.score = 0;
        this.healthDrainPerSecond = 1;
        this.baleHealthValue = 50;
        this.rockDamageMin = 5;
        this.rockDamageMax = 15;
        this.rockCollisionRadius = 4.5;
        this.rockContacts = new Set();
        this.gameOver = false;

        this.cameraFollowDistance = 20;
        this.cameraFollowHeight = 8;
        this.cameraLookAhead = 6;
    }

    init(application) {
        super.init(application);
        this.initCameras();
        this.initLights();

        this.gl.clearDepth(10000.0);
        this.gl.clearColor(0.0, 0.0, 0.0, 1.0);
        this.gl.enable(this.gl.DEPTH_TEST);
        this.gl.enable(this.gl.CULL_FACE);
        this.gl.depthFunc(this.gl.LEQUAL);

        this.enableTextures(true);

        // Objects

        this.MySky = new MySky(this);
        this.MySun = new MySun(this);
        this.terrain = new MyTerrain(this, 100, 200);
        this.grassField = new MyGrassField(this, this.terrain, this.terrainScale, this.grassPatchCount);
        this.flowerField = new MyFlowerField(this, this.terrain, this.terrainScale, 60);
        this.rockField = new MyRockField(this, this.terrain, this.terrainScale, 45);
        this.myWagon = new MyWagon(this);
        this.barn = new MyBarn(this);
        this.baleArea = new MyBaleArea(this, 2.0, -75.0, 8.0);
        this.bales = new MyBales(this, this.terrain, this.terrainScale);

        // Texture

        this.sunTexture = new CGFtexture(this, 'textures/Enviroment/Sun/sunTexture.png');
        this.skyTexture = new CGFtexture(this, 'textures/Enviroment/Sky/skyTexture.jpg');
        this.cloudTexture = new CGFtexture(this, 'textures/Enviroment/Sky/cloudTexture.png');
        this.terrainTexture = new CGFtexture(this, "textures/Enviroment/Terrain/Grass.png");
        this.trampledTexture = new CGFtexture(this, "textures/Enviroment/Terrain/TrampledTerrain.png");
        this.rockTexture = new CGFtexture(this, "textures/Enviroment/Terrain/RockBase.jpg");

        this.barnWoodTexture = new CGFtexture(this, "textures/Barn/Walls.jpg");
        this.roofDarkTexture = new CGFtexture(this, "textures/Barn/barnRoof.jpg");
        this.windowTexture = new CGFtexture(this, "textures/Barn/window.jpg");
        this.doorTexture = new CGFtexture(this, "textures/Barn/Doors.jpg");


        // Shaders
        this.shaders = [
            new CGFshader(this.gl, "shaders/Enviroment/Sun/sun.vert", "shaders/Enviroment/Sun/sun.frag"),
            new CGFshader(this.gl, "shaders/Enviroment/Sky/sky.vert", "shaders/Enviroment/Sky/sky.frag"),
        ];

        // Shaders
        this.sunShader = new CGFshader(this.gl, "shaders/Enviroment/Sun/sun.vert", "shaders/Enviroment/Sun/sun.frag");
        this.skyShader = new CGFshader(this.gl, "shaders/Enviroment/Sky/sky.vert", "shaders/Enviroment/Sky/sky.frag");
        this.terrainShader = new CGFshader(this.gl, "shaders/Terrain/terrain.vert", "shaders/Terrain/terrain.frag");

        this.shaders = [this.sunShader, this.skyShader, this.terrainShader];


        this.skyShader.setUniformsValues({ uSampler: 0, uSampler2: 1 });
        this.terrainShader.setUniformsValues({ uSampler: 0, uSampler2: 1 });

        // Appearance
        this.appearance = new CGFappearance(this);
        this.appearance.setAmbient(0.3, 0.3, 0.3, 1);
        this.appearance.setDiffuse(0, 0, 0, 1);
        this.appearance.setSpecular(0.0, 0.0, 0.0, 1);
        this.appearance.setShininess(120);

        this.sunAppearance = new CGFappearance(this);
        this.sunAppearance.setAmbient(1, 1, 1, 1);
        this.sunAppearance.setDiffuse(0.7, 0.7, 0.7, 1);
        this.sunAppearance.setSpecular(0.0, 0.0, 0.0, 1);
        this.sunAppearance.setShininess(120);
        this.sunAppearance.setTexture(this.sunTexture);
        this.sunAppearance.setTextureWrap('REPEAT', 'REPEAT');


        this.skyAppearance = new CGFappearance(this);
        this.skyAppearance.setAmbient(0.3, 0.3, 0.3, 1);
        this.skyAppearance.setDiffuse(0.7, 0.7, 0.7, 1);
        this.skyAppearance.setSpecular(0.0, 0.0, 0.0, 1);
        this.skyAppearance.setShininess(120);
        this.skyAppearance.setTexture(this.skyTexture);
        this.skyAppearance.setTextureWrap('REPEAT', 'REPEAT');

        this.cloudAppearance = new CGFappearance(this);
        this.cloudAppearance.setAmbient(0.3, 0.3, 0.3, 1);
        this.cloudAppearance.setDiffuse(0.7, 0.7, 0.7, 1);
        this.cloudAppearance.setSpecular(0.0, 0.0, 0.0, 1);
        this.cloudAppearance.setShininess(120);
        this.cloudAppearance.setTexture(this.cloudTexture);
        this.cloudAppearance.setTextureWrap('REPEAT', 'REPEAT');

        this.initGameOverScreen();

        this.setUpdatePeriod(16);
    }

    initGameOverScreen() {
        this.gameOverScreen = document.getElementById('gameOverScreen');
        this.gameOverScoreLabel = document.getElementById('gameOverScore');
        this.gameOverBalesLabel = document.getElementById('gameOverBales');

        const button = document.getElementById('restartButton');
        if (button) button.addEventListener('click', () => this.restartGame());
    }

    showGameOverScreen() {
        if (!this.gameOverScreen) return;
        this.gameOverScoreLabel.textContent = `Score: ${Math.floor(this.score)} s`;
        this.gameOverBalesLabel.textContent = `Bales Delivered: ${this.totalBalesDropped}`;
        this.gameOverScreen.classList.add('visible');
    }

    hideGameOverScreen() {
        if (this.gameOverScreen) this.gameOverScreen.classList.remove('visible');
    }

    restartGame() {
        this.currentHealthPoints = this.maxHealthPoints;
        this.instantaneousDamage = 0;
        this.instantaneousHealthRestored = 0;
        this.totalBalesDropped = 0;
        this.healthRestoredTotal = 0;
        this.score = 0;
        this.gameOver = false;
        this.lastT = 0;
        this.carriedBales = [];
        this.rockContacts = new Set();
        this.pPressedLast = false;
        this.lPressedLast = false;

        this.myWagon = new MyWagon(this);
        this.bales = new MyBales(this, this.terrain, this.terrainScale);

        this.hideGameOverScreen();
        this.syncGameStats();
    }


    initCameras() {
        this.camera = new CGFcamera(
            0.4, 0.1, 500,
            vec3.fromValues(0, 20, 100),
            vec3.fromValues(0, 0, 0)
        );
    }

    initLights() {
        if (this.lights.length > 0) {
            this.lights[0].setPosition(45, 75, 45, 0);
            this.lights[0].setAmbient(0.2, 0.2, 0.2, 1);
            this.lights[0].setDiffuse(0.9, 0.9, 1.0, 1);
            this.lights[0].setSpecular(0, 0, 0, 1);
            this.lights[0].enable();
            this.lights[0].update();
        }
    }

    onWireframeChanged(v) {
        const obj = this.objects[this.selectedObject];
        if (obj && typeof obj.setFillMode === 'function') {
            if (v) obj.setLineMode();
            else obj.setFillMode();
        }
    }

    onSelectedShaderChanged(v) {
        this.onScaleFactorChanged(this.scaleFactor);
    }

    onScaleFactorChanged(v) {
        if (this.shaders[this.selectedExampleShader])
            this.shaders[this.selectedExampleShader].setUniformsValues({ scaleFactor: v });
    }

    update(t) {
        if (this.gameOver) {
            if (this.gui && this.gui.isKeyPressed("KeyR")) {
                this.restartGame();
            }
            this.syncGameStats();
            return;
        }


        const dt = this.lastT === 0 ? 0 : (t - this.lastT) / 1000;
        this.lastT = t;
        this.elapsedTime = t / 1000;
        this.score += dt;
        this.instantaneousHealthRestored = 0;

        if (dt > 0) {
            this.currentHealthPoints = Math.max(0, this.currentHealthPoints - this.healthDrainPerSecond * dt);
        }

        if (this.myWagon) this.myWagon.update(dt);

        this.updateFollowCamera();

        this.instantaneousDamage += this.applyRockDamage(dt);


        let time = t / 1000 % 100;
        if (this.shaders[0]) this.shaders[0].setUniformsValues({ timeFactor: time });
        if (this.shaders[1]) this.shaders[1].setUniformsValues({ timeFactor: time });
        if (this.shaders[2]) this.shaders[2].setUniformsValues({ timeFactor: time });
        if (this.shaders[3]) this.shaders[3].setUniformsValues({ timeFactor: time });

        const pNow = this.gui.isKeyPressed("KeyP");

        if (pNow && !this.pPressedLast) {
            this.handlePickup();
        }
        this.pPressedLast = pNow;

        const lNow = this.gui.isKeyPressed("KeyL");
        if (lNow && !this.lPressedLast) {
            this.handleDrop();
        }
        this.lPressedLast = lNow;

        if (this.bales) this.bales.update(dt);

        if (this.currentHealthPoints <= 0) {
            this.currentHealthPoints = 0;
            this.gameOver = true;
            this.score = Math.max(this.score, 0);
            this.showGameOverScreen();
        }

        this.syncGameStats();
    }

    updateFollowCamera() {
        if (!this.camera || !this.myWagon) return;

        const wagonX = this.myWagon.myPosition[0];
        const wagonY = this.terrain.getHeightAt(wagonX, this.myWagon.myPosition[2], this.terrainScale) + 1.2;
        const wagonZ = this.myWagon.myPosition[2];
        const yaw = this.myWagon.yaw;

        const forwardX = Math.sin(yaw);
        const forwardZ = Math.cos(yaw);

        const cameraX = wagonX - forwardX * this.cameraFollowDistance;
        const cameraY = wagonY + this.cameraFollowHeight;
        const cameraZ = wagonZ - forwardZ * this.cameraFollowDistance;

        const targetX = wagonX + forwardX * this.cameraLookAhead;
        const targetY = wagonY + 2.0;
        const targetZ = wagonZ + forwardZ * this.cameraLookAhead;

        this.camera.setPosition(vec3.fromValues(cameraX, cameraY, cameraZ));
        this.camera.setTarget(vec3.fromValues(targetX, targetY, targetZ));
    }

    syncGameStats() {
        if (this.gui && typeof this.gui.updateGameStats === 'function') {
            this.gui.updateGameStats({
                currentHealthPoints: this.currentHealthPoints,
                instantaneousDamage: this.instantaneousDamage,
                healthRestoredTotal: this.healthRestoredTotal,
                totalBalesDropped: this.totalBalesDropped,
                score: this.score
            });
        }
    }

    applyRockDamage(dt) {
        if (!this.rockField || !this.myWagon || !this.rockField.rocks) return;
 
        const wagonX = this.myWagon.myPosition[0];
        const wagonZ = this.myWagon.myPosition[2];
        let totalDamage = 0;
 
        for (const rock of this.rockField.rocks) {
            const dx = rock.x - wagonX;
            const dz = rock.z - wagonZ;
            const distance = Math.sqrt(dx * dx + dz * dz);
            const rockScale = Math.max(rock.sx || 1, rock.sz || 1);
            const threshold = this.rockCollisionRadius * rockScale;
            const key = `${rock.x}:${rock.z}:${rock.seed}`;
            const inContact = this.rockContacts.has(key);
 
            if (distance < threshold) {
                if (!inContact) {
                    const damage = Math.floor(this.rockDamageMin + Math.random() * (this.rockDamageMax - this.rockDamageMin + 1));
                    totalDamage += damage;
                    this.currentHealthPoints -= damage;
                    this.rockContacts.add(key);
                }
            } else if (inContact && distance > threshold * 1.1) {
                this.rockContacts.delete(key);
            }
        }
 
        if (this.currentHealthPoints < 0) this.currentHealthPoints = 0;
        return totalDamage;
    }



    handlePickup() {
        const w = this.myWagon;
        if (this.carriedBales.length >= this.maxCarried) return;

        const pickupRadius = 5.0;
        let best = null, bestDist = pickupRadius;
        for (const b of this.bales.getActiveBales()) {
            const dx = b.position[0] - w.myPosition[0];
            const dz = b.position[2] - w.myPosition[2];
            const dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < bestDist) { bestDist = dist; best = b; }
        }
        if (best) {
            best.attached = true;
            this.carriedBales.push(best);
        }
    }

    handleDrop() {
        const w = this.myWagon;
        if (this.carriedBales.length === 0) return;

        const areaX = 2.0, areaZ = -75.0, dropRadius = 8.0;
        const dx = w.myPosition[0] - areaX;
        const dz = w.myPosition[2] - areaZ;
        const dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < dropRadius) {
            const baseY = this.terrain.getHeightAt(areaX, areaZ, this.terrainScale);
            const deliveredCount = this.carriedBales.length;
            this.carriedBales.forEach((b, i) => {
                b.attached = false;
                b.delivered = true;
                b.position[0] = areaX;
                b.position[1] = baseY + i * 2.0;
                b.position[2] = areaZ;
            });
            this.totalBalesDropped += deliveredCount;
            this.instantaneousHealthRestored = deliveredCount * this.baleHealthValue;
            this.healthRestoredTotal += this.instantaneousHealthRestored;
            this.currentHealthPoints = Math.min(this.maxHealthPoints, this.currentHealthPoints + this.instantaneousHealthRestored);
            this.carriedBales = [];
        }
    }

    display() {
        this.gl.viewport(0, 0, this.gl.canvas.width, this.gl.canvas.height);
        this.gl.clear(this.gl.COLOR_BUFFER_BIT | this.gl.DEPTH_BUFFER_BIT);

        this.updateProjectionMatrix();
        this.loadIdentity();
        this.applyViewMatrix();

        this.lights[0].update();

        if (this.displayAxis) this.axis.display();

        this.appearance.apply();

        if (this.displayNormals) {
            this.MySun.enableNormalViz();
            this.MySky.enableNormalViz();
            this.myWagon.enableNormalViz();
        }

        else {
            this.MySun.disableNormalViz();
            this.MySky.disableNormalViz();
            this.myWagon.disableNormalViz();

        }


        // Display Sun with it's shader and texture and Transformations
        this.pushMatrix();

        this.sunAppearance.apply();
        this.setActiveShader(this.shaders[0]);

        this.translate(45, 75, 45)

        this.MySun.display();
        this.setActiveShader(this.defaultShader);

        this.popMatrix();

        // DisPlay Sky Box
        this.pushMatrix();

        this.skyAppearance.apply();
        this.setActiveShader(this.shaders[1]);
        this.cloudTexture.bind(1);
        this.MySky.display();
        this.setActiveShader(this.defaultShader);

        this.popMatrix();

        // Display Terrain
        this.pushMatrix();
        this.setActiveShader(this.terrainShader);

        // Vincular texturas: Unit 0 (Grama), Unit 1 (Pisada/Heightmap)
        this.terrainTexture.bind(0);
        this.trampledTexture.bind(1);

        this.terrainShader.setUniformsValues({
            uSampler: 0,
            uSampler2: 1,
            nScale: this.terrainScale
        });

        this.terrain.display();

        this.setActiveShader(this.defaultShader);
        this.popMatrix();

        // Display Rock
        this.pushMatrix();
        this.rockTexture.bind(0);
        this.rockField.display();
        this.popMatrix();

        // Display Grass and Flowers
        this.pushMatrix();
        this.grassField.terrainScale = this.terrainScale;
        this.grassField.display();
        this.popMatrix();


        this.flowerField.terrainScale = this.terrainScale;
        this.flowerField.display();

        // Display Barn
        let wagonPos = { 
            x: this.myWagon ? this.myWagon.myPosition[0] : 0, 
            z: this.myWagon ? this.myWagon.myPosition[2] : 0 
        };

        this.pushMatrix();
        let barnX = 2.0;
        let barnZ = -88.0;
        let barnY = this.terrain.getHeightAt(barnX, barnZ, this.terrainScale);
        this.translate(barnX, barnY + 0.5, barnZ);
        this.barn.display(this.defaultShader, this.barnWoodTexture, this.roofDarkTexture, this.windowTexture, this.doorTexture);
        this.popMatrix();

        let isCarryingBale = (this.carriedBales && this.carriedBales.length > 0);
        
        let baleOffset = 0.8;
        let baleY = this.terrain.getHeightAt(this.baleArea.x, this.baleArea.z, this.terrainScale);

        this.baleArea.display(wagonPos, baleY + baleOffset, isCarryingBale);


        this.pushMatrix();
        const w = this.myWagon;
        const groundY = this.terrain.getHeightAt(w.myPosition[0], w.myPosition[2], this.terrainScale) + 1.2;
        this.translate(w.myPosition[0], groundY, w.myPosition[2]);
        this.rotate(w.yaw, 0, 1, 0);
        this.myWagon.display();

        this.carriedBales.forEach((b, i) => {
            this.pushMatrix();
            this.translate(0, 0.7, 1.0 - 2.2 * i);
            this.bales.bale.display();
            this.popMatrix();
        });

        this.popMatrix();

        this.pushMatrix();
        this.bales.display(this.myWagon.myPosition[0], this.myWagon.myPosition[2]);
        this.popMatrix();
    }
}