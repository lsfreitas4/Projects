package com.project.View.Map;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

public class BlockElementViewFactory {
    private final TextGraphics graphics;

    public BlockElementViewFactory(TextGraphics graphics) {
        this.graphics = graphics;
    }

    public void create(
            String colour,
            int x,
            int y) {
        TextColor backgroundColour = TextColor.Factory.fromString(colour);
        graphics.setBackgroundColor(backgroundColour);
        graphics.fillRectangle(new TerminalPosition(x, y), new TerminalSize(1, 1), ' ');
    }
}
