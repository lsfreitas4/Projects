package com.project.View.Menus;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import com.project.Model.Map;

import java.util.List;

public class SelectLevelView extends MenuView {
    private List<Map> maps;

    public SelectLevelView(Screen screen) {
        super(screen);
    }

    public void setMaps(List<Map> maps) {
        this.maps = maps;
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
        String title = "SELECT LEVEL";
        String menu = "[M]menu";
        String quit = "[Q]quit";

        // Draw menu title
        graphics.putString(new TerminalPosition(center(title.length()), 5), title);

        // Draw levels
        int column = 10;
        for (int i = 0; i < maps.size(); ++i) {
            int level = i + 1;
            drawLevelBox(level, maps.get(i).getHighestScore(), column, 8);
            column += 5;
        }

        // Draw buttons
        TextPanelView btn = new TextPanelView(screen);
        TerminalSize terminalSize = new TerminalSize(menu.length() + 2, 1);

        btn.draw(menu, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(2, 18), terminalSize);
        btn.draw(quit, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(17, 18), terminalSize);
    }

    private void drawLevelBox(int level, int score, int column, int row) {
        TerminalSize terminalSize = new TerminalSize(3, 3);
        TextPanelView btn = new TextPanelView(screen);

        btn.draw(String.valueOf(level), Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(column, row), terminalSize);

        String highestScore = String.valueOf(score);
        if (highestScore.length() != 3) column += 1;

        if (score != 0)
            graphics.putString(new TerminalPosition(column, 11), highestScore);
    }

}
