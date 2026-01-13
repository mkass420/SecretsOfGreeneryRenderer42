package com.secretsofgreenery.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector3fTest {

    @Test
    void testConstructorAndGetters() {
        Vector3f v = new Vector3f(1.5F, 2.5F, 3.5F);
        assertEquals(1.5, v.getX(), 1e-5);
        assertEquals(2.5, v.getY(), 1e-5);
        assertEquals(3.5, v.getZ(), 1e-5);
    }

    @Test
    void testAdd() {
        Vector3f v1 = new Vector3f(1, 2, 3);
        Vector3f v2 = new Vector3f(4, 5, 6);
        Vector3f result = v1.add(v2);
        assertEquals(new Vector3f(5, 7, 9), result);
    }

    @Test
    void testSubtract() {
        Vector3f v1 = new Vector3f(10, 11, 12);
        Vector3f v2 = new Vector3f(1, 2, 3);
        Vector3f result = v1.subtract(v2);
        assertEquals(new Vector3f(9, 9, 9), result);
    }

    @Test
    void testMultiply() {
        Vector3f v = new Vector3f(2, 3, 4);
        Vector3f result = v.multiply(2);
        assertEquals(new Vector3f(4, 6, 8), result);
    }

    @Test
    void testDivide() {
        Vector3f v = new Vector3f(6, 8, 10);
        Vector3f result = v.divide(2);
        assertEquals(new Vector3f(3, 4, 5), result);
    }

    @Test
    void testDivideByZero() {
        Vector3f v = new Vector3f(1, 2, 3);
        assertThrows(IllegalArgumentException.class, () -> v.divide(0));
    }

    @Test
    void testLength() {
        Vector3f v = new Vector3f(2, 3, 6);
        assertEquals(7.0, v.length(), 1e-5);
    }

    @Test
    void testNormalize() {
        Vector3f v = new Vector3f(2, 0, 0);
        Vector3f normalized = v.normalize();
        assertEquals(1.0, normalized.length(), 1e-5);
        assertEquals(1.0, normalized.getX(), 1e-5);
        assertEquals(0.0, normalized.getY(), 1e-5);
        assertEquals(0.0, normalized.getZ(), 1e-5);
    }

    @Test
    void testDot() {
        Vector3f v1 = new Vector3f(1, 2, 3);
        Vector3f v2 = new Vector3f(4, 5, 6);
        assertEquals(32.0, v1.dot(v2), 1e-5);
    }

    @Test
    void testToVector4f(){
        Vector3f v = new Vector3f(1, 1, 1);
        Vector4f res = new Vector4f(1, 1, 1, 1);
        assertEquals(res, Vector3f.toVector4f(v));
    }

    @Test
    void testCross() {
        Vector3f v1 = new Vector3f(1, 0, 0);
        Vector3f v2 = new Vector3f(0, 1, 0);
        Vector3f result = v1.cross(v2);
        assertEquals(new Vector3f(0, 0, 1), result);
    }

    @Test
    void testCrossAntiCommutative() {
        Vector3f v1 = new Vector3f(1, 2, 3);
        Vector3f v2 = new Vector3f(4, 5, 6);
        Vector3f cross1 = v1.cross(v2);
        Vector3f cross2 = v2.cross(v1);
        assertEquals(cross1, cross2.multiply(-1));
    }

    @Test
    void testToString() {
        Vector3f v = new Vector3f(1.23456F, 2.34567F, 3.45678F);
        String str = v.toString();
        assertTrue(str.contains("Vector3f"));
        assertTrue(str.contains("1.2346"));
        assertTrue(str.contains("2.3457"));
        assertTrue(str.contains("3.4568"));
    }
}