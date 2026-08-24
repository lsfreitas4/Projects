package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import com.project.Model.Spike;

public class SpikeView implements ElementView <Spike> {
    private final StringElementViewFactory elementViewFactory;

    public SpikeView(StringElementViewFactory elementViewFactory) {
        this.elementViewFactory = elementViewFactory;
    }

    @Override
    public void draw(Spike spike, Screen screen, TextGraphics graphics) {
        elementViewFactory.create(
                Constants.SPIKES_COLOUR,
                Constants.BACKGROUND_COLOUR,
                spike.getPosition().getX(),
                spike.getPosition().getY(),
                Constants.SPIKE_SYMBOL
        );
    }
}
