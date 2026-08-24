package com.project.View.Menus;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;

public class PauseView extends MenuView {

    public PauseView(Screen screen) {
        super(screen);
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
        String title = "PAUSED";
        String cont = "[C]continue";
        String restart = "[R]restart";
        String menu = "[M]menu";
        String quit = "[Q]quit";

        // Draw title
        graphics.putString(new TerminalPosition(3, 9), title);
        graphics.putString(new TerminalPosition(5, 11), "||");

        // Draw buttons
        TextPanelView btn = new TextPanelView(screen);

        TerminalSize terminalSize = new TerminalSize(cont.length() + 2, 3);
        int column = 12;
        int startRow = 3;

        btn.draw(cont, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR,new TerminalPosition(column, startRow), terminalSize);
        btn.draw(restart, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR,new TerminalPosition(column, startRow + 4), terminalSize);
        btn.draw(menu, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(column, startRow + 8), terminalSize);
        btn.draw(quit, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(column, startRow + 12), terminalSize);


    }
}
