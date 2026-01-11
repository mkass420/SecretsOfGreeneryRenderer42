package com.secretsofgreenery.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Matrix3fTest {

    @Test
    void testConstructorValid() {
        float[][] data = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        Matrix3f m = new Matrix3f(data);
        assertEquals(1, m.get(0, 0), 1e-10);
        assertEquals(9, m.get(2, 2), 1e-10);
    }

    @Test
    void testConstructorInvalid() {
        float[][] invalid1 = {{1, 2}, {3, 4}};
        float[][] invalid2 = {{1, 2, 3}, {4, 5, 6}};

        assertThrows(IllegalArgumentException.class, () -> new Matrix3f(invalid1));
        assertThrows(IllegalArgumentException.class, () -> new Matrix3f(invalid2));
    }

    @Test
    void testIdentity() {
        Matrix3f identity = Matrix3f.identity();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    assertEquals(1, identity.get(i, j), 1e-10);
                } else {
                    assertEquals(0, identity.get(i, j), 1e-10);
                }
            }
        }
    }

    @Test
    void testZero() {
        Matrix3f zero = Matrix3f.zero();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(0, zero.get(i, j), 1e-10);
            }
        }
    }

    @Test
    void testAdd() {
        float[][] data1 = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        float[][] data2 = {{9, 8, 7}, {6, 5, 4}, {3, 2, 1}};
        Matrix3f m1 = new Matrix3f(data1);
        Matrix3f m2 = new Matrix3f(data2);
        Matrix3f result = m1.add(m2);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(10, result.get(i, j), 1e-10);
            }
        }
    }

    @Test
    void testSubtract() {
        float[][] data1 = {{10, 10, 10}, {10, 10, 10}, {10, 10, 10}};
        float[][] data2 = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        Matrix3f m1 = new Matrix3f(data1);
        Matrix3f m2 = new Matrix3f(data2);
        Matrix3f result = m1.subtract(m2);

        assertEquals(9, result.get(0, 0), 1e-10);
        assertEquals(8, result.get(0, 1), 1e-10);
        assertEquals(7, result.get(0, 2), 1e-10);
    }

    @Test
    void testMultiply() {
        float[][] data1 = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        float[][] data2 = {{9, 8, 7}, {6, 5, 4}, {3, 2, 1}};
        Matrix3f m1 = new Matrix3f(data1);
        Matrix3f m2 = new Matrix3f(data2);
        Matrix3f result = m1.multiply(m2);

        assertEquals(30, result.get(0, 0), 1e-10);
        assertEquals(69, result.get(1, 1), 1e-10);
    }

    @Test
    void testMultiplyWithIdentity() {
        float[][] data = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        Matrix3f m = new Matrix3f(data);
        Matrix3f identity = Matrix3f.identity();

        Matrix3f result1 = m.multiply(identity);
        Matrix3f result2 = identity.multiply(m);

        assertEquals(m, result1);
        assertEquals(m, result2);
    }

    @Test
    void testTranspose() {
        float[][] data = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        Matrix3f m = new Matrix3f(data);
        Matrix3f transposed = m.transpose();

        assertEquals(1, transposed.get(0, 0), 1e-10);
        assertEquals(4, transposed.get(0, 1), 1e-10);
        assertEquals(7, transposed.get(0, 2), 1e-10);
        assertEquals(2, transposed.get(1, 0), 1e-10);
    }

    @Test
    void testDeterminant() {
        float[][] data = {{1, 2, 3}, {0, 1, 4}, {5, 6, 0}};
        Matrix3f m = new Matrix3f(data);
        assertEquals(1, m.determinant(), 1e-10);
    }

    @Test
    void testInverse() {
        float[][] data = {{4, 7, 2}, {3, 6, 1}, {2, 5, 3}};
        Matrix3f m = new Matrix3f(data);
        Matrix3f inverse = m.inverse();

        Matrix3f product = m.multiply(inverse);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    assertEquals(1, product.get(i, j), 1e-8);
                } else {
                    assertEquals(0, product.get(i, j), 1e-8);
                }
            }
        }
    }

    @Test
    void testInverseSingularMatrix() {
        float[][] data = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        Matrix3f m = new Matrix3f(data);
        assertThrows(IllegalArgumentException.class, m::inverse);
    }

    @Test
    void testToString() {
        float[][] data = {{1.123456F, 2.234567F, 3.345678F},
                {4.456789F, 5.567890F, 6.678901F},
                {7.789012F, 8.890123F, 9.901234F}};
        Matrix3f m = new Matrix3f(data);
        String str = m.toString();

        assertTrue(str.contains("["));
        assertTrue(str.contains("]"));
        assertTrue(str.contains("1.1235"));
        assertTrue(str.contains("9.9012"));
    }

    @Test
    void testMultiplyByVector(){
        float[][] data = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9},
        };
        Matrix3f m = new Matrix3f(data);
        Vector3f v = new Vector3f(1, 2, 3);
        Vector3f v_expected = new Vector3f(14, 32, 50);
        assertEquals(v_expected, m.multiplyByVector(v));
    }
}