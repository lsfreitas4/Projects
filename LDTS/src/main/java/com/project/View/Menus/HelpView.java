package com.project.View.Menus;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;

import java.util.Arrays;
import java.util.List;

public class HelpView extends MenuView {
    public HelpView(Screen screen) {
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
        String title = "HELP";
        List<String> instructions = Arrays.asList(
                "welcome to tomb of the mask!",
                "",
                "-coins increase your score",
                "-3 stars open the exit",
                "-spikes will kill you",
                "-'o' open hidden walls",
                "",
                "Good luck adventurous!"
                );

        String menu = "[M]menu";
        String quit = "[Q]quit";

        // Draw Help
        graphics.putString(new TerminalPosition(center(title.length()), 3), title);

        // Draw Instructions
        for (int i = 0; i < instructions.size(); ++i) {
            graphics.putString(new TerminalPosition(0, 7 + i), instructions.get(i));
        }

        // Draw Buttons
        TextPanelView btn = new TextPanelView(screen);

        TerminalSize terminalSize = new TerminalSize(menu.length() + 2, 1);

        btn.draw(menu, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(2, 18), terminalSize);
        btn.draw(quit, Constants.BACKGROUND_COLOUR , Constants.PLAYER_COLOUR, new TerminalPosition(17, 18), terminalSize);
    }
}