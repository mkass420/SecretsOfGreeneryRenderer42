package com.secretsofgreenery.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Matrix4fTest {

    @Test
    void testConstructorValid() {
        float[][] data = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4f m = new Matrix4f(data);
        assertEquals(1, m.get(0, 0), 1e-10);
        assertEquals(16, m.get(3, 3), 1e-10);
    }

    @Test
    void testConstructorInvalid() {
        float[][] invalid1 = {{1, 2}, {3, 4}};
        float[][] invalid2 = new float[3][4];

        assertThrows(IllegalArgumentException.class, () -> new Matrix4f(invalid1));
        assertThrows(IllegalArgumentException.class, () -> new Matrix4f(invalid2));
    }

    @Test
    void testIdentity() {
        Matrix4f identity = Matrix4f.identity();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    assertEquals(1, identity.get(i, j), 1e-10);
                } else {
                    assertEquals(0, identity.get(i, j), 1e-10);
                }
            }
        }
    }

    @Test
    void testAdd() {
        float[][] data1 = {
                {1, 1, 1, 1},
                {1, 1, 1, 1},
                {1, 1, 1, 1},
                {1, 1, 1, 1}
        };
        float[][] data2 = {
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2}
        };
        Matrix4f m1 = new Matrix4f(data1);
        Matrix4f m2 = new Matrix4f(data2);
        Matrix4f result = m1.add(m2);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(3, result.get(i, j), 1e-10);
            }
        }
    }

    @Test
    void testMultiply() {
        float[][] data1 = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        float[][] data2 = {
                {16, 15, 14, 13},
                {12, 11, 10, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}
        };
        Matrix4f m1 = new Matrix4f(data1);
        Matrix4f m2 = new Matrix4f(data2);
        Matrix4f result = m1.multiply(m2);

        assertEquals(80, result.get(0, 0), 1e-10);

        assertEquals(386, result.get(3, 3), 1e-10);
    }

    @Test
    void testMultiplyWithIdentity() {
        float[][] data = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4f m = new Matrix4f(data);
        Matrix4f identity = Matrix4f.identity();

        Matrix4f result1 = m.multiply(identity);
        Matrix4f result2 = identity.multiply(m);

        assertEquals(m, result1);
        assertEquals(m, result2);
    }

    @Test
    void testTranspose() {
        float[][] data = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4f m = new Matrix4f(data);
        Matrix4f transposed = m.transpose();

        assertEquals(1, transposed.get(0, 0), 1e-10);
        assertEquals(5, transposed.get(0, 1), 1e-10);
        assertEquals(9, transposed.get(0, 2), 1e-10);
        assertEquals(13, transposed.get(0, 3), 1e-10);
        assertEquals(2, transposed.get(1, 0), 1e-10);
    }

    @Test
    void testDeterminant() {
        Matrix4f identity = Matrix4f.identity();
        assertEquals(1, identity.determinant(), 1e-10);

        float[][] data = {
                {1, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 0, 3, 0},
                {0, 0, 0, 4}
        };
        Matrix4f m = new Matrix4f(data);
        assertEquals(24, m.determinant(), 1e-10); // 1*2*3*4 = 24
    }

    @Test
    void testInverse() {
        float[][] data = {
                {2, 0, 0, 0},
                {0, 3, 0, 0},
                {0, 0, 4, 0},
                {0, 0, 0, 5}
        };
        Matrix4f m = new Matrix4f(data);
        Matrix4f inverse = m.inverse();

        assertEquals(0.5, inverse.get(0, 0), 1e-10);
        assertEquals(1.0/3, inverse.get(1, 1), 1e-10);
        assertEquals(0.25, inverse.get(2, 2), 1e-10);
        assertEquals(0.2, inverse.get(3, 3), 1e-10);
    }

    @Test
    void testInverseSingularMatrix() {
        float[][] data = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4f m = new Matrix4f(data);
        assertThrows(IllegalArgumentException.class, m::inverse);
    }

    @Test
    void testToString() {
        float[][] data = {
                {1.1F, 2.2F, 3.3F, 4.4F},
                {5.5F, 6.6F, 7.7F, 8.8F},
                {9.9F, 10.10F, 11.11F, 12.12F},
                {13.13F, 14.14F, 15.15F, 16.16F}
        };
        Matrix4f m = new Matrix4f(data);
        String str = m.toString();

        assertTrue(str.contains("["));
        assertTrue(str.contains("]"));
        assertTrue(str.contains("1.1000"));
        assertTrue(str.contains("16.1600"));
    }

    @Test
    void testMultiplyByVector(){
        float[][] data = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        Matrix4f m = new Matrix4f(data);
        Vector4f v = new Vector4f(1, 2, 3, 4);
        Vector4f v_expected = new Vector4f(30, 70, 110, 150);
        assertEquals(v_expected, m.multiplyByVector(v));
    }
}