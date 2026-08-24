package com.project.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerTest {
    Player player;

    @BeforeEach
    void setUp() {
        this.player = new Player(new Position(0, 0));
    }

    @Test
    void testMoveUp() {
        Position newPosition = player.moveUp();

        assertEquals(0, newPosition.getX());
        assertEquals(-1, newPosition.getY());
    }

    @Test
    void testMoveRight() {
        Position newPosition = player.moveRight();

        assertEquals(1, newPosition.getX());
        assertEquals(0, newPosition.getY());
    }

    @Test
    void testMoveDown() {
        Position newPosition = player.moveDown();

        assertEquals(0, newPosition.getX());
        assertEquals(1, newPosition.getY());
    }

    @Test
    void testMoveLeft() {
        Position newPosition = player.moveLeft();

        assertEquals(-1, newPosition.getX());
        assertEquals(0, newPosition.getY());
    }
}