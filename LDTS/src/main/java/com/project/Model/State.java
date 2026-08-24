package com.project.Model;

import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import com.project.Controller.*;
import com.project.Controller.Map.LevelController;
import com.project.Controller.Menus.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class State {
    private static final List<Map> maps = Arrays.asList(
            new Map(Constants.MAP_LEVEL1),
            new Map(Constants.MAP_LEVEL2));

    private int activeLevel;
    private int levelScore;
    private int levelHighestScore;
    private Controller controller;
    private Controller oldController;
    private StateType stateType;
    private final Screen screen;

    public State(Screen screen){
        this.screen = screen;
    }

    public Controller getController(){
        return controller;
    }

    public StateType getStateType(){
        return stateType;
    }

    public int getActiveLevel() {
        return this.activeLevel;
    }

    public Map getActiveMap() {
        return maps.get(activeLevel - 1);
    }

    public List<Map> getMaps() {
        return maps;
    }

    public int getLevelHighestScore() {
        return this.levelHighestScore;
    }

    public int getLevelScore() {
        return this.levelScore;
    }

    public void setOldController(Controller oldController) {
        this.oldController = oldController;
    }

    public void setStateType(StateType stateType){
        this.stateType = stateType;
    }

    public void setActiveLevel(int level){
        this.activeLevel = level;
    }

    public void setLevelHighestScore(int score) {
        this.levelHighestScore = score;
    }

    public void setLevelScore(int score) {
        this.levelScore = score;
    }

    public void handleStateController() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        switch (stateType){
            case MENU:
                controller = new MainMenuController(screen, this);
                break;
            case SELECT_LEVEL:
                controller = new SelectLevelController(screen, this);
                break;
            case HELP:
                controller = new HelpController(screen, this);
                break;
            case LEVEL:
                levelScore = 0;
                levelHighestScore = 0;
                controller = new LevelController(screen, this);
                break;
            case GAMEOVER:
                controller = new GameOverController(screen, this);
                break;
            case PAUSE:
                oldController = controller;
                controller = new PauseController(screen, this);
                break;
            case RESUME:
                controller = oldController;
                oldController = null;
                break;
            case VICTORY:
                controller = new VictoryController(screen, this);
                break;
            default:
                break;
        }
        controller.renderView();
    }
}