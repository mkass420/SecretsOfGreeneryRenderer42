package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector3f;
import com.secretsofgreenery.math.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatrixFactoriesTest {

    /**
     * Тестируем только видовую и проекционные матрицы, остальные тестируются в AffineTest
     */

    @Test
    void testCreateProjection() {
        // Условия теста
        float fov = (float) Math.toRadians(90); // 90 градусов
        float aspectRatio = 1.0f; // Квадратный экран
        float near = 1.0f;
        float far = 10.0f;

        Matrix4f projection = MatrixFactories.createProjection(fov, aspectRatio, near, far);

        assertEquals(1.0f, projection.get(0, 0), 1e-5);
        assertEquals(1.0f, projection.get(1, 1), 1e-5);
        assertEquals(11.0f / 9.0f, projection.get(2, 2), 1e-5);
        assertEquals(20.0f / -9.0f, projection.get(2, 3), 1e-5);
        assertEquals(1.0f, projection.get(3, 2), 1e-5);
        assertEquals(0.0f, projection.get(3, 3), 1e-5);
    }

    @Test
    void testCreateView() {
        Vector3f eye = new Vector3f(0, 0, 10);
        Vector3f target = new Vector3f(0, 0, 0);
        Vector3f up = new Vector3f(0, 1, 0);

        Matrix4f view = MatrixFactories.createView(eye, target, up);

        // Точка в начале координат (target) должна перейти в (0, 0, -10) в пространстве камеры,
        // так как камера стоит в (0,0,10) и смотрит в -Z (обычно)
        Vector4f worldPoint = new Vector4f(0, 0, 0, 1);
        Vector4f viewPoint = view.multiplyByVector(worldPoint);

        assertEquals(0, viewPoint.getX(), 1e-5);
        assertEquals(0, viewPoint.getY(), 1e-5);
        assertEquals(10, Math.abs(viewPoint.getZ()), 1e-5);
    }
}