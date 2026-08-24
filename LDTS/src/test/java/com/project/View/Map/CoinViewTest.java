package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.Coin;
import com.project.Constants;
import com.project.Model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class CoinViewTest {
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
        Coin coin = new Coin(new Position(1, 1));
        CoinView coinView = new CoinView(elementViewFactory);

        // Act
        coinView.draw(coin, screenMock, graphics);

        // Assert
        verify(elementViewFactory).create(
                Constants.COIN_COLOUR,
                Constants.BACKGROUND_COLOUR,
                coin.getPosition().getX(),
                coin.getPosition().getY(),
                Constants.COIN_SYMBOL
        );

        verify(elementViewFactory, times(1)).create(
                Constants.COIN_COLOUR,
                Constants.BACKGROUND_COLOUR,
                coin.getPosition().getX(),
                coin.getPosition().getY(),
                Constants.COIN_SYMBOL
        );
    }
}
