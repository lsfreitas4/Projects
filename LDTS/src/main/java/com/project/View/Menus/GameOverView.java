package com.project.View.Menus;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;

public class GameOverView extends MenuView {
    public GameOverView(Screen screen) {
        super(screen);
    }

    @Override
    protected void drawMenu() {
        // Set the background color
        graphics.setBackgroundColor(TextColor.Factory.fromString(Constants.BACKGROUND_COLOUR));
        graphics.fillRectangle(new TerminalPosition(0, 0), new TerminalSize(Constants.TERMINAL_WIDTH, Constants.TERMINAL_HEIGHT), ' ');

        // Font colour
        graphics.setForegroundColor(TextColor.Factory.fromString(Constants.PLAYER_COLOUR));
        graphics.enableModifiers(SGR.BOLD);

        // Text
        String title = "GAME OVER :(";
        String menu = "[M]menu";
        String restart = "[R]restart";
        String quit = "[Q]quit";

        // Draw game over
        graphics.putString(new TerminalPosition(center(title.length()), 3), title);

        // Draw [M]menu & [R]restart
        TextPanelView btn = new TextPanelView(screen);
        TerminalSize terminalSize = new TerminalSize(restart.length() + 2, 3);

        btn.draw(menu, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(8, 6), terminalSize);
        btn.draw(restart, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR,new TerminalPosition(8, 10), terminalSize);
        btn.draw(quit, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR,new TerminalPosition(8, 14), terminalSize);
    }
}

