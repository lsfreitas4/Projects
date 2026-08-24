package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import com.project.Model.Exit;
import com.project.Model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class ExitViewTest {
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
        Exit exit = new Exit(new Position(1, 1));
        ExitView exitView = new ExitView(elementViewFactory);

        // Act
        exitView.draw(exit, screenMock, graphics);

        // Assert
        verify(elementViewFactory).create(
                Constants.EXIT_COLOUR,
                exit.getPosition().getX(),
                exit.getPosition().getY()
        );

        verify(elementViewFactory, times(1)).create(
                Constants.EXIT_COLOUR,
                exit.getPosition().getX(),
                exit.getPosition().getY()
        );
    }
}
