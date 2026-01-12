package com.secretsofgreenery.model;

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

        assertEquals(expectedModel, testModel);
    }

    @Test
    void testRemovePolygon(){
        Model testModel = new Model();
        testModel.setVertices(new ArrayList<>(Arrays.asList(new Vector3f(0, 0, 0), new Vector3f(1, 0,0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1))));

        Polygon p1 = new Polygon();
        ArrayList<Integer> v1 = new ArrayList<>(Arrays.asList(0, 1, 3));
        p1.setVertexIndices(v1);

        Polygon p2 = new Polygon();
        ArrayList<Integer> v2 = new ArrayList<>(Arrays.asList(1, 2, 3));
        p2.setVertexIndices(v2);

        testModel.setPolygons(new ArrayList<>(Arrays.asList(p1, p2)));

        Model result1 = testModel.removePolygon(p2, true);
        Model result2 = testModel.removePolygon(p2, false);

        Model expected1 = new Model();
        Model expected2 = new Model();
        expected1.setVertices(new ArrayList<>(Arrays.asList(new Vector3f(0, 0, 0), new Vector3f(1, 0,0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1))));
        expected2.setVertices(new ArrayList<>(Arrays.asList(new Vector3f(0, 0, 0), new Vector3f(1, 0,0), null, new Vector3f(0, 0, 1))));
        expected1.setPolygons(new ArrayList<>(Arrays.asList(p1)));
        expected2.setPolygons(new ArrayList<>(Arrays.asList(p1)));

        assertEquals(result1, expected1);
        assertEquals(result2, expected2);
    }
}
