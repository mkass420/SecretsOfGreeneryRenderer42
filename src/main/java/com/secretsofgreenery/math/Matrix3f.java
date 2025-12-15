package com.secretsofgreenery.math;

import java.util.Locale;

public class Matrix3f {
    private final float[][] data;

    public Matrix3f(float[][] data) {
        if (data.length != 3 || data[0].length != 3) {
            throw new IllegalArgumentException("Матрица должна быть вида 3x3.");
        }
        this.data = new float[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(data[i], 0, this.data[i], 0, 3);
        }
    }

    public Matrix3f() {
        this.data = new float[3][3];
    }

    public static Matrix3f identity() {
        float[][] identity = {
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        };
        return new Matrix3f(identity);
    }

    public static Matrix3f zero() {
        return new Matrix3f(new float[3][3]);
    }

    public float get(int row, int col) {
        return data[row][col];
    }

    public Matrix3f add(Matrix3f other) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.data[i][j] + other.data[i][j];
            }
        }
        return new Matrix3f(result);
    }

    public Matrix3f subtract(Matrix3f other) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i][j] = this.data[i][j] - other.data[i][j];
            }
        }
        return new Matrix3f(result);
    }

    public Matrix3f multiply(Matrix3f other) {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    result[i][j] += this.data[i][k] * other.data[k][j];
                }
            }
        }
        return new Matrix3f(result);
    }

    public Matrix3f transpose() {
        float[][] result = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[j][i] = this.data[i][j];
            }
        }
        return new Matrix3f(result);
    }

    public float determinant() {
        return data[0][0] * (data[1][1] * data[2][2] - data[1][2] * data[2][1])
                - data[0][1] * (data[1][0] * data[2][2] - data[1][2] * data[2][0])
                + data[0][2] * (data[1][0] * data[2][1] - data[1][1] * data[2][0]);
    }

    public Matrix3f inverse() {
        float det = determinant();
        if (Math.abs(det) < 1e-10) {
            throw new IllegalArgumentException("Нельзя найти обратную матрицу, определитель равен нулю.");
        }

        float[][] result = new float[3][3];
        float invDet = 1.0f / det;

        result[0][0] = (data[1][1] * data[2][2] - data[1][2] * data[2][1]) * invDet;
        result[0][1] = (data[0][2] * data[2][1] - data[0][1] * data[2][2]) * invDet;
        result[0][2] = (data[0][1] * data[1][2] - data[0][2] * data[1][1]) * invDet;
        result[1][0] = (data[1][2] * data[2][0] - data[1][0] * data[2][2]) * invDet;
        result[1][1] = (data[0][0] * data[2][2] - data[0][2] * data[2][0]) * invDet;
        result[1][2] = (data[0][2] * data[1][0] - data[0][0] * data[1][2]) * invDet;
        result[2][0] = (data[1][0] * data[2][1] - data[1][1] * data[2][0]) * invDet;
        result[2][1] = (data[0][1] * data[2][0] - data[0][0] * data[2][1]) * invDet;
        result[2][2] = (data[0][0] * data[1][1] - data[0][1] * data[1][0]) * invDet;

        return new Matrix3f(result);
    }

    public Vector3f multiplyByVector(Vector3f v){
        float[][] result = new float[3][1];
        result[0][0] = data[0][0] * v.getX() + data[0][1] * v.getY() + data[0][2] * v.getZ();
        result[1][0] = data[1][0] * v.getX() + data[1][1] * v.getY() + data[1][2] * v.getZ();
        result[2][0] = data[2][0] * v.getX() + data[2][1] * v.getY() + data[2][2] * v.getZ();
        return new Vector3f(result[0][0], result[1][0], result[2][0]);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix3f matrix3 = (Matrix3f) obj;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (Math.abs(data[i][j] - matrix3.data[i][j]) >= 1e-10) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append("[");
            for (int j = 0; j < 3; j++) {
                sb.append(String.format(Locale.US,"%.4f", data[i][j]));
                if (j < 2) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}