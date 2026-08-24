package com.project.Controller.Map;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.project.Controller.ThreadSleeper;
import com.project.Model.*;
import com.project.View.Map.LevelView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LevelControllerTest {
    @Mock
    private State stateMock;

    @Mock
    private Screen screenMock;

    @Mock
    private KeyStroke keyMock;

    @Mock
    private LevelView viewMock;

    @Mock
    private Map activeMapMock;

    @Mock
    private Level levelMock;

    @Mock
    private ThreadSleeper threadSleeperMock;

    LevelController controller;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mocking the unpackMap behaviour
        List<String> mapArray = Arrays.asList("...", "...", "...");
        when(stateMock.getActiveMap()).thenReturn(activeMapMock);
        when(activeMapMock.getMapArray()).thenReturn(mapArray);

        controller = spy(new LevelController(screenMock, stateMock));

        LevelView viewMock = Mockito.mock(LevelView.class);
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        Mockito.when(screenMock.newTextGraphics()).thenReturn(textGraphics);

        controller.setLevelView(viewMock);
        controller.setThreadSleeper(threadSleeperMock);
    }

    @Test
    public void play_KeyReturned_HandleInputCalled() throws Exception {
        // Arrange
        when(viewMock.inputListener()).thenReturn(keyMock);
        doReturn(true).when(controller).handleInput(eq(keyMock));
        controller.setLevelView(viewMock);

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
        controller.setLevelView(viewMock);

        // Act
        boolean result = controller.play();

        // Assert
        assertFalse(result);
        verify(viewMock, times(1)).inputListener();
        verify(controller, times(1)).handleInput(eq(keyMock));
    }


    @Test
    public void collectCoins_CoinAtPosition_CoinCollectedAndScoreIncremented() {
        // Arrange
        Level level = controller.getLevel();
        Position position = new Position(1, 1);
        Coin coin = new Coin(position);
        List<Coin> coins = new ArrayList<>();

        coins.add(coin);
        level.setCoins(coins);

        int initialScore = 0;
        level.setScore(initialScore);

        // Act
        controller.collectCoins(position);

        // Assert
        assertFalse(level.getCoins().contains(coin));
        assertEquals(initialScore + 1, level.getScore());
    }

    @Test
    public void collectStars_StarCollected_UserCanNotExit() {
        // Arrange
        Level level = controller.getLevel();
        Position position = new Position(1, 1);
        Star star = new Star(position);
        List<Star> stars = new ArrayList<>();

        stars.add(star);
        level.setStars(stars);

        int initialCollectedStars = 0;
        int totalStars = 3;
        level.setCollectedStars(initialCollectedStars);
        level.setTotalStars(totalStars);

        // Act
        controller.collectStars(position);

        // Assert
        assertFalse(level.getStars().contains(star));
        assertEquals(initialCollectedStars + 1, level.getCollectedStars());
        assertFalse(level.isCanUserExit());
    }

    @Test
    public void collectStars_StarCollected_UserCanExit() {
        // Arrange
        Level level = controller.getLevel();
        Position position = new Position(1, 1);
        Star star = new Star(position);
        List<Star> stars = new ArrayList<>();

        stars.add(star);
        level.setStars(stars);

        int initialCollectedStars = 2;
        int totalStars = 3;
        level.setCollectedStars(initialCollectedStars);
        level.setTotalStars(totalStars);

        // Act
        controller.collectStars(position);

        // Assert
        assertFalse(level.getStars().contains(star));
        assertEquals(initialCollectedStars + 1, level.getCollectedStars());
        assertTrue(level.isCanUserExit());
    }

    @Test
    public void processPlayerLocation_Test() throws IOException, InterruptedException {
        // Arrange
        controller.setLevel(levelMock);
        Position position = new Position(1, 1);
        when(levelMock.isOpenBtnAt(position)).thenReturn(true);

        // Act
        controller.processPlayerLocation(position);

        // Assert
        verify(controller, times(1)).collectCoins(position);
        verify(controller, times(1)).collectStars(position);

        verify(levelMock, times(1)).isOpenBtnAt(position);
        verify(levelMock, times(1)).toggleWalls();

        verify(controller, times(1)).renderView();
    }

    @Test
    public void hasPlayerWon_PlayerNotAtExit_ReturnsTrue() {
        // Arrange
        Position exitPosition = new Position(1, 1);
        Position playerPosition = new Position(0, 0);

        controller.setLevel(levelMock);
        when(levelMock.isExitAt(exitPosition)).thenReturn(false);

        // Act
        boolean hasWon = controller.hasPlayerWon(playerPosition);

        // Assert
        assertTrue(hasWon);
    }

    @Test
    public void hasPlayerWon_PlayerAtExit_ReturnsFalse() {
        // Arrange
        Position exitPosition = new Position(1, 1);
        Position playerPosition = new Position(1, 1);

        controller.setLevel(levelMock);
        when(levelMock.isExitAt(exitPosition)).thenReturn(true);

        // Act
        boolean hasWon = controller.hasPlayerWon(playerPosition);

        // Assert
        assertFalse(hasWon);
    }

    @Test
    public void testDeath() throws Exception {
        // Arrange
        doNothing().when(stateMock).setStateType(StateType.GAMEOVER);

        // Act
        controller.setState(stateMock);
        controller.death();

        // Assert
        verify(threadSleeperMock, times(1)).sleep(200);
        verify(stateMock, times(1)).setStateType(StateType.GAMEOVER);
        verify(stateMock, times(1)).handleStateController();
    }

    @Test
    public void testVictory() throws Exception {
        // Arrange
        int levelScore = 100;
        when(levelMock.getScore()).thenReturn(levelScore);

        // Act
        controller.setLevel(levelMock);
        controller.victory();

        // Assert
        verify(threadSleeperMock, times(1)).sleep(200);
        verify(controller, times(1)).setHighestScore();
        verify(stateMock, times(1)).setLevelScore(levelScore);
        verify(stateMock, times(1)).setStateType(StateType.VICTORY);
        verify(stateMock, times(1)).handleStateController();
    }

    @Test
    public void testSetHighestScore_LevelScoreLowerThanMap_HighestScoreNotUpdated() {
        // Arrange
        int levelScore = 50;
        when(levelMock.getScore()).thenReturn(levelScore);

        int mapHighestScore = 100;
        when(activeMapMock.getHighestScore()).thenReturn(mapHighestScore);

        // Act
        controller.setLevel(levelMock);
        controller.setHighestScore();

        // Assert
        verify(activeMapMock, never()).setHighestScore(levelScore);
        verify(stateMock).setLevelHighestScore(mapHighestScore);
    }

    @Test
    public void testSetHighestScore_LevelScoreHigherThanMap_HighestScoreUpdated() {
        // Arrange
        int levelScore = 100;
        when(levelMock.getScore()).thenReturn(levelScore);

        int mapHighestScore = 50;
        when(activeMapMock.getHighestScore()).thenReturn(mapHighestScore);

        // Act
        controller.setLevel(levelMock);
        controller.setHighestScore();

        // Assert
        verify(activeMapMock).setHighestScore(levelScore);
        verify(stateMock).setLevelHighestScore(levelScore);
    }

    @Test
    public void testSetHighestScore_LevelScoreEqualToMap_HighestScoreNotUpdated() {
        // Arrange
        int levelScore = 100;
        when(levelMock.getScore()).thenReturn(levelScore);

        int mapHighestScore = 100;
        when(activeMapMock.getHighestScore()).thenReturn(mapHighestScore);

        // Act
        controller.setLevel(levelMock);
        controller.setHighestScore();

        // Assert
        verify(activeMapMock, never()).setHighestScore(levelScore);
        verify(stateMock).setLevelHighestScore(mapHighestScore);
    }

    @Test
    void renderView_Success() throws IOException, InterruptedException {
        // Arrange
        TextGraphics textGraphics = Mockito.mock(TextGraphics.class);
        Mockito.when(screenMock.newTextGraphics()).thenReturn(textGraphics);
        controller.setScreen(screenMock);
        controller.setLevelView(viewMock);

        // Act
        controller.renderView();

        // Assert
        verify(screenMock).setCursorPosition(null);
        verify(screenMock).startScreen();
        verify(screenMock).doResizeIfNecessary();
        verify(viewMock).draw();
    }
}
