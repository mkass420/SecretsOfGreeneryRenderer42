package com.secretsofgreenery.math;

import java.util.Locale;

public class Vector2f {
    private final float x;
    private final float y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public Vector2f add(Vector2f other) {
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    public Vector2f subtract(Vector2f other) {
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    public Vector2f multiply(float scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    public Vector2f divide(float scalar) {
        if (Math.abs(scalar) < 1e-10) {
            throw new IllegalArgumentException("Невозможно осуществить деление вектора на ноль.");
        }
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vector2f normalize() {
        float len = length();
        if (len < 1e-10) {
            throw new IllegalArgumentException("Невозможно нормализовать нулевой вектор.");
        }
        return new Vector2f(x / len, y / len);
    }

    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector2f vector2 = (Vector2f) obj;
        return Math.abs(x - vector2.x) < 1e-10 &&
                Math.abs(y - vector2.y) < 1e-10;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,"Vector2(%.2f, %.2f)", x, y);
    }
}