package com.secretsofgreenery.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector2fTest {

    @Test
    void testConstructorAndGetters() {
        Vector2f v = new Vector2f(1.5F, 2.5F);
        assertEquals(1.5, v.getX(), 1e-10);
        assertEquals(2.5, v.getY(), 1e-10);
    }

    @Test
    void testAdd() {
        Vector2f v1 = new Vector2f(1, 2);
        Vector2f v2 = new Vector2f(3, 4);
        Vector2f result = v1.add(v2);
        assertEquals(new Vector2f(4, 6), result);
    }

    @Test
    void testSubtract() {
        Vector2f v1 = new Vector2f(5, 7);
        Vector2f v2 = new Vector2f(2, 3);
        Vector2f result = v1.subtract(v2);
        assertEquals(new Vector2f(3, 4), result);
    }

    @Test
    void testMultiply() {
        Vector2f v = new Vector2f(2, 3);
        Vector2f result = v.multiply(2.5F);
        assertEquals(new Vector2f(5, 7.5F), result);
    }

    @Test
    void testDivide() {
        Vector2f v = new Vector2f(6, 8);
        Vector2f result = v.divide(2);
        assertEquals(new Vector2f(3, 4), result);
    }

    @Test
    void testDivideByZero() {
        Vector2f v = new Vector2f(1, 2);
        assertThrows(IllegalArgumentException.class, () -> v.divide(0));
    }

    @Test
    void testLength() {
        Vector2f v = new Vector2f(3, 4);
        assertEquals(5.0, v.length(), 1e-10);
    }

    @Test
    void testNormalize() {
        Vector2f v = new Vector2f(3, 4);
        Vector2f normalized = v.normalize();
        assertEquals(1.0, normalized.length(), 1e-10);
        assertEquals(0.6, normalized.getX(), 1e-10);
        assertEquals(0.8, normalized.getY(), 1e-10);
    }

    @Test
    void testNormalizeZeroVector() {
        Vector2f v = new Vector2f(0, 0);
        assertThrows(IllegalArgumentException.class, v::normalize);
    }

    @Test
    void testDot() {
        Vector2f v1 = new Vector2f(1, 2);
        Vector2f v2 = new Vector2f(3, 4);
        assertEquals(11.0, v1.dot(v2), 1e-10);
    }

    @Test
    void testEquals() {
        Vector2f v1 = new Vector2f(1.0F, 2.0F);
        Vector2f v2 = new Vector2f(1.0F, 2.0F);
        Vector2f v3 = new Vector2f(1.0F, 2.0000000001F);
        assertTrue(v1.equals(v2));
        assertFalse(v1.equals(v3));
        assertFalse(v1.equals(new Vector2f(2.0F, 1.0F)));
    }
}