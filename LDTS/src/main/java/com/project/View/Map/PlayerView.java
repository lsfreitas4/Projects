package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.project.Constants;
import com.project.Model.Player;
import com.googlecode.lanterna.screen.Screen;

public class PlayerView implements ElementView<Player> {
    private final StringElementViewFactory elementViewFactory;

    public PlayerView(StringElementViewFactory elementViewFactory) {
        this.elementViewFactory = elementViewFactory;
    }

    @Override
    public void draw(Player player, Screen screen, TextGraphics graphics) {
        elementViewFactory.create(
                Constants.PLAYER_COLOUR,
                Constants.BACKGROUND_COLOUR,
                player.getPosition().getX(),
                player.getPosition().getY(),
                Constants.PLAYER_SYMBOL
        );
    }
}
