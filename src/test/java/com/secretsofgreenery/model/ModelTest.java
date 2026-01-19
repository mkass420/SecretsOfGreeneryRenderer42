package com.secretsofgreenery.model;

import com.secretsofgreenery.math.Vector2f;
import com.secretsofgreenery.math.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelTest {

    @Test
    void testRemoveVertex(){
        Model expectedModel = new Model();
        Model testModel = new Model();

        Polygon p1 = new Polygon();
        ArrayList<Integer> v1 = new ArrayList<>(Arrays.asList(0, 1, 3));
        p1.setVertexIndices(v1);

        Polygon p2 = new Polygon();
        ArrayList<Integer> v2 = new ArrayList<>(Arrays.asList(1, 2, 3));
        p2.setVertexIndices(v2);

        Polygon p3 = new Polygon();
        ArrayList<Integer> v3 = new ArrayList<>(Arrays.asList(0, 1, 2));
        p3.setVertexIndices(v3);

        Polygon p4 = new Polygon();
        ArrayList<Integer> v4 = new ArrayList<>(Arrays.asList(0, 2, 3));
        p4.setVertexIndices(v4);

        ArrayList<Polygon> initialPolygons = new ArrayList<>(Arrays.asList(p1, p2, p3, p4));
        ArrayList<Polygon> expectedPolygons = new ArrayList<>(Arrays.asList(p1));

        expectedModel.setPolygons(expectedPolygons);
        ArrayList<Vector3f> expectedVertices = new ArrayList<>(Arrays.asList(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), null, new Vector3f(0, 0, 1)));
        expectedModel.setVertices(expectedVertices);

        testModel.setPolygons(initialPolygons);
        testModel.setVertices(new ArrayList<>(Arrays.asList(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1))));

        testModel.removeVertex(2);
        Normals.recalculateVertexNormals(expectedModel);

        assertEquals(expectedModel, testModel);
    }

    @Test
    void testRemovePolygon() {
        ArrayList<Vector3f> vertices = new ArrayList<>(Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0),
                new Vector3f(0, 0, 1)
        ));
        ArrayList<Vector2f> textureVertices = new ArrayList<>(Arrays.asList(
                new Vector2f(0, 0),
                new Vector2f(0, 1),
                new Vector2f(1, 1),
                new Vector2f(2, 2)
        ));

        Polygon p1 = new Polygon();
        p1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 3)));
        p1.setTextureVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 3)));

        Polygon p2 = new Polygon();
        p2.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));
        p2.setTextureVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));

        // --- ТЕСТ 1: Удаление с сохранением висячих вершин ---
        Model model1 = new Model();
        model1.setVertices(new ArrayList<>(vertices));
        model1.setTextureVertices(new ArrayList<>(textureVertices));
        model1.setPolygons(new ArrayList<>(Arrays.asList(p1, p2)));

        Model result1 = model1.removePolygon(p2, true);

        Model expected1 = new Model();
        expected1.setVertices(new ArrayList<>(vertices));
        expected1.setTextureVertices(new ArrayList<>(textureVertices));
        expected1.setPolygons(new ArrayList<>(Arrays.asList(p1)));
        Normals.recalculateVertexNormals(expected1);

        assertEquals(expected1, result1);

        // --- ТЕСТ 2: Удаление с удалением висячих вершин ---
        Model model2 = new Model();
        model2.setVertices(new ArrayList<>(vertices));
        model2.setPolygons(new ArrayList<>(Arrays.asList(p1, p2)));

        Model result2 = model2.removePolygon(p2, false);

        Model expected2 = new Model();
        ArrayList<Vector3f> expectedVertices2 = new ArrayList<>(Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                null,
                new Vector3f(0, 0, 1)
        ));
        ArrayList<Vector2f> expectedTextureVerties2 = new ArrayList<>(Arrays.asList(
                new Vector2f(0, 0),
                new Vector2f(0, 1),
                null,
                new Vector2f(2, 2)
        ));
        expected2.setVertices(expectedVertices2);
        expected2.setPolygons(new ArrayList<>(Arrays.asList(p1)));
        Normals.recalculateVertexNormals(expected2);

        assertEquals(expected2, result2);
    }
}
