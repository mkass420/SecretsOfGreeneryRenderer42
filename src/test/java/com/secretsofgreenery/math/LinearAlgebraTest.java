package com.secretsofgreenery.math;

import com.secretsofgreenery.math.LinearAlgebra;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinearAlgebraTest {

    @Test
    void testSolveSystem2x2() {
        float[][] A = {
                {2, 1},
                {1, 3}
        };
        float[] b = {5, 10};

        float[] x = LinearAlgebra.solveSystem(A, b);

        assertEquals(1, x[0], 1e-8);
        assertEquals(3, x[1], 1e-8);

        assertEquals(5, A[0][0]*x[0] + A[0][1]*x[1], 1e-8);
        assertEquals(10, A[1][0]*x[0] + A[1][1]*x[1], 1e-8);
    }

    @Test
    void testSolveSystem3x3() {
        float[][] A = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 10}
        };
        float[] b = {6, 15, 25};

        float[] x = LinearAlgebra.solveSystem(A, b);

        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i] += A[i][j] * x[j];
            }
        }

        assertEquals(b[0], result[0], 1e-8);
        assertEquals(b[1], result[1], 1e-8);
        assertEquals(b[2], result[2], 1e-8);
    }

    @Test
    void testSolveSystemInvalidMatrix() {
        float[][] A = {
                {1, 2},
                {3, 4},
                {5, 6}
        };
        float[] b = {1, 2, 3};

        assertThrows(IllegalArgumentException.class, () -> LinearAlgebra.solveSystem(A, b));
    }

    @Test
    void testSolveSystemSingular() {
        float[][] A = {
                {1, 2},
                {2, 4}
        };
        float[] b = {1, 2};

        assertThrows(IllegalArgumentException.class, () -> LinearAlgebra.solveSystem(A, b));
    }

    @Test
    void testSolveSystemWithZeroDiagonal() {
        float[][] A = {
                {0, 1},
                {1, 0}
        };
        float[] b = {2, 3};

        float[] x = LinearAlgebra.solveSystem(A, b);

        assertEquals(3, x[0], 1e-8);
        assertEquals(2, x[1], 1e-8);
    }

    @Test
    void testSolveSystem4x4() {
        float[][] A = {
                {2, 0, 0, 0},
                {0, 3, 0, 0},
                {0, 0, 4, 0},
                {0, 0, 0, 5}
        };
        float[] b = {6, 12, 20, 30};

        float[] x = LinearAlgebra.solveSystem(A, b);

        assertEquals(3, x[0], 1e-8);
        assertEquals(4, x[1], 1e-8);
        assertEquals(5, x[2], 1e-8);
        assertEquals(6, x[3], 1e-8);
    }
}