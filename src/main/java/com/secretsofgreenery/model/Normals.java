package com.secretsofgreenery.model;

import com.secretsofgreenery.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Normals {
    public static Vector3f polygonNormal(Polygon p, List<Vector3f> vertices){
        List<Integer> indices = p.getVertexIndices();
        Vector3f v0 = vertices.get(indices.get(0));
        Vector3f v1 = vertices.get(indices.get(1));
        Vector3f v2 = vertices.get(indices.get(2));

        Vector3f e1 = v1.subtract(v0);
        Vector3f e2 = v2.subtract(v0);

        return e1.cross(e2);
    }

    public static void recalculateVertexNormals(Model model){
        List<Vector3f> vertices = model.getVertices();
        ArrayList<Polygon> polygons = model.getPolygons();

        ArrayList<Vector3f> vertexNormals = new ArrayList<Vector3f>();
        for(int i = 0; i < vertices.size(); i++){
            vertexNormals.add(new Vector3f(0, 0, 0));
        }

        for(Polygon p: polygons){
            Vector3f polygonNormal = polygonNormal(p, vertices);
            for(Integer i : p.getVertexIndices()){
                vertexNormals.set(i, vertexNormals.get(i).add(polygonNormal));
            }
        }

        // если вершина нигде не используется - направляем нормаль вверх, остальные нормализуем
        for(Vector3f n : vertexNormals){
            n = n.length() > 1e-10 ? n.normalize() : new Vector3f(0, 1, 0);
        }

        model.setNormals(vertexNormals);
    }
}
