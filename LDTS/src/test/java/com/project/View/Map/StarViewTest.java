package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import com.project.Model.Position;
import com.project.Model.Star;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class StarViewTest {
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
        Star star = new Star(new Position(1, 1));
        StarView starView = new StarView(elementViewFactory);

        // Act
        starView.draw(star, screenMock, graphics);

        // Assert
        verify(elementViewFactory).create(
                Constants.STAR_COLOUR,
                Constants.BACKGROUND_COLOUR,
                star.getPosition().getX(),
                star.getPosition().getY(),
                Constants.STAR_SYMBOL
        );

        verify(elementViewFactory, times(1)).create(
                Constants.STAR_COLOUR,
                Constants.BACKGROUND_COLOUR,
                star.getPosition().getX(),
                star.getPosition().getY(),
                Constants.STAR_SYMBOL
        );
    }
}
