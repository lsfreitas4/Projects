package com.project.Model;

import com.project.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LevelTest {
    @Mock
    State stateMock;

    @InjectMocks
    Level level;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toggleWalls_FromFalseToTrue() {
        level.setToggledWallsState(false);
        assertFalse(level.isToggledWallsState());
        level.toggleWalls();
        assertTrue(level.isToggledWallsState());
    }

    @Test
    void toggleWalls_FromTrueToFalse() {
        level.setToggledWallsState(true);
        assertTrue(level.isToggledWallsState());
        level.toggleWalls();
        assertFalse(level.isToggledWallsState());
    }

    @Test
    void incrementScore_Success() {
        int initialScore = level.getScore();
        level.incrementScore();
        assertEquals(initialScore + 1, level.getScore());
    }

    @Test
    void incrementCollectedStarsScore_Success() {
        int initialStars = level.getCollectedStars();
        level.incrementCollectedStars();
        assertEquals(initialStars + 1, level.getCollectedStars());
    }

    @Test
    void removeStar_PositiveCase() {
        Position removeFromPosition = new Position(1, 1);

        List<Star> starsList = new ArrayList<>();
        Star star = mock(Star.class);
        starsList.add(star);
        when(star.getPosition()).thenReturn(removeFromPosition);

        level.setStars(starsList);
        level.removeStar(removeFromPosition);

        assertTrue(starsList.isEmpty());
    }

    @Test
    void removeStar_NegativeCase() {
        Position removeFromPosition = new Position(1, 1);

        List<Star> starsList = new ArrayList<>();
        Star star = mock(Star.class);
        starsList.add(star);
        when(star.getPosition()).thenReturn(new Position(0, 0));

        level.setStars(starsList);
        level.removeStar(removeFromPosition);

        assertFalse(starsList.isEmpty());
    }

    @Test
    void removeCoin_PositiveCase() {
        Position removeFromPosition = new Position(1, 1);

        List<Coin> coinsList = new ArrayList<>();
        Coin coin = mock(Coin.class);
        coinsList.add(coin);
        when(coin.getPosition()).thenReturn(removeFromPosition);

        level.setCoins(coinsList);
        level.removeCoin(removeFromPosition);

        assertTrue(coinsList.isEmpty());
    }

    @Test
    void removeCoin_NegativeCase() {
        Position removeFromPosition = new Position(1, 1);

        List<Coin> coinList = new ArrayList<>();
        Coin coin = mock(Coin.class);
        coinList.add(coin);
        when(coin.getPosition()).thenReturn(new Position(0, 0));

        level.setCoins(coinList);
        level.removeCoin(removeFromPosition);

        assertFalse(coinList.isEmpty());
    }

    @Test
    void isWallAtExit_WhenAllStarsNotCollected() {
        Position exitPosition = new Position(1, 1);
        level.setCanUserExit(false);
        level.setExit(mock(Exit.class));
        when(level.getExit().getPosition()).thenReturn(exitPosition);

        boolean isWall = level.isWallAt(exitPosition);

        assertTrue(isWall);
    }

    @Test
    void isWallAt_ToggledWallsTrue() {
        level.setToggledWallsState(true);
        Wall toggledWall = mock(Wall.class);
        when(toggledWall.getPosition()).thenReturn(new Position(1, 1));
        level.getToggledWalls().add(toggledWall);
        level.setExit(new Exit(new Position(5,5)));

        boolean isWallTrue = level.isWallAt(new Position(1, 1));
        boolean isWallFalse = level.isWallAt(new Position(0, 0));

        assertTrue(isWallTrue);
        assertFalse(isWallFalse);
    }

    @Test
    void isRegularWallAt() {
        Wall wall = mock(Wall.class);
        when(wall.getPosition()).thenReturn(new Position(1, 1));
        level.getWalls().add(wall);
        level.setExit(new Exit(new Position(5,5)));

        boolean isWallTrue = level.isWallAt(new Position(1,1));
        boolean isWallFalse = level.isWallAt(new Position(0,0));

        assertTrue(isWallTrue);
        assertFalse(isWallFalse);
    }

    @Test
    void isExitAtPositionAndCanUserExit_True() {
        level.setCollectedStars(3);
        level.setTotalStars(3);
        Exit exit = mock(Exit.class);
        when(exit.getPosition()).thenReturn(new Position(1, 1));
        level.setExit(exit);
        level.setCanUserExit(true);

        boolean isExit = level.isExitAt(new Position(1, 1));

        assertTrue(isExit);
    }

    @Test
    void isExitAtPositionAndCanUserExit_False() {
        Exit exit = mock(Exit.class);
        when(exit.getPosition()).thenReturn(new Position(1, 1));
        level.setExit(exit);
        level.setCanUserExit(false);

        boolean isExit = level.isExitAt(new Position(1, 1));

        assertFalse(isExit);
    }

    @Test
    void isExitNotAtPositionAndCanUserExit_False() {
        Exit exit = mock(Exit.class);
        when(exit.getPosition()).thenReturn(new Position(1, 1));
        level.setExit(exit);
        level.setCanUserExit(true);

        boolean isExit = level.isExitAt(new Position(2, 2));

        assertFalse(isExit);
    }

    @Test
    void isSpikeAt_PositiveCase() {
        // Arrange
        Spike mockSpike = mock(Spike.class);
        Position position = new Position(1, 1);
        when(mockSpike.getPosition()).thenReturn(position);
        level.getSpikes().add(mockSpike);

        // Act
        boolean isSpikeAt = level.isSpikeAt(position);

        // Assert
        assertTrue(isSpikeAt);
    }

    @Test
    void isSpikeAt_NegativeCase() {
        // Arrange
        Spike mockSpike = mock(Spike.class);
        Position position = new Position(1, 1);
        when(mockSpike.getPosition()).thenReturn(new Position(0, 0));
        level.getSpikes().add(mockSpike);

        // Act
        boolean isSpikeAt = level.isSpikeAt(position);

        // Assert
        assertFalse(isSpikeAt);
    }

    @Test
    void isStarAt_PositiveCase() {
        // Arrange
        Star mockStar = mock(Star.class);
        Position position = new Position(1, 1);
        when(mockStar.getPosition()).thenReturn(position);
        level.getStars().add(mockStar);

        // Act
        boolean isStarAt = level.isStarAt(position);

        // Assert
        assertTrue(isStarAt);
    }

    @Test
    void isStarAt_NegativeCase() {
        // Arrange
        Star mockStar = mock(Star.class);
        Position position = new Position(1, 1);
        when(mockStar.getPosition()).thenReturn(new Position(0, 0));
        level.getStars().add(mockStar);

        // Act
        boolean isStarAt = level.isStarAt(position);

        // Assert
        assertFalse(isStarAt);
    }

    @Test
    void isOpenBtnAt_PositiveCase() {
        // Arrange
        OpenBtn mockOpenBtn = mock(OpenBtn.class);
        Position position = new Position(1, 1);
        when(mockOpenBtn.getPosition()).thenReturn(position);
        level.getOpenBtns().add(mockOpenBtn);

        // Act
        boolean isOpenBtnAt = level.isOpenBtnAt(position);

        // Assert
        assertTrue(isOpenBtnAt);
    }

    @Test
    void isOpenBtnAt_NegativeCase() {
        // Arrange
        OpenBtn mockOpenBtn = mock(OpenBtn.class);
        Position position = new Position(1, 1);
        when(mockOpenBtn.getPosition()).thenReturn(new Position(0, 0));
        level.getOpenBtns().add(mockOpenBtn);

        // Act
        boolean isOpenBtnAt = level.isOpenBtnAt(position);

        // Assert
        assertFalse(isOpenBtnAt);
    }

    @Test
    void isCoinAt_PositiveCase() {
        // Arrange
        Coin mockCoin = mock(Coin.class);
        Position position = new Position(1, 1);
        when(mockCoin.getPosition()).thenReturn(position);
        level.getCoins().add(mockCoin);

        // Act
        boolean isCoinAt = level.isCoinAt(position);

        // Assert
        assertTrue(isCoinAt);
    }

    @Test
    void isCoinAt_NegativeCase() {
        // Arrange
        Coin mockCoin = mock(Coin.class);
        Position position = new Position(1, 1);
        when(mockCoin.getPosition()).thenReturn(new Position(0, 0));
        level.getCoins().add(mockCoin);

        // Act
        boolean isCoinAt = level.isCoinAt(position);

        // Assert
        assertFalse(isCoinAt);
    }

    @Test
    void canUserExit_CollectedStarsEqualToTotalStars() {
        // Arrange
        level.setCollectedStars(3);
        level.setTotalStars(3);

        // Act
        boolean canUserExit = level.canUserExit();

        // Assert
        assertTrue(canUserExit);
    }

    @Test
    void canUserExitCollectedStarsNotEqualToTotalStars() {
        // Arrange
        level.setCollectedStars(0);
        level.setTotalStars(3);

        // Act
        boolean canUserExit = level.canUserExit();

        // Assert
        assertFalse(canUserExit);
    }

    @Test
    void initializeMapArray() {
        Map activeMap = mock(Map.class);
        List<String> expectedMapArray = Arrays.asList("ww", "mm");
        when(activeMap.getMapArray()).thenReturn(expectedMapArray);
        when(stateMock.getActiveMap()).thenReturn(activeMap);
        level.setActiveMapArray(expectedMapArray);

        level.initializeMapArray(stateMock);

        assertEquals(level.getActiveMapArray(), expectedMapArray);
    }

    @Test
    void unpackMap() {
        level.setActiveMapArray(Arrays.asList("w", "-", "m", "/", ".", "*", "o"));
        level.unpackMap();

        assertEquals(1, level.getWalls().size());
        assertEquals(1, level.getToggledWalls().size());
        assertEquals(1, level.getSpikes().size());
        assertEquals(1, level.getCoins().size());
        assertEquals(1, level.getStars().size());
        assertEquals(1, level.getOpenBtns().size());
        assertNotNull(level.getExit());

        assertEquals(new Position(0, Constants.HEADER_HEIGHT), level.getWalls().get(0).getPosition());
        assertEquals(new Position(0, Constants.HEADER_HEIGHT + 1), level.getExit().getPosition());
        assertEquals(new Position(0, Constants.HEADER_HEIGHT + 2), level.getToggledWalls().get(0).getPosition());
        assertEquals(new Position(0, Constants.HEADER_HEIGHT + 3), level.getSpikes().get(0).getPosition());
        assertEquals(new Position(0, Constants.HEADER_HEIGHT + 4), level.getCoins().get(0).getPosition());
        assertEquals(new Position(0, Constants.HEADER_HEIGHT + 5), level.getStars().get(0).getPosition());
        assertEquals(new Position(0, Constants.HEADER_HEIGHT + 6), level.getOpenBtns().get(0).getPosition());
    }
}