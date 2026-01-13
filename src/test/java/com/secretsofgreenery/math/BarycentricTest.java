package com.secretsofgreenery.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BarycentricTest {

    @Test
    void testPointInside() {
        Point2f a = new Point2f(0, 0);
        Point2f b = new Point2f(10, 0);
        Point2f c = new Point2f(0, 10);

        // Точка в центре масс (должно быть 1/3, 1/3, 1/3)
        Point2f p = new Point2f(10.0f/3, 10.0f/3);

        Vector3f barycentric = Barycentric.barycentric(p, a, b, c);

        assertEquals(1.0f/3, barycentric.getX(), 1e-4, "Alpha should be ~0.33");
        assertEquals(1.0f/3, barycentric.getY(), 1e-4, "Beta should be ~0.33");
        assertEquals(1.0f/3, barycentric.getZ(), 1e-4, "Gamma should be ~0.33");

        assertEquals(1.0f, barycentric.getX() + barycentric.getY() + barycentric.getZ(), 1e-5);
    }

    @Test
    void testPointOnVertex() {
        Point2f a = new Point2f(0, 0);
        Point2f b = new Point2f(10, 0);
        Point2f c = new Point2f(0, 10);

        // Точка совпадает с A
        Vector3f barycentric = Barycentric.barycentric(a, a, b, c);

        // Alpha (вес A) должен быть 1
        assertEquals(1.0f, barycentric.getX(), 1e-5);
        assertEquals(0.0f, barycentric.getY(), 1e-5);
        assertEquals(0.0f, barycentric.getZ(), 1e-5);
    }

    @Test
    void testPointOutside() {
        Point2f a = new Point2f(0, 0);
        Point2f b = new Point2f(10, 0);
        Point2f c = new Point2f(0, 10);

        Point2f p = new Point2f(10, 10); // Явно снаружи

        Vector3f barycentric = Barycentric.barycentric(p, a, b, c);

        // Одна из координат должна быть отрицательной
        boolean isOutside = barycentric.getX() < 0 || barycentric.getY() < 0 || barycentric.getZ() < 0;
        assertTrue(isOutside, "Point outside triangle should have at least one negative coordinate");
    }
}