package com.secretsofgreenery.affine_transforms;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector4f;
import com.secretsofgreenery.render_engine.AffineTransform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AffineTest {
    @Test
    void testScale() {
        Vector4f v4 = new Vector4f(1, 2, 3, 1);
        Matrix4f m4 = new AffineTransform().scale(4, 5, 6).getMatrix();
        float[][] expectedM = {{4, 0, 0, 0},
                                {0, 5, 0, 0},
                                {0, 0, 6, 0},
                                {0, 0, 0, 1}};
        assertEquals(new Matrix4f(expectedM), m4);
        assertEquals(new Vector4f(4, 10, 18, 1), m4.multiplyByVector(v4));
    }

    @Test
    void testTranslate() {
        Vector4f v4 = new Vector4f(1, 2, 3, 1);
        Matrix4f m4 = new AffineTransform().translate(4, 5, 6).getMatrix();
        float[][] expectedM = {{1, 0, 0, 4},
                                {0, 1, 0, 5},
                                {0, 0, 1, 6},
                                {0, 0, 0, 1}};
        assertEquals(new Matrix4f(expectedM), m4);
        assertEquals(new Vector4f(5, 7, 9, 1), m4.multiplyByVector(v4));
    }

    @Test
    void testRotateX() {
        float angle = (float) Math.toRadians(45.0);
        Vector4f v4 = new Vector4f(1, 2, 3, 1);
        Matrix4f m4 = new AffineTransform().rotateX(angle).getMatrix();
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float[][] expectedM = {{1, 0, 0, 0},
                                {0, c, s, 0},
                                {0, -s, c, 0},
                                {0, 0, 0, 1}};
        float expectedX = 1;
        float expectedY = 3.53553390593f;
        float expectedZ = 0.70710678119f;
        float expectedW = 1;
        assertEquals(new Matrix4f(expectedM), m4);
        assertEquals(new Vector4f(expectedX, expectedY, expectedZ, expectedW), m4.multiplyByVector(v4));
    }

    @Test
    void testRotateY() {
        float angle = (float) Math.toRadians(30.0);
        Vector4f v4 = new Vector4f(1, 2, 3, 1);
        Matrix4f m4 = new AffineTransform().rotateY(angle).getMatrix();
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float[][] expectedM = {{ c, 0, s, 0},
                                { 0, 1, 0, 0},
                                {-s, 0, c, 0},
                                { 0, 0, 0, 1}};
        float expectedX = 2.36602540378f;
        float expectedY = 2;
        float expectedZ = 2.09807621135f;
        float expectedW = 1;
        assertEquals(new Matrix4f(expectedM), m4);
        assertEquals(new Vector4f(expectedX, expectedY, expectedZ, expectedW), m4.multiplyByVector(v4));
    }

    @Test
    void testRotateZ() {
        float angle = (float) Math.toRadians(78.3);
        Vector4f v4 = new Vector4f(1, 2, 3, 1);
        Matrix4f m4 = new AffineTransform().rotateZ(angle).getMatrix();
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float[][] expectedM = {{ c, s, 0, 0},
                                {-s, c, 0, 0},
                                { 0, 0, 1, 0},
                                { 0, 0, 0, 1}};
        float expectedX = 2.1612329166f;
        float expectedY = -0.57364821991f;
        float expectedZ = 3;
        float expectedW = 1;
        assertEquals(new Matrix4f(expectedM), m4);
        assertEquals(new Vector4f(expectedX, expectedY, expectedZ, expectedW), m4.multiplyByVector(v4));
    }
}