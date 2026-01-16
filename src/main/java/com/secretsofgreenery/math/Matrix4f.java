package com.secretsofgreenery.math;

import java.util.Locale;

public class Matrix4f {
    private final float[][] data;

    public Matrix4f(float[][] data) {
        if (data.length != 4 || data[0].length != 4) {
            throw new IllegalArgumentException("Матрица должна быть вида 4x4.");
        }
        this.data = new float[4][4];
        for (int i = 0; i < 4; i++) {
            System.arraycopy(data[i], 0, this.data[i], 0, 4);
        }
    }

    public Matrix4f(Matrix4f matrix){
        this(matrix.getData());
    }

    public Matrix4f() {
        this.data = new float[4][4];
    }

    public static Matrix4f identity() {
        float[][] identity = {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        return new Matrix4f(identity);
    }

    public static Matrix4f zero() {
        return new Matrix4f(new float[4][4]);
    }

    public float[][] getData() {
        return data;
    }

    public float get(int row, int col) {
        return data[row][col];
    }

    public Matrix4f add(Matrix4f other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[i][j] + other.data[i][j];
            }
        }
        return new Matrix4f(result);
    }

    public Matrix4f subtract(Matrix4f other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = this.data[i][j] - other.data[i][j];
            }
        }
        return new Matrix4f(result);
    }

    public Matrix4f multiply(Matrix4f other) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    result[i][j] += this.data[i][k] * other.data[k][j];
                }
            }
        }
        return new Matrix4f(result);
    }

    public Matrix4f transpose() {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[j][i] = this.data[i][j];
            }
        }
        return new Matrix4f(result);
    }

    public float determinant() {
        float det = 0;
        for (int i = 0; i < 4; i++) {
            det += (i % 2 == 0 ? 1 : -1) * data[0][i] * minor(0, i);
        }
        return det;
    }

    private float minor(int row, int col) {
        float[][] minorMatrix = new float[3][3];
        int minorRow = 0;
        for (int i = 0; i < 4; i++) {
            if (i == row) continue;
            int minorCol = 0;
            for (int j = 0; j < 4; j++) {
                if (j == col) continue;
                minorMatrix[minorRow][minorCol] = data[i][j];
                minorCol++;
            }
            minorRow++;
        }
        Matrix3f minor = new Matrix3f(minorMatrix);
        return minor.determinant();
    }

    public Matrix4f inverse() {
        float det = determinant();
        if (Math.abs(det) < 1e-5) {
            throw new IllegalArgumentException("Нельзя найти обратную матрицу, определитель равен нулю.");
        }

        float[][] result = new float[4][4];
        float invDet = 1.0f / det;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float cofactor = (float) (Math.pow(-1, i + j) * minor(i, j));
                result[j][i] = cofactor * invDet;
            }
        }

        return new Matrix4f(result);
    }

    public Vector4f multiplyByVector(Vector4f v){
        float[][] result = new float[4][1];
        result[0][0] = data[0][0] * v.getX() + data[0][1] * v.getY() + data[0][2] * v.getZ() + data[0][3] * v.getW();
        result[1][0] = data[1][0] * v.getX() + data[1][1] * v.getY() + data[1][2] * v.getZ() + data[1][3] * v.getW();
        result[2][0] = data[2][0] * v.getX() + data[2][1] * v.getY() + data[2][2] * v.getZ() + data[2][3] * v.getW();
        result[3][0] = data[3][0] * v.getX() + data[3][1] * v.getY() + data[3][2] * v.getZ() + data[3][3] * v.getW();
        return new Vector4f(result[0][0], result[1][0], result[2][0], result[3][0]);
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        Vector4f v4 = new Vector4f(vertex, 1);
        v4 = matrix.multiplyByVector(v4);
        float x, y, z, w;
        x = v4.getX();
        y = v4.getY();
        z = v4.getZ();
        w = v4.getW();
        return new Vector3f(x / w, y / w, z / w);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix4f matrix4 = (Matrix4f) obj;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (Math.abs(data[i][j] - matrix4.data[i][j]) >= 1e-5) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append("[");
            for (int j = 0; j < 4; j++) {
                sb.append(String.format(Locale.US, "%.4f", data[i][j]));
                if (j < 3) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}