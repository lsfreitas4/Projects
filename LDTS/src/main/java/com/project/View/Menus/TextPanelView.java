package com.project.View.Menus;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public class TextPanelView {
    final TextGraphics graphics;
    public TextPanelView(Screen screen) {
        this.graphics = screen.newTextGraphics();
    }

    public void draw(String text, String textColour, String panelColour, TerminalPosition terminalPosition, TerminalSize terminalSize) {
        terminalSize = terminalSize == null ? new TerminalSize(text.length() + 2, 3) : terminalSize;

        TextColor backgroundColor = TextColor.Factory.fromString(panelColour);
        graphics.setBackgroundColor(backgroundColor);
        graphics.fillRectangle(terminalPosition, terminalSize, ' ');

        graphics.setForegroundColor(TextColor.Factory.fromString(textColour));

        int x = terminalPosition.getColumn() + 1;
        int y = terminalPosition.getRow() + terminalSize.getRows() / 2;

        // Draw the text in the center of the panel
        graphics.putString(new TerminalPosition(x, y), text);
    }
}
