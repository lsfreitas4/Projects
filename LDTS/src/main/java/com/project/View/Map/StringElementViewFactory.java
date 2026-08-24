package com.project.View.Map;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

public class StringElementViewFactory {
    private final TextGraphics graphics;

    public StringElementViewFactory(TextGraphics graphics) {
        this.graphics = graphics;
    }

    public void create(
            String colour,
            String backgroundColour,
            int x,
            int y,
            String symbol) {
        graphics.setForegroundColor(TextColor.Factory.fromString(colour));
        graphics.setBackgroundColor(TextColor.Factory.fromString(backgroundColour));
        graphics.enableModifiers(SGR.BOLD);
        graphics.putString(new TerminalPosition(x, y), symbol);
    }
}
