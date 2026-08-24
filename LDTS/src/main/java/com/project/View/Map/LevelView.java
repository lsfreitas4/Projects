package com.project.View.Map;

import com.project.Constants;
import com.project.Model.*;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;

public class LevelView extends MapView<Level> {
    private final StringElementViewFactory stringElementViewFactory = new StringElementViewFactory(graphics);
    private final BlockElementViewFactory blockElementViewFactory = new BlockElementViewFactory(graphics);

    public LevelView(Level level, Screen screen) {
        super(level, screen);
    }

    @Override
    public void drawElements() {
        // Paint the Map floor
        graphics.setBackgroundColor(TextColor.Factory.fromString(Constants.BACKGROUND_COLOUR));
        graphics.fillRectangle(new TerminalPosition(0, Constants.HEADER_HEIGHT), new TerminalSize(Constants.TERMINAL_WIDTH, Constants.TERMINAL_HEIGHT), ' ');

        drawElements(screen, getModel().getWalls(), new WallView(blockElementViewFactory));
        drawElements(screen, getModel().getSpikes(), new SpikeView(stringElementViewFactory));
        drawElements(screen, getModel().getStars(), new StarView(stringElementViewFactory));
        drawElements(screen, getModel().getCoins(), new CoinView(stringElementViewFactory));

        // Draw Exit
        if (getModel().getCollectedStars() == getModel().getTotalStars())
            drawElement(screen, getModel().getExit(), new ExitView(blockElementViewFactory));
        else drawElement(screen, new Wall(getModel().getExit().getPosition()), new WallView(blockElementViewFactory));

        // Draw moving walls
        if (getModel().getToggleWallsState() && !getModel().getToggledWalls().isEmpty()) {
            drawElements(screen, getModel().getToggledWalls(), new WallView(blockElementViewFactory));
        }

        // Draw opening buttons
        drawElements(screen, getModel().getOpenBtns(), new OpenBtnView(stringElementViewFactory));

        // Draw score
        drawScore();

        // Draw player
        drawElement(screen, getModel().getPlayer(), new PlayerView(stringElementViewFactory));
    }

    public <T extends Element> void drawElements(Screen screen, List<T> elements, ElementView<T> view) {
        for (T element : elements)
            drawElement(screen, element, view);
    }

    public <T extends Element> void drawElement(Screen screen, T element, ElementView<T> view) {
        view.draw(element, screen, graphics);
    }

    public void drawScore() {
        // Paint the header
        graphics.setBackgroundColor(TextColor.Factory.fromString(Constants.HEADER_COLOUR));
        graphics.fillRectangle(new TerminalPosition(0, 0), new TerminalSize(Constants.TERMINAL_WIDTH, Constants.HEADER_HEIGHT), ' ');

        // Set font colour
        graphics.setForegroundColor(TextColor.Factory.fromString(Constants.PLAYER_COLOUR));

        // Score counter
        graphics.putString(new TerminalPosition(0, 1), String.format("score:%s", getModel().getScore()));

        // Stars counter
        graphics.putString(new TerminalPosition(0, 2), "*".repeat(Math.max(0, getModel().getCollectedStars())));
    }
}
