package com.secretsofgreenery.model.triangulation.src;

import com.secretsofgreenery.model.Polygon;
import java.util.ArrayList;

public class Triangulation {
    public static ArrayList<Polygon> Triangulation(Polygon polygon) {
        ArrayList<Integer> vertices = polygon.getVertexIndices();
        int vertices_count = vertices.size();
        ArrayList<Polygon> triangles = new ArrayList<>();

        int starting_point = vertices.get(0);

        for (int i = 1; i < vertices_count - 1; i++) {
            Polygon triangle = new Polygon();
            ArrayList<Integer> triangle_vertices = new ArrayList<>(3);

            triangle_vertices.add(starting_point);
            triangle_vertices.add(vertices.get(i + 1));
            triangle_vertices.add(vertices.get(i));

            triangle.setVertexIndices(triangle_vertices);
            triangles.add(triangle);
        }

        return triangles;
    }
}