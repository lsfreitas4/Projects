package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.Spike;
import com.project.Constants;
import com.project.Model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class SpikeViewTest {
    @Mock
    private Screen screenMock;

    @Mock
    private TextGraphics graphics;

    private StringElementViewFactory elementViewFactory;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        elementViewFactory = spy(new StringElementViewFactory(graphics));
        when(screenMock.newTextGraphics()).thenReturn(graphics);
    }

    @Test
    void testDraw() {
        // Arrange
        Spike spike = new Spike(new Position(1, 1));
        SpikeView spikeView = new SpikeView(elementViewFactory);

        // Act
        spikeView.draw(spike, screenMock, graphics);

        // Assert
        verify(elementViewFactory).create(
                Constants.SPIKES_COLOUR,
                Constants.BACKGROUND_COLOUR,
                spike.getPosition().getX(),
                spike.getPosition().getY(),
                Constants.SPIKE_SYMBOL
        );

        verify(elementViewFactory, times(1)).create(
                Constants.SPIKES_COLOUR,
                Constants.BACKGROUND_COLOUR,
                spike.getPosition().getX(),
                spike.getPosition().getY(),
                Constants.SPIKE_SYMBOL
        );
    }
}
