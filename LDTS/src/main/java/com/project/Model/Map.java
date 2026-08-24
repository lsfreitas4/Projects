package com.project.Model;

import java.util.List;

public class Map {
    private final List<String> mapArray;
    private int highestScore;

    public Map(List<String> mapArray) {
        this.mapArray = mapArray;
        this.highestScore = 0;
    }

    public List<String> getMapArray() {
        return mapArray;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int score) {
        this.highestScore = score;
    }
}
