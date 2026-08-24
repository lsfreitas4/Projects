package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;

public abstract class MapView<T> {
    private final T model;
    protected final Screen screen;
    protected final TextGraphics graphics;

    public MapView(T model, Screen screen) {
        this.model = model;
        this.screen = screen;
        this.graphics = screen.newTextGraphics();
    }

    public KeyStroke inputListener() throws IOException {
        return screen.readInput();
    }

    protected abstract void drawElements();

    public void draw() throws IOException {
        screen.clear();
        drawElements();
        screen.refresh();
    }

    public T getModel() {
        return model;
    }
}



