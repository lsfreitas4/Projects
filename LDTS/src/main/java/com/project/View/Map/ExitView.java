package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import com.project.Model.*;

public class ExitView implements ElementView<Exit> {
    private final BlockElementViewFactory elementViewFactory;

    public ExitView(BlockElementViewFactory elementViewFactory) {
        this.elementViewFactory = elementViewFactory;
    }

    @Override
    public void draw(Exit exit, Screen screen, TextGraphics graphics) {
        elementViewFactory.create(
                Constants.EXIT_COLOUR,
                exit.getPosition().getX(),
                exit.getPosition().getY()
        );
    }
}



