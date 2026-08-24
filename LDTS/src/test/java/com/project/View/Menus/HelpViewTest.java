package com.project.View.Menus;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class HelpViewTest {
    @Mock
    TextGraphics graphicsMock;

    @Mock
    Screen screenMock;

    HelpView view;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(screenMock.newTextGraphics()).thenReturn(graphicsMock);
        view = spy(new HelpView(screenMock));
    }

    @Test
    public void testDrawMenu() {
        // Arrange
        TextPanelView btnMock = mock(TextPanelView.class);
        String title = "HELP";
        List<String> instructions = Arrays.asList(
                "welcome to tomb of the mask!",
                "",
                "-coins increase your score",
                "-3 stars open the exit",
                "-spikes will kill you",
                "-'o' open hidden walls",
                "",
                "Good luck adventurous!"
        );

        String menu = "[M]menu";
        String quit = "[Q]quit";

        // Act
        view.drawMenu();

        // Assert
        verify(graphicsMock).setBackgroundColor(TextColor.Factory.fromString(Constants.BACKGROUND_COLOUR));
        verify(graphicsMock).fillRectangle(new TerminalPosition(0, 0), new TerminalSize(Constants.TERMINAL_WIDTH, Constants.TERMINAL_HEIGHT), ' ');
        verify(graphicsMock).setForegroundColor(TextColor.Factory.fromString(Constants.PLAYER_COLOUR));
        verify(graphicsMock).enableModifiers(SGR.BOLD);

        verify(graphicsMock).putString(new TerminalPosition(view.center(title.length()), 3), title);

    }
}
