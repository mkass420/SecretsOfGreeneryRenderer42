package com.secretsofgreenery.math;

import main.java.com.example.math.Vector3;
import main.java.com.example.math.Vector4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Vector4Test {

    @Test
    void testConstructorAndGetters() {
        Vector4 v = new Vector4(1.5, 2.5, 3.5, 4.5);
        assertEquals(1.5, v.getX(), 1e-10);
        assertEquals(2.5, v.getY(), 1e-10);
        assertEquals(3.5, v.getZ(), 1e-10);
        assertEquals(4.5, v.getW(), 1e-10);
    }

    @Test
    void testAdd() {
        Vector4 v1 = new Vector4(1, 2, 3, 4);
        Vector4 v2 = new Vector4(5, 6, 7, 8);
        Vector4 result = v1.add(v2);
        assertEquals(new Vector4(6, 8, 10, 12), result);
    }

    @Test
    void testSubtract() {
        Vector4 v1 = new Vector4(10, 20, 30, 40);
        Vector4 v2 = new Vector4(1, 2, 3, 4);
        Vector4 result = v1.subtract(v2);
        assertEquals(new Vector4(9, 18, 27, 36), result);
    }

    @Test
    void testMultiply() {
        Vector4 v = new Vector4(1, 2, 3, 4);
        Vector4 result = v.multiply(2.5);
        assertEquals(new Vector4(2.5, 5, 7.5, 10), result);
    }

    @Test
    void testDivide() {
        Vector4 v = new Vector4(10, 20, 30, 40);
        Vector4 result = v.divide(10);
        assertEquals(new Vector4(1, 2, 3, 4), result);
    }

    @Test
    void testLength() {
        Vector4 v = new Vector4(2, 4, 4, 8);
        assertEquals(10.0, v.length(), 1e-10);
    }

    @Test
    void testNormalize() {
        Vector4 v = new Vector4(0, 0, 0, 5);
        Vector4 normalized = v.normalize();
        assertEquals(1.0, normalized.length(), 1e-10);
        assertEquals(0.0, normalized.getX(), 1e-10);
        assertEquals(0.0, normalized.getY(), 1e-10);
        assertEquals(0.0, normalized.getZ(), 1e-10);
        assertEquals(1.0, normalized.getW(), 1e-10);
    }

    @Test
    void testDot() {
        Vector4 v1 = new Vector4(1, 2, 3, 4);
        Vector4 v2 = new Vector4(5, 6, 7, 8);
        assertEquals(70.0, v1.dot(v2), 1e-10);
    }

    @Test
    void testToVector3(){
        Vector4 v1 = new Vector4(1, 1, 1, 1);
        Vector3 v2 = new Vector3(1, 1, 1);
        assertEquals(v2, Vector4.toVector3(v1));
    }

    @Test
    void testToString() {
        Vector4 v = new Vector4(1.1, 2.2, 3.3, 4.4);
        String str = v.toString();
        assertTrue(str.contains("Vector4"));
        assertTrue(str.contains("1.1000"));
        assertTrue(str.contains("2.2000"));
        assertTrue(str.contains("3.3000"));
        assertTrue(str.contains("4.4000"));
    }
}