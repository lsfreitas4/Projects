package com.project.View.Menus;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;

public class VictoryView extends MenuView {
    private int score;
    private int highestScore;
    public VictoryView(Screen screen) {
        super (screen);
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    @Override
    public void drawMenu() {
        // Set the background color
        graphics.setBackgroundColor(TextColor.Factory.fromString(Constants.BACKGROUND_COLOUR));
        graphics.fillRectangle(new TerminalPosition(0, 0), new TerminalSize(Constants.TERMINAL_WIDTH, Constants.TERMINAL_HEIGHT), ' ');

        // Font colour
        graphics.setForegroundColor(TextColor.Factory.fromString(Constants.PLAYER_COLOUR));
        graphics.enableModifiers(SGR.BOLD);

        // Text
        String title = "VICTORY!";
        String score = this.highestScore > this.score ? "score: " + this.score : "new highest score: " + this.highestScore;
        String menu = "[M]menu";
        String level = "[L]levels";
        String quit = "[Q]quit";

        // Draw Victory!
        graphics.putString(new TerminalPosition(center(title.length()), 3), title);

        // Draw Score
        graphics.putString(new TerminalPosition(center(score.length()), 5), score);

        // Draw Buttons
        TextPanelView btn = new TextPanelView(screen);
        TerminalSize terminalSize = new TerminalSize(level.length() + 2, 3);

        btn.draw(menu, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(8, 8), terminalSize);
        btn.draw(level, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(8, 12), terminalSize);
        btn.draw(quit, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(8, 16), terminalSize);
    }
}
