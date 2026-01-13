package com.secretsofgreenery.model;

import com.secretsofgreenery.math.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NormalsTest {

    @Test
    void testPolygonNormal() {
        // Треугольник в плоскости XY: (0,0,0), (1,0,0), (0,1,0)
        // Нормаль должна смотреть в Z (0,0,1)
        List<Vector3f> vertices = Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0)
        );
        Polygon p = new Polygon();
        p.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        Vector3f normal = Normals.polygonNormal(p, vertices);

        assertEquals(0, normal.getX(), 1e-5);
        assertEquals(0, normal.getY(), 1e-5);
        assertEquals(1, normal.getZ(), 1e-5);
    }

    @Test
    void testRecalculateVertexNormals() {
        // Два треугольника, образующие плоскость (квадрат из двух треугольников)
        Model model = new Model();
        model.setVertices(new ArrayList<>(Arrays.asList(
                new Vector3f(0, 0, 0), // 0
                new Vector3f(1, 0, 0), // 1
                new Vector3f(0, 1, 0), // 2
                new Vector3f(1, 1, 0)  // 3
        )));

        Polygon p1 = new Polygon();
        p1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2))); // Нижний левый
        Polygon p2 = new Polygon();
        p2.setVertexIndices(new ArrayList<>(Arrays.asList(2, 1, 3))); // Верхний правый

        model.setPolygons(new ArrayList<>(Arrays.asList(p1, p2)));

        Normals.recalculateVertexNormals(model);

        List<Vector3f> normals = model.getNormals();
        assertEquals(4, normals.size());

        // Все вершины лежат в плоскости Z=0, нормали должны быть (0,0,1)
        for (Vector3f n : normals) {
            assertEquals(0, n.getX(), 1e-5);
            assertEquals(0, n.getY(), 1e-5);
            assertEquals(1, n.getZ(), 1e-5);
        }
    }
}