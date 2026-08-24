package com.project.Controller.Menus;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.project.Model.State;
import com.project.Model.StateType;
import com.project.View.Menus.VictoryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static com.project.Model.StateType.QUIT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VictoryControllerTest {
    @Mock
    State stateMock;

    @Mock
    Screen screenMock;

    @Mock
    KeyStroke keyMock;

    @Mock
    VictoryView viewMock;

    @InjectMocks
    VictoryController controller;

    @BeforeEach
    public void setUp() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        controller = spy(new VictoryController(screenMock, stateMock));
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
        VictoryController controllerSpy = spy(new VictoryController(screenMock, stateMock));
        when(keyMock.getKeyType()).thenReturn(KeyType.Character);
        when(keyMock.getCharacter()).thenReturn('a');

        boolean handleInput = controllerSpy.handleInput(keyMock);
        assertTrue(handleInput);
        verify(controllerSpy, times(1)).processKey(eq(keyMock));
    }

    @Test
    public void processKey_CharacterL_SetStateToMenu() throws Exception {
        // Arrange
        when(keyMock.getKeyType()).thenReturn(KeyType.Character);
        when(keyMock.getCharacter()).thenReturn('l');

        // Act
        controller.processKey(keyMock);

        // Assert
        verify(stateMock).setStateType(StateType.SELECT_LEVEL);
        verify(stateMock).handleStateController();
    }

    @Test
    public void processKey_CharacterM_SetStateToMenu() throws Exception {
        // Arrange
        when(keyMock.getKeyType()).thenReturn(KeyType.Character);
        when(keyMock.getCharacter()).thenReturn('m');

        // Act
        controller.processKey(keyMock);

        // Assert
        verify(stateMock).setStateType(StateType.MENU);
        verify(stateMock).handleStateController();
    }

    @Test
    public void processKey_DefaultCase_DoNothing() throws Exception {
        // Arrange
        when(keyMock.getKeyType()).thenReturn(KeyType.Character);
        when(keyMock.getCharacter()).thenReturn('x');

        // Act
        controller.processKey(keyMock);

        // Assert
        verify(stateMock, never()).setStateType(any());
        verify(stateMock, never()).setActiveLevel(anyInt());
        verify(stateMock, never()).handleStateController();
    }

    @Test
    public void play_KeyReturned_HandleInputCalled() throws Exception {
        // Arrange
        when(viewMock.inputListener()).thenReturn(keyMock);
        doReturn(true).when(controller).handleInput(eq(keyMock));
        controller.setMenuView(viewMock);

        // Act
        boolean result = controller.play();

        // Assert
        assertTrue(result);
        verify(viewMock, times(1)).inputListener();
        verify(controller, times(1)).handleInput(eq(keyMock));
    }

    @Test
    public void play_KeyReturned_HandleInputNotCalled() throws Exception {
        // Arrange
        when(viewMock.inputListener()).thenReturn(keyMock);
        doReturn(false).when(controller).handleInput(eq(keyMock));
        controller.setMenuView(viewMock);

        // Act
        boolean result = controller.play();

        // Assert
        assertFalse(result);
        verify(viewMock, times(1)).inputListener();
        verify(controller, times(1)).handleInput(eq(keyMock));
    }

    @Test
    void renderView_Success() throws IOException {
        // Arrange
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        Mockito.when(screenMock.newTextGraphics()).thenReturn(textGraphics);
        controller.setScreen(screenMock);
        controller.setMenuView(viewMock);

        // Act
        controller.renderView();

        // Assert
        verify(screenMock).setCursorPosition(null);
        verify(screenMock).startScreen();
        verify(screenMock).doResizeIfNecessary();
        verify(viewMock).draw();
    }
}
