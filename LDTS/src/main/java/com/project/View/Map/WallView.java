package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.project.Constants;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.Wall;

public class WallView implements ElementView<Wall> {
    private final BlockElementViewFactory elementViewFactory;

    public WallView(BlockElementViewFactory elementViewFactory) {
        this.elementViewFactory = elementViewFactory;
    }

    @Override
    public void draw(Wall wall, Screen screen, TextGraphics graphics) {
        elementViewFactory.create(
                Constants.WALLS_COLOUR,
                wall.getPosition().getX(),
                wall.getPosition().getY()
        );
    }
}
