package com.project.View.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import com.project.Model.OpenBtn;
import com.project.Model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class OpenBtnViewTest {
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
        OpenBtn openBtn = new OpenBtn(new Position(1, 1));
        OpenBtnView openBtnView = new OpenBtnView(elementViewFactory);

        // Act
        openBtnView.draw(openBtn, screenMock, graphics);

        // Assert
        verify(elementViewFactory).create(
                Constants.OPENBTN_COLOUR,
                Constants.BACKGROUND_COLOUR,
                openBtn.getPosition().getX(),
                openBtn.getPosition().getY(),
                Constants.OPENBTN_SYMBOL
        );

        verify(elementViewFactory, times(1)).create(
                Constants.OPENBTN_COLOUR,
                Constants.BACKGROUND_COLOUR,
                openBtn.getPosition().getX(),
                openBtn.getPosition().getY(),
                Constants.OPENBTN_SYMBOL
        );
    }
}
