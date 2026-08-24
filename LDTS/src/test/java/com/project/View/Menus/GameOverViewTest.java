package com.project.View.Menus;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GameOverViewTest {
    @Mock
    private Screen screenMock;

    @Mock
    private TextGraphics textGraphicsMock;

    GameOverView view;

    @BeforeEach
    public void setup() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        when(screenMock.newTextGraphics()).thenReturn(textGraphicsMock);
        view = new GameOverView(screenMock);
    }

}
