package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.Star;
import com.project.Constants;

public class StarView implements ElementView <Star> {
    private final StringElementViewFactory elementViewFactory;

    public StarView(StringElementViewFactory elementViewFactory) {
        this.elementViewFactory = elementViewFactory;
    }

    @Override
    public void draw(Star star, Screen screen, TextGraphics graphics) {
        elementViewFactory.create(
                Constants.STAR_COLOUR,
                Constants.BACKGROUND_COLOUR,
                star.getPosition().getX(),
                star.getPosition().getY(),
                Constants.STAR_SYMBOL
        );
    }
}
