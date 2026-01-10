package com.secretsofgreenery.math;

import main.java.com.example.math.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Vector2Test {

    @Test
    void testConstructorAndGetters() {
        Vector2 v = new Vector2(1.5, 2.5);
        assertEquals(1.5, v.getX(), 1e-10);
        assertEquals(2.5, v.getY(), 1e-10);
    }

    @Test
    void testAdd() {
        Vector2 v1 = new Vector2(1, 2);
        Vector2 v2 = new Vector2(3, 4);
        Vector2 result = v1.add(v2);
        assertEquals(new Vector2(4, 6), result);
    }

    @Test
    void testSubtract() {
        Vector2 v1 = new Vector2(5, 7);
        Vector2 v2 = new Vector2(2, 3);
        Vector2 result = v1.subtract(v2);
        assertEquals(new Vector2(3, 4), result);
    }

    @Test
    void testMultiply() {
        Vector2 v = new Vector2(2, 3);
        Vector2 result = v.multiply(2.5);
        assertEquals(new Vector2(5, 7.5), result);
    }

    @Test
    void testDivide() {
        Vector2 v = new Vector2(6, 8);
        Vector2 result = v.divide(2);
        assertEquals(new Vector2(3, 4), result);
    }

    @Test
    void testDivideByZero() {
        Vector2 v = new Vector2(1, 2);
        assertThrows(IllegalArgumentException.class, () -> v.divide(0));
    }

    @Test
    void testLength() {
        Vector2 v = new Vector2(3, 4);
        assertEquals(5.0, v.length(), 1e-10);
    }

    @Test
    void testNormalize() {
        Vector2 v = new Vector2(3, 4);
        Vector2 normalized = v.normalize();
        assertEquals(1.0, normalized.length(), 1e-10);
        assertEquals(0.6, normalized.getX(), 1e-10);
        assertEquals(0.8, normalized.getY(), 1e-10);
    }

    @Test
    void testNormalizeZeroVector() {
        Vector2 v = new Vector2(0, 0);
        assertThrows(IllegalArgumentException.class, v::normalize);
    }

    @Test
    void testDot() {
        Vector2 v1 = new Vector2(1, 2);
        Vector2 v2 = new Vector2(3, 4);
        assertEquals(11.0, v1.dot(v2), 1e-10);
    }

    @Test
    void testEquals() {
        Vector2 v1 = new Vector2(1.0, 2.0);
        Vector2 v2 = new Vector2(1.0, 2.0);
        Vector2 v3 = new Vector2(1.0, 2.0000000001);
        assertTrue(v1.equals(v2));
        assertFalse(v1.equals(v3));
        assertFalse(v1.equals(new Vector2(2.0, 1.0)));
    }
}