package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.Element;

public interface ElementView <T extends Element> {
    void draw(T element, Screen screen, TextGraphics graphics);
}
