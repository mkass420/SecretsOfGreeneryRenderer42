package com.secretsofgreenery.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TriangulationTest {
    @Test
    void testTriangulation(){
        Model model = new Model();
        Polygon polygon = new Polygon();
        // Original: [4, 8, 71, 10, 1, 2, 5]
        // Indices:   0  1   2   3  4  5  6
        ArrayList<Integer> vertexIndices = new ArrayList<>(Arrays.asList(4, 8, 71, 10, 1, 2, 5));
        polygon.setVertexIndices(vertexIndices);
        model.setPolygons(new ArrayList<Polygon>(Collections.singleton(polygon)));

        Triangulation.triangulate(model);

        // The algorithm creates triangles (0, i, i+1)

        // i=1: (0, 1, 2) -> [4, 8, 71]
        Polygon t1 = new Polygon();
        ArrayList<Integer> vertices = new ArrayList<>(Arrays.asList(4, 8, 71));
        t1.setVertexIndices(vertices);

        // i=2: (0, 2, 3) -> [4, 71, 10]
        Polygon t2 = new Polygon();
        vertices = new ArrayList<>(Arrays.asList(4, 71, 10));
        t2.setVertexIndices(vertices);

        // i=3: (0, 3, 4) -> [4, 10, 1]
        Polygon t3 = new Polygon();
        vertices = new ArrayList<>(Arrays.asList(4, 10, 1));
        t3.setVertexIndices(vertices);

        // i=4: (0, 4, 5) -> [4, 1, 2]
        Polygon t4 = new Polygon();
        vertices = new ArrayList<>(Arrays.asList(4, 1, 2));
        t4.setVertexIndices(vertices);

        // i=5: (0, 5, 6) -> [4, 2, 5]
        Polygon t5 = new Polygon();
        vertices = new ArrayList<>(Arrays.asList(4, 2, 5));
        t5.setVertexIndices(vertices);

        ArrayList<Polygon> rightTriangles = new ArrayList<>(Arrays.asList(t1, t2, t3, t4, t5));

        assertEquals(rightTriangles, model.getPolygons());
    }
}