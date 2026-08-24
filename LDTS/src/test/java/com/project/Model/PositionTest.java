package com.project.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PositionTest {
    @Test
    public void testEquals() {
        Position position1 = new Position(2, 3);
        Position position2 = new Position(2, 3);
        Position position3 = new Position(4, 5);

        assertTrue(position1.equals(position1));

        assertTrue(position1.equals(position2));
        assertTrue(position2.equals(position1));

        assertFalse(position2.equals(position3));
        assertFalse(position1.equals(position3));

        assertFalse(position1.equals(null));

        assertFalse(position1.equals(""));
    }

    @Test
    public void testHashCode() {
        Position position1 = new Position(1, 2);
        Position position2 = new Position(1, 2);
        Position position3 = new Position(2, 3);

        int hashCode1 = position1.hashCode();
        int hashCode2 = position2.hashCode();
        int hashCode3 = position3.hashCode();

        assertEquals(hashCode1, hashCode2);
        assertEquals(position1.hashCode(), position2.hashCode());
        assertNotEquals(hashCode1, hashCode3);
        assertEquals(65, position3.hashCode());
    }



    @Test
    public void testToString() {
        Position position = new Position(2, 3);
        assertEquals("Position {x=2, y=3}", position.toString());
    }
}