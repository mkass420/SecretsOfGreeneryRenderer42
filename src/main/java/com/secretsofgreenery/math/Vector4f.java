package com.secretsofgreenery.math;

import java.util.Locale;

public class Vector4f {
    protected final float x;
    protected final float y;
    protected final float z;
    protected final float w;

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4f(Vector3f v, float w) {
        this.x = v.getX();
        this.y = v.getY();
        this.z = v.getZ();
        this.w = w;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getW() { return w; }

    public Vector4f add(Vector4f other) {
        return new Vector4f(this.x + other.x, this.y + other.y,
                this.z + other.z, this.w + other.w);
    }

    public Vector4f subtract(Vector4f other) {
        return new Vector4f(this.x - other.x, this.y - other.y,
                this.z - other.z, this.w - other.w);
    }

    public Vector4f multiply(float scalar) {
        return new Vector4f(this.x * scalar, this.y * scalar,
                this.z * scalar, this.w * scalar);
    }

    public Vector4f divide(float scalar) {
        if (Math.abs(scalar) < 1e-5) {
            throw new IllegalArgumentException("Невозможно осуществить деление вектора на ноль.");
        }
        return new Vector4f(this.x / scalar, this.y / scalar,
                this.z / scalar, this.w / scalar);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public Vector4f normalize() {
        float len = length();
        if (len < 1e-5) {
            throw new IllegalArgumentException("Невозможно нормализовать нулевой вектор.");
        }
        return new Vector4f(x / len, y / len, z / len, w / len);
    }

    public float dot(Vector4f other) {
        return this.x * other.x + this.y * other.y +
                this.z * other.z + this.w * other.w;
    }

    public static Vector3f toVector3f(Vector4f v){
        return new Vector3f(v.getX(), v.getY(), v.getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector4f vector4 = (Vector4f) obj;
        return Math.abs(x - vector4.x) < 1e-5 &&
                Math.abs(y - vector4.y) < 1e-5 &&
                Math.abs(z - vector4.z) < 1e-5 &&
                Math.abs(w - vector4.w) < 1e-5;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,"Vector4f(%.4f, %.4f, %.4f, %.4f)", x, y, z, w);
    }
}