package com.project.Model;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.project.Controller.Map.LevelController;
import com.project.Controller.Menus.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StateTest {
    @Mock
    private Screen screenMock;

    @InjectMocks
    private State state;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        state = Mockito.spy(new State(screenMock));
    }

    @Test
    void testInitialState() {
        assertNull(state.getController());
        assertNull(state.getStateType());
        assertEquals(0, state.getActiveLevel());
        assertEquals(0, state.getLevelScore());
        assertEquals(0, state.getLevelHighestScore());
        assertNotNull(state.getMaps());
        assertFalse(state.getMaps().isEmpty());
    }

    @Test
    void testHandleStateControllerMenuSuccess() throws InterruptedException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Arrange
        state.setStateType(StateType.MENU);
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        when(screenMock.newTextGraphics()).thenReturn(textGraphics);

        // Act
        state.handleStateController();

        // Assert
        assertNotNull(state.getController());
        assertTrue(state.getController() instanceof MainMenuController);
    }

    @Test
    public void testHandleStateControllerSelectLevelSuccess() throws InterruptedException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Arrange
        state.setStateType(StateType.SELECT_LEVEL);
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        when(screenMock.newTextGraphics()).thenReturn(textGraphics);

        // Act
        state.handleStateController();

        // Assert
        assertNotNull(state.getController());
        assertTrue(state.getController() instanceof SelectLevelController);
    }

    @Test
    public void testHandleStateControllerHelpSuccess() throws InterruptedException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Arrange
        state.setStateType(StateType.HELP);
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        when(screenMock.newTextGraphics()).thenReturn(textGraphics);

        // Act
        state.handleStateController();

        // Assert
        assertNotNull(state.getController());
        assertTrue(state.getController() instanceof HelpController);
    }

    @Test
    public void testHandleStateControllerLevelSuccess() throws InterruptedException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Arrange
        state.setStateType(StateType.LEVEL);
        state.setActiveLevel(1);
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        when(screenMock.newTextGraphics()).thenReturn(textGraphics);

        // Act
        state.handleStateController();

        // Assert
        assertNotNull(state.getController());
        assertTrue(state.getController() instanceof LevelController);
        assertEquals(0, state.getLevelScore());
        assertEquals(0, state.getLevelHighestScore());
    }

    @Test
    public void testHandleStateControllerGameOverSuccess() throws InterruptedException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Arrange
        state.setStateType(StateType.GAMEOVER);
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        when(screenMock.newTextGraphics()).thenReturn(textGraphics);

        // Act
        state.handleStateController();

        // Assert
        assertNotNull(state.getController());
        assertTrue(state.getController() instanceof GameOverController);
    }

    @Test
    public void testHandleStateControllerPauseSuccess() throws InterruptedException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Arrange
        state.setStateType(StateType.PAUSE);
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        when(screenMock.newTextGraphics()).thenReturn(textGraphics);

        // Act
        state.handleStateController();

        // Assert
        assertNotNull(state.getController());
        assertTrue(state.getController() instanceof PauseController);
    }

    @Test
    void handleStateController_ResumeStateOldControllerNotNull_ControllerRestored() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Arrange
        state.setStateType(StateType.RESUME);
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        when(screenMock.newTextGraphics()).thenReturn(textGraphics);
        state.setOldController(new MainMenuController(screenMock, state));

        // Act
        state.handleStateController();

        // Assert
        assertTrue(state.getController() instanceof MainMenuController);
    }

    @Test
    public void handleStateController_VictoryState_Success() throws InterruptedException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        // Arrange
        state.setStateType(StateType.VICTORY);
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        when(screenMock.newTextGraphics()).thenReturn(textGraphics);

        // Act
        state.handleStateController();

        // Assert
        assertNotNull(state.getController());
        assertTrue(state.getController() instanceof VictoryController);
    }
}
