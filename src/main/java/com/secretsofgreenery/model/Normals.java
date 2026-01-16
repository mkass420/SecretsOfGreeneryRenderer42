package com.secretsofgreenery.model;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector3f;
import com.secretsofgreenery.math.Vector4f;

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
        for(int i = 0; i < vertexNormals.size(); i++){
            Vector3f normal = vertexNormals.get(i);
            vertexNormals.set(i, normal.length() > 1e-5 ? normal.normalize() : new Vector3f(0, 1, 0));
        }

        for (Polygon p : polygons) {
            p.setNormalIndices(new ArrayList<>(p.getVertexIndices()));
        }

        model.setNormals(vertexNormals);
    }

    public static Vector3f multiplyMatrix4ByNormal(Matrix4f matrix, Vector3f normal) {
        Vector4f v4 = new Vector4f(normal.getX(), normal.getY(), normal.getZ(), 0);
        Vector4f result4 = matrix.multiplyByVector(v4);
        return new Vector3f(result4.getX(), result4.getY(), result4.getZ()).normalize();
    }
}
