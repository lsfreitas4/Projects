package com.project.Model;

import com.project.Constants;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private final Player player;
    private final List<Wall> walls = new ArrayList<>();
    private final List<Spike> spikes = new ArrayList<>();
    private List<Coin> coins = new ArrayList<>();
    private final List<OpenBtn> openBtns = new ArrayList<>();
    private final List<Wall> toggledWalls = new ArrayList<>();
    private List<Star> stars = new ArrayList<>();
    private Exit exit;
    private int score;
    private int collectedStars;
    private int totalStars;
    private boolean toggledWallsState;
    private boolean canUserExit;
    private List<String> activeMapArray;

    public Level() {
        // Set initial values
        player = new Player(Constants.PLAYER_INIT_POSITION);
        score = 0;
        totalStars = 0;
        collectedStars = 0;
        toggledWallsState = true;
    }

    public void setState(State state) {
    }

    public void setExit(Exit exit) {
        this.exit = exit;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setStars(List<Star> stars) {
        this.stars = stars;
    }

    public void setCoins(List<Coin> coins) {
        this.coins = coins;
    }

    public void setCollectedStars(int collectedStars) {
        this.collectedStars = collectedStars;
    }

    public void setTotalStars(int totalStars) {
        this.totalStars = totalStars;
    }

    public void setToggledWallsState(boolean toggledWallsState) {
        this.toggledWallsState = toggledWallsState;
    }

    public void setCanUserExit(boolean canUserExit) {
        this.canUserExit = canUserExit;
    }

    public void setActiveMapArray(List<String> activeMapArray) {
        this.activeMapArray = activeMapArray;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public List<Spike> getSpikes() {
        return spikes;
    }

    public Exit getExit() {
        return exit;
    }

    public List<Coin> getCoins() {
        return coins;
    }

    public List<Star> getStars() {
        return stars;
    }

    public List<OpenBtn> getOpenBtns() {
        return openBtns;
    }

    public List<Wall> getToggledWalls() {
        return toggledWalls;
    }

    public int getScore() {
        return score;
    }

    public int getCollectedStars() {
        return collectedStars;
    }

    public int getTotalStars() {
        return totalStars;
    }

    public boolean getToggleWallsState() {
        return toggledWallsState;
    }

    public List<String> getActiveMapArray() {
        return activeMapArray;
    }

    public boolean isToggledWallsState() {
        return toggledWallsState;
    }

    public boolean isCanUserExit() {
        return canUserExit;
    }

    public void toggleWalls() {
        toggledWallsState = !toggledWallsState;
    }

    public void incrementScore() {
        score++;
    }

    public void incrementCollectedStars() {
        collectedStars++;
    }

    public boolean isWallAt(Position position) {
        // When all stars haven't been collected, exit is a wall
        if (!canUserExit && exit.getPosition().getX() == position.getX() && exit.getPosition().getY() == position.getY())
            return true;

        // Toggled walls
        if (toggledWallsState) {
            for (Wall wall : toggledWalls) {
                if (wall.getPosition().getX() == position.getX() && wall.getPosition().getY() == position.getY())
                    return true;
            }
        }

        for (Wall wall : walls) {
            if (wall.getPosition().getX() == position.getX() && wall.getPosition().getY() == position.getY())
                return true;
        }

        return false;
    }

    public boolean isSpikeAt(Position position) {
        for (Spike spike : spikes) {
            if (spike.getPosition().getX() == position.getX() && spike.getPosition().getY() == position.getY())
                return true;
        }
        return false;
    }

    public boolean isCoinAt(Position position) {
        for (Coin coin : coins) {
            if (coin.getPosition().getX() == position.getX() && coin.getPosition().getY() == position.getY()) return true;
        }
        return false;
    }
    public boolean isStarAt(Position position) {
        for (Star star : stars) {
            if (star.getPosition().getX() == position.getX() && star.getPosition().getY() == position.getY()) return true;
        }
        return false;
    }

    public boolean isOpenBtnAt(Position position) {
        for (OpenBtn openBtn : openBtns) {
            if (openBtn.getPosition().getX() == position.getX() && openBtn.getPosition().getY() == position.getY()) return true;
        }
        return false;
    }

    public boolean isExitAt(Position position) {
        return exit.getPosition().getX() == position.getX() &&
                exit.getPosition().getY() == position.getY() &&
                canUserExit;
    }

    public boolean canUserExit() {
        return collectedStars == totalStars;
    }

    public void removeCoin(Position position) {
        coins.removeIf(coin -> coin.getPosition().getX() == position.getX() && coin.getPosition().getY() == position.getY());
    }

    public void removeStar(Position position) {
        stars.removeIf(star -> star.getPosition().getX() == position.getX() && star.getPosition().getY() == position.getY());
    }

    public void initializeMapArray(State state) {
        activeMapArray = state.getActiveMap().getMapArray();
    }

    public void unpackMap() {
        int rows = activeMapArray.size();
        int cols = activeMapArray.get(0).length();

        for (int i = 0; i < rows; i++) {
            String row = activeMapArray.get(i);
            for (int j = 0; j < cols; j++) {
                char symbol = row.charAt(j);
                Position position = new Position(j, Constants.HEADER_HEIGHT + i);

                // Create Wall, Spike or Exit based on symbol
                switch (symbol) {
                    case 'w' :
                        walls.add(new Wall(position));
                        break;
                    case '/' :
                        spikes.add(new Spike(position));
                        break;
                    case '-' :
                        exit = new Exit(position);
                        break;
                    case '.' :
                        coins.add(new Coin(position));
                        break;
                    case '*' :
                        stars.add(new Star(position));
                        totalStars = stars.size();
                        break;
                    case 'o' :
                        openBtns.add(new OpenBtn(position));
                        break;
                    case 'm' :
                        toggledWalls.add(new Wall(position));
                    default :
                        break;
                }
            }
        }
    }
}
