package com.project.View.Menus;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;

public class MainMenuView extends MenuView {
    public MainMenuView(Screen screen) {
        super (screen);
    }

    @Override
    public void drawMenu() {
        // Set the background color
        //String fillCharacter = ""; new TextCharacter(fillCharacter.charAt(0))
        graphics.setBackgroundColor(TextColor.Factory.fromString(Constants.BACKGROUND_COLOUR));
        graphics.fillRectangle(new TerminalPosition(0, 0), new TerminalSize(Constants.TERMINAL_WIDTH, Constants.TERMINAL_HEIGHT), ' ');

        // Font colour
        graphics.setForegroundColor(TextColor.Factory.fromString(Constants.PLAYER_COLOUR));
        graphics.enableModifiers(SGR.BOLD);

        // Text
        String title = "TOMB OF THE MASK";
        String play = "[P]play";
        String help = "[H]help";
        String quit = "[Q]quit";

        // Draw MENU
        graphics.putString(new TerminalPosition(center(title.length()), 3), title);

        // Draw [P]play & [H]help & [Q]quit
        TextPanelView btn = new TextPanelView(screen);
        TerminalSize terminalSize = new TerminalSize(help.length() + 2, 3);

        btn.draw(play, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(center(play.length()), 6), terminalSize);
        btn.draw(help, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR,new TerminalPosition(center(help.length()), 10), terminalSize);
        btn.draw(quit, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR,new TerminalPosition(center(quit.length()), 14), terminalSize);
        
    }
}
