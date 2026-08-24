package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import com.project.Model.*;

public class OpenBtnView implements ElementView<OpenBtn> {
    private final StringElementViewFactory elementViewFactory;

    public OpenBtnView(StringElementViewFactory elementViewFactory) {
        this.elementViewFactory = elementViewFactory;
    }

    @Override
    public void draw(OpenBtn openBtn, Screen screen, TextGraphics graphics) {
        elementViewFactory.create(
                Constants.OPENBTN_COLOUR,
                Constants.BACKGROUND_COLOUR,
                openBtn.getPosition().getX(),
                openBtn.getPosition().getY(),
                Constants.OPENBTN_SYMBOL
        );
    }
}
