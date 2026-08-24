package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.Coin;
import com.project.Constants;

public class CoinView implements ElementView<Coin> {
    private final StringElementViewFactory elementViewFactory;

    public CoinView(StringElementViewFactory elementViewFactory) {
        this.elementViewFactory = elementViewFactory;
    }

    @Override
    public void draw(Coin coin, Screen screen, TextGraphics graphics) {
        elementViewFactory.create(
                Constants.COIN_COLOUR,
                Constants.BACKGROUND_COLOUR,
                coin.getPosition().getX(),
                coin.getPosition().getY(),
                Constants.COIN_SYMBOL
        );
    }
}
