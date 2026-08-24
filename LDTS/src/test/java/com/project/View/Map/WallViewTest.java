package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import com.project.Model.Position;
import com.project.Model.Wall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class WallViewTest {
    @Mock
    private Screen screenMock;

    @Mock
    private TextGraphics graphics;

    private BlockElementViewFactory elementViewFactory;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        elementViewFactory = spy(new BlockElementViewFactory(graphics));
        when(screenMock.newTextGraphics()).thenReturn(graphics);
    }

    @Test
    void testDraw() {
        // Arrange
        Wall wall = new Wall(new Position(1, 1));
        WallView wallView = new WallView(elementViewFactory);

        // Act
        wallView.draw(wall, screenMock, graphics);

        // Assert
        verify(elementViewFactory).create(
                Constants.WALLS_COLOUR,
                wall.getPosition().getX(),
                wall.getPosition().getY()
        );

        verify(elementViewFactory, times(1)).create(
                Constants.WALLS_COLOUR,
                wall.getPosition().getX(),
                wall.getPosition().getY()
        );
    }
}
