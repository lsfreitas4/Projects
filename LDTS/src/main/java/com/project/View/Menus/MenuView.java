package com.project.View.Menus;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;

import java.io.IOException;

public abstract class MenuView {
    protected final Screen screen;
    protected final TextGraphics graphics;

    public MenuView(Screen screen) {
        this.screen = screen;
        this.graphics = screen.newTextGraphics();
    }

    public KeyStroke inputListener() throws IOException {
        return screen.readInput();
    }

    public int center(int size) {
        return (Constants.TERMINAL_WIDTH - size) / 2;
    }

    protected abstract void drawMenu();

    public void draw() throws IOException {
        screen.clear();
        drawMenu();
        screen.refresh();
    }
}
