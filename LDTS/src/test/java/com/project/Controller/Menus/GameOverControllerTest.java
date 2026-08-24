package com.project.Controller.Menus;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.State;
import com.project.Model.StateType;
import com.project.View.Menus.GameOverView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static com.project.Model.StateType.QUIT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class GameOverControllerTest {
    @Mock
    State stateMock;

    @Mock
    Screen screenMock;

    @Mock
    GameOverView gameOverViewMock;

    @Mock
    KeyStroke keyMock;

    @InjectMocks
    GameOverController controller;

    @BeforeEach
    public void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        controller = spy(new GameOverController(screenMock, stateMock));
        controller.setMenuView(gameOverViewMock);
    }

    @Test
    void testPlayValidInput() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Arrange
        GameOverView gameOverViewMock = mock(GameOverView.class);
        when(gameOverViewMock.inputListener()).thenReturn(keyMock);
        doReturn(true).when(controller).handleInput(eq(keyMock));
        controller.setMenuView(gameOverViewMock);

        // Act
        boolean result = controller.play();

        // Assert and verify
        assertTrue(result);
        verify(gameOverViewMock, times(1)).inputListener();
        verify(controller, times(1)).handleInput(eq(keyMock));
    }

    @Test
    void testPlayInvalidInput() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Arrange
        GameOverController controller = spy(new GameOverController(screenMock, stateMock));
        GameOverView gameOverViewMock = mock(GameOverView.class);

        when(gameOverViewMock.inputListener()).thenReturn(keyMock);
        doReturn(false).when(controller).handleInput(eq(keyMock));
        controller.setMenuView(gameOverViewMock);

        // Act
        boolean result = controller.play();

        // Assert and verify
        assertFalse(result);
        verify(gameOverViewMock, times(1)).inputListener();
        verify(controller, times(1)).handleInput(eq(keyMock));
    }

    @Test
    public void testProcessKeyWithNonCharacterKey() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        KeyStroke keyMock = new KeyStroke(KeyType.ArrowUp);
        controller.processKey(keyMock);

        verify(stateMock, never()).setStateType(Mockito.any(StateType.class));
        verify(stateMock, never()).handleStateController();
    }

    @Test
    public void testProcessKeyWithInvalidCharacter() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        KeyStroke keyMock = mock(KeyStroke.class);
        Mockito.when(keyMock.getKeyType()).thenReturn(KeyType.Character);
        Mockito.when(keyMock.getCharacter()).thenReturn('o');

        controller.processKey(keyMock);

        verify(stateMock, never()).setStateType(Mockito.any(StateType.class));
        verify(stateMock, never()).handleStateController();
    }

    @Test
    public void testProcessKeyWithMUpperCharacter() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        KeyStroke keyMock = mock(KeyStroke.class);
        Mockito.when(keyMock.getKeyType()).thenReturn(KeyType.Character);
        Mockito.when(keyMock.getCharacter()).thenReturn('m');

        controller.processKey(keyMock);

        verify(stateMock, Mockito.times(1)).setStateType(StateType.MENU);
        verify(stateMock, Mockito.times(1)).handleStateController();
    }

    @Test
    public void testProcessKeyWithMLowerCharacter() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        KeyStroke keyMock = mock(KeyStroke.class);
        Mockito.when(keyMock.getKeyType()).thenReturn(KeyType.Character);
        Mockito.when(keyMock.getCharacter()).thenReturn('M');

        controller.processKey(keyMock);

        verify(stateMock, Mockito.times(1)).setStateType(StateType.MENU);
        verify(stateMock, Mockito.times(1)).handleStateController();
    }

    @Test
    public void testProcessKeyWithRUpperCharacter() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        KeyStroke keyMock = mock(KeyStroke.class);
        Mockito.when(keyMock.getKeyType()).thenReturn(KeyType.Character);
        Mockito.when(keyMock.getCharacter()).thenReturn('r');

        controller.processKey(keyMock);

        verify(stateMock, Mockito.times(1)).setStateType(StateType.LEVEL);
        verify(stateMock, Mockito.times(1)).handleStateController();
    }

    @Test
    public void testProcessKeyWithRLowerCharacter() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        KeyStroke keyMock = mock(KeyStroke.class);
        Mockito.when(keyMock.getKeyType()).thenReturn(KeyType.Character);
        Mockito.when(keyMock.getCharacter()).thenReturn('R');

        controller.processKey(keyMock);

        verify(stateMock, Mockito.times(1)).setStateType(StateType.LEVEL);
        verify(stateMock, Mockito.times(1)).handleStateController();
    }

    @Test
    void testRenderView() throws IOException {
        GameOverView gameOverViewMock = Mockito.mock(GameOverView.class);
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        Mockito.when(screenMock.newTextGraphics()).thenReturn(textGraphics);

        controller.setScreen(screenMock);
        controller.setMenuView(gameOverViewMock);

        controller.renderView();

        verify(screenMock).setCursorPosition(null);
        verify(screenMock).startScreen();
        verify(screenMock).doResizeIfNecessary();
        verify(gameOverViewMock).draw();
    }

    @Test
    void testHandleInputQlowerCase() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        when(keyMock.getKeyType()).thenReturn(KeyType.Character);

        when(keyMock.getCharacter()).thenReturn('q');
        assertFalse(controller.handleInput(keyMock));
        verify(stateMock).setStateType(QUIT);
    }

    @Test
    void testHandleInputQupperCase() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        when(keyMock.getKeyType()).thenReturn(KeyType.Character);

        when(keyMock.getCharacter()).thenReturn('Q');
        assertFalse(controller.handleInput(keyMock));
        verify(stateMock).setStateType(QUIT);
    }

    @Test
    void testHandleInputEOF() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        when(keyMock.getKeyType()).thenReturn(KeyType.EOF);

        assertFalse(controller.handleInput(keyMock));
        verify(stateMock).setStateType(QUIT);
    }

    @Test
    void testHandleInputRegularKey() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        GameOverController controller = spy(new GameOverController(screenMock, stateMock));
        when(keyMock.getKeyType()).thenReturn(KeyType.Character);
        when(keyMock.getCharacter()).thenReturn('a');

        boolean handleInput = controller.handleInput(keyMock);
        assertTrue(handleInput);
        verify(controller, times(1)).processKey(eq(keyMock));
    }
}
