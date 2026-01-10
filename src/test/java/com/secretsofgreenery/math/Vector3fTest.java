package com.secretsofgreenery.math;

import main.java.com.example.math.Vector3;
import main.java.com.example.math.Vector4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector3Test {

    @Test
    void testConstructorAndGetters() {
        Vector3 v = new Vector3(1.5, 2.5, 3.5);
        assertEquals(1.5, v.getX(), 1e-10);
        assertEquals(2.5, v.getY(), 1e-10);
        assertEquals(3.5, v.getZ(), 1e-10);
    }

    @Test
    void testAdd() {
        Vector3 v1 = new Vector3(1, 2, 3);
        Vector3 v2 = new Vector3(4, 5, 6);
        Vector3 result = v1.add(v2);
        assertEquals(new Vector3(5, 7, 9), result);
    }

    @Test
    void testSubtract() {
        Vector3 v1 = new Vector3(10, 11, 12);
        Vector3 v2 = new Vector3(1, 2, 3);
        Vector3 result = v1.subtract(v2);
        assertEquals(new Vector3(9, 9, 9), result);
    }

    @Test
    void testMultiply() {
        Vector3 v = new Vector3(2, 3, 4);
        Vector3 result = v.multiply(2);
        assertEquals(new Vector3(4, 6, 8), result);
    }

    @Test
    void testDivide() {
        Vector3 v = new Vector3(6, 8, 10);
        Vector3 result = v.divide(2);
        assertEquals(new Vector3(3, 4, 5), result);
    }

    @Test
    void testDivideByZero() {
        Vector3 v = new Vector3(1, 2, 3);
        assertThrows(IllegalArgumentException.class, () -> v.divide(0));
    }

    @Test
    void testLength() {
        Vector3 v = new Vector3(2, 3, 6);
        assertEquals(7.0, v.length(), 1e-10);
    }

    @Test
    void testNormalize() {
        Vector3 v = new Vector3(2, 0, 0);
        Vector3 normalized = v.normalize();
        assertEquals(1.0, normalized.length(), 1e-10);
        assertEquals(1.0, normalized.getX(), 1e-10);
        assertEquals(0.0, normalized.getY(), 1e-10);
        assertEquals(0.0, normalized.getZ(), 1e-10);
    }

    @Test
    void testDot() {
        Vector3 v1 = new Vector3(1, 2, 3);
        Vector3 v2 = new Vector3(4, 5, 6);
        assertEquals(32.0, v1.dot(v2), 1e-10);
    }

    @Test
    void testToVector4(){
        Vector3 v = new Vector3(1, 1, 1);
        Vector4 res = new Vector4(1, 1, 1, 1);
        assertEquals(res, Vector3.toVector4(v));
    }

    @Test
    void testCross() {
        Vector3 v1 = new Vector3(1, 0, 0);
        Vector3 v2 = new Vector3(0, 1, 0);
        Vector3 result = v1.cross(v2);
        assertEquals(new Vector3(0, 0, 1), result);
    }

    @Test
    void testCrossAntiCommutative() {
        Vector3 v1 = new Vector3(1, 2, 3);
        Vector3 v2 = new Vector3(4, 5, 6);
        Vector3 cross1 = v1.cross(v2);
        Vector3 cross2 = v2.cross(v1);
        assertEquals(cross1, cross2.multiply(-1));
    }

    @Test
    void testToString() {
        Vector3 v = new Vector3(1.23456, 2.34567, 3.45678);
        String str = v.toString();
        assertTrue(str.contains("Vector3"));
        assertTrue(str.contains("1.2346"));
        assertTrue(str.contains("2.3457"));
        assertTrue(str.contains("3.4568"));
    }
}