package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import com.project.Model.Player;
import com.project.Model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class PlayerViewTest {
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
        Player player = new Player(new Position(1, 1));
        PlayerView playerView = new PlayerView(elementViewFactory);

        // Act
        playerView.draw(player, screenMock, graphics);

        // Assert
        verify(elementViewFactory).create(
                Constants.PLAYER_COLOUR,
                Constants.BACKGROUND_COLOUR,
                player.getPosition().getX(),
                player.getPosition().getY(),
                Constants.PLAYER_SYMBOL
        );

        verify(elementViewFactory, times(1)).create(
                Constants.PLAYER_COLOUR,
                Constants.BACKGROUND_COLOUR,
                player.getPosition().getX(),
                player.getPosition().getY(),
                Constants.PLAYER_SYMBOL
        );
    }
}
