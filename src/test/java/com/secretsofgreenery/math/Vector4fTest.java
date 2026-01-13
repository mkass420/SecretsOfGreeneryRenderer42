package com.secretsofgreenery.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Vector4fTest {

    @Test
    void testConstructorAndGetters() {
        Vector4f v = new Vector4f(1.5F, 2.5F, 3.5F, 4.5F);
        assertEquals(1.5, v.getX(), 1e-5);
        assertEquals(2.5, v.getY(), 1e-5);
        assertEquals(3.5, v.getZ(), 1e-5);
        assertEquals(4.5, v.getW(), 1e-5);
    }

    @Test
    void testAdd() {
        Vector4f v1 = new Vector4f(1, 2, 3, 4);
        Vector4f v2 = new Vector4f(5, 6, 7, 8);
        Vector4f result = v1.add(v2);
        assertEquals(new Vector4f(6, 8, 10, 12), result);
    }

    @Test
    void testSubtract() {
        Vector4f v1 = new Vector4f(10, 20, 30, 40);
        Vector4f v2 = new Vector4f(1, 2, 3, 4);
        Vector4f result = v1.subtract(v2);
        assertEquals(new Vector4f(9, 18, 27, 36), result);
    }

    @Test
    void testMultiply() {
        Vector4f v = new Vector4f(1, 2, 3, 4);
        Vector4f result = v.multiply(2.5F);
        assertEquals(new Vector4f(2.5F, 5, 7.5F, 10), result);
    }

    @Test
    void testDivide() {
        Vector4f v = new Vector4f(10, 20, 30, 40);
        Vector4f result = v.divide(10);
        assertEquals(new Vector4f(1, 2, 3, 4), result);
    }

    @Test
    void testLength() {
        Vector4f v = new Vector4f(2, 4, 4, 8);
        assertEquals(10.0, v.length(), 1e-5);
    }

    @Test
    void testNormalize() {
        Vector4f v = new Vector4f(0, 0, 0, 5);
        Vector4f normalized = v.normalize();
        assertEquals(1.0, normalized.length(), 1e-5);
        assertEquals(0.0, normalized.getX(), 1e-5);
        assertEquals(0.0, normalized.getY(), 1e-5);
        assertEquals(0.0, normalized.getZ(), 1e-5);
        assertEquals(1.0, normalized.getW(), 1e-5);
    }

    @Test
    void testDot() {
        Vector4f v1 = new Vector4f(1, 2, 3, 4);
        Vector4f v2 = new Vector4f(5, 6, 7, 8);
        assertEquals(70.0, v1.dot(v2), 1e-5);
    }

    @Test
    void testToVector3f(){
        Vector4f v1 = new Vector4f(1, 1, 1, 1);
        Vector3f v2 = new Vector3f(1, 1, 1);
        assertEquals(v2, Vector4f.toVector3f(v1));
    }

    @Test
    void testToString() {
        Vector4f v = new Vector4f(1.1F, 2.2F, 3.3F, 4.4F);
        String str = v.toString();
        assertTrue(str.contains("Vector4f"));
        assertTrue(str.contains("1.1000"));
        assertTrue(str.contains("2.2000"));
        assertTrue(str.contains("3.3000"));
        assertTrue(str.contains("4.4000"));
    }
}