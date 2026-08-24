package com.project.Controller.Map;

import com.project.Controller.Controller;
import com.project.Controller.ThreadSleeper;
import com.project.Model.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.project.View.Map.LevelView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class LevelController extends Controller {
    protected final Player player;
    private Level level;
    private LevelView levelView;
    private ThreadSleeper threadSleeper;

    public LevelController(Screen screen, State state) {
        super(screen, state);
        this.threadSleeper = new ThreadSleeper();
        this.level = new Level();
        this.level.setState(state);
        this.level.initializeMapArray(state);
        this.level.unpackMap();
        this.player = level.getPlayer();
        this.levelView = new LevelView(level, screen);
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setLevelView(LevelView levelView) {
        this.levelView = levelView;
    }

    public void setThreadSleeper(ThreadSleeper threadSleeper) {
        this.threadSleeper = threadSleeper;
    }

    @Override
    public boolean play() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        KeyStroke key = levelView.inputListener();
        return handleInput(key);
    }

    @Override
    public void processKey(KeyStroke key) throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        handleKey(key);
        if (level.isExitAt(player.getPosition()))
            victory();
    }

    protected void handleKey(KeyStroke key) throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        switch (key.getKeyType()) {
            case ArrowUp:
                while (canPlayerMove(player.moveUp()) && hasPlayerWon(player.getPosition())) {
                    movePlayer(player.moveUp());
                    processPlayerLocation(player.getPosition());
                }
                break;
            case ArrowRight:
                while (canPlayerMove(player.moveRight()) && hasPlayerWon(player.getPosition())) {
                    movePlayer(player.moveRight());
                    processPlayerLocation(player.getPosition());
                }
                break;
            case ArrowDown:
                while (canPlayerMove(player.moveDown()) && hasPlayerWon(player.getPosition())) {
                    movePlayer(player.moveDown());
                    processPlayerLocation(player.getPosition());
                }
                break;
            case ArrowLeft:
                while (canPlayerMove(player.moveLeft()) && hasPlayerWon(player.getPosition())) {
                    movePlayer(player.moveLeft());
                    processPlayerLocation(player.getPosition());
                }
                break;
            case Character:
                if (Character.toLowerCase(key.getCharacter()) == 'p') {
                    state.setStateType(StateType.PAUSE);
                    state.handleStateController();
                }
                break;
            default :
                break;
        }
    }

    void processPlayerLocation(Position position) throws IOException, InterruptedException {
        collectCoins(position);
        collectStars(position);
        if (level.isOpenBtnAt(position)) level.toggleWalls();
        renderView();
    }

    @Override
    public void renderView() throws IOException {
        screen.setCursorPosition(null);
        screen.startScreen();
        screen.doResizeIfNecessary();

        levelView.draw();
    }

    void movePlayer(Position position) throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (canPlayerMove(position)) {
            player.setPosition(position);
        }
    }

    public boolean canPlayerMove(Position position) throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (level.isWallAt(position))
            return false;

        if (level.isSpikeAt(position)) {
            death();
            return false;
        }

        return true;
    }

    public void death() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        threadSleeper.sleep(200);
        state.setStateType(StateType.GAMEOVER);
        state.handleStateController();
    }

    public void victory() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        threadSleeper.sleep(200);
        setHighestScore();
        state.setLevelScore(level.getScore());
        state.setStateType(StateType.VICTORY);
        state.handleStateController();
    }

    public void setHighestScore() {
        Map activeMap = state.getActiveMap();
        if (activeMap.getHighestScore() < level.getScore()) {
            activeMap.setHighestScore(level.getScore());
            state.setLevelHighestScore(level.getScore());
        }
        else {
            state.setLevelHighestScore(activeMap.getHighestScore());
        }
    }

    public boolean hasPlayerWon(Position position) {
        return !level.isExitAt(position);
    }

    public void collectCoins(Position position) {
        if (level.isCoinAt(position)) {
            level.removeCoin(position);
            level.incrementScore();
        }
    }

    public void collectStars(Position position) {
        if (level.isStarAt(position)) {
            level.removeStar(position);
            level.incrementCollectedStars();
            level.setCanUserExit(level.getCollectedStars() == level.getTotalStars());
        }
    }
}
