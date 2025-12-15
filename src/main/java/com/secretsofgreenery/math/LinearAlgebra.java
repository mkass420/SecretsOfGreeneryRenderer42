package com.secretsofgreenery.math;

public class LinearAlgebra {

    public static float[] solveSystem(float[][] A, float[] b) {
        int n = b.length;

        if (A.length != n || A[0].length != n) {
            throw new IllegalArgumentException("Матрица A должна быть вида nxn");
        }

        float[][] Ab = new float[n][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, Ab[i], 0, n);
            Ab[i][n] = b[i];
        }

        for (int pivot = 0; pivot < n; pivot++) {
            int maxRow = pivot;
            for (int i = pivot + 1; i < n; i++) {
                if (Math.abs(Ab[i][pivot]) > Math.abs(Ab[maxRow][pivot])) {
                    maxRow = i;
                }
            }

            if (maxRow != pivot) {
                float[] temp = Ab[pivot];
                Ab[pivot] = Ab[maxRow];
                Ab[maxRow] = temp;
            }

            if (Math.abs(Ab[pivot][pivot]) < 1e-10) {
                throw new IllegalArgumentException("Нельзя найти решение, определитель матрицы равен нулю.");
            }

            for (int i = pivot + 1; i < n; i++) {
                float factor = Ab[i][pivot] / Ab[pivot][pivot];
                for (int j = pivot; j <= n; j++) {
                    Ab[i][j] -= factor * Ab[pivot][j];
                }
            }
        }

        float[] x = new float[n];
        for (int i = n - 1; i >= 0; i--) {
            float sum = 0.0f;
            for (int j = i + 1; j < n; j++) {
                sum += Ab[i][j] * x[j];
            }
            x[i] = (Ab[i][n] - sum) / Ab[i][i];
        }

        return x;
    }
}