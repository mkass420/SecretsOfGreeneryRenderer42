package com.secretsofgreenery.model;

import java.util.ArrayList;
import java.util.List;

public class Triangulation {

    public static void triangulate(Model model) {
        ArrayList<Polygon> newPolygons = new ArrayList<>();

        for (Polygon polygon : model.getPolygons()) {
            newPolygons.addAll(triangulatePolygon(polygon));
        }

        model.setPolygons(newPolygons);
    }

    private static List<Polygon> triangulatePolygon(Polygon polygon) {
        List<Integer> vertices = polygon.getVertexIndices();
        List<Integer> textureVertices = polygon.getTextureVertexIndices();
        List<Integer> normals = polygon.getNormalIndices();

        ArrayList<Polygon> triangles = new ArrayList<>();

        if (vertices.size() < 3) {
            return triangles;
        }

        for (int i = 1; i < vertices.size() - 1; i++) {
            Polygon triangle = new Polygon();

            ArrayList<Integer> newVertexIndices = new ArrayList<>();
            newVertexIndices.add(vertices.get(0));
            newVertexIndices.add(vertices.get(i));
            newVertexIndices.add(vertices.get(i + 1));
            triangle.setVertexIndices(newVertexIndices);

            if (textureVertices != null && !textureVertices.isEmpty()) {
                ArrayList<Integer> newTextureIndices = new ArrayList<>();
                newTextureIndices.add(textureVertices.get(0));
                newTextureIndices.add(textureVertices.get(i));
                newTextureIndices.add(textureVertices.get(i + 1));
                triangle.setTextureVertexIndices(newTextureIndices);
            }

            if (normals != null && !normals.isEmpty()) {
                ArrayList<Integer> newNormalIndices = new ArrayList<>();
                newNormalIndices.add(normals.get(0));
                newNormalIndices.add(normals.get(i));
                newNormalIndices.add(normals.get(i + 1));
                triangle.setNormalIndices(newNormalIndices);
            }

            triangles.add(triangle);
        }

        return triangles;
    }
}