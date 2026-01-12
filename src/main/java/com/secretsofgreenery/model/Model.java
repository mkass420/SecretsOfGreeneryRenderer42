package com.secretsofgreenery.model;
import com.secretsofgreenery.math.Vector2f;
import com.secretsofgreenery.math.Vector3f;

import java.util.*;

public class Model {
    private ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    private ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    private ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    private ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    public ArrayList<Vector3f> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<Vector3f> vertices) {
        this.vertices = vertices;
    }

    public ArrayList<Vector2f> getTextureVertices() {
        return textureVertices;
    }

    public void setTextureVertices(ArrayList<Vector2f> textureVertices) {
        this.textureVertices = textureVertices;
    }

    public ArrayList<Vector3f> getNormals() {
        return normals;
    }

    public void setNormals(ArrayList<Vector3f> normals) {
        this.normals = normals;
    }

    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<Polygon> polygons) {
        this.polygons = polygons;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return Objects.equals(vertices, model.vertices) && Objects.equals(textureVertices, model.textureVertices) && Objects.equals(normals, model.normals) && Objects.equals(polygons, model.polygons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertices, textureVertices, normals, polygons);
    }

    public void removeVertex(int vertexIndex){
        ArrayList<Polygon> polygons = this.getPolygons();

        ArrayList<Polygon> rightPolygons = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++){
            Polygon currentPolygon = polygons.get(i);
            ArrayList<Integer> vertices = currentPolygon.getVertexIndices();
            if (vertices.contains(vertexIndex)){
                break;
            }
            else{
                rightPolygons.add(currentPolygon);
            }
        }

        ArrayList<Vector3f> rightVertices = new ArrayList<>();
        for (int i = 0; i < this.vertices.size(); i++){
            Vector3f currentVertex = this.vertices.get(i);
            if (i != vertexIndex){
                rightVertices.add(currentVertex);
            }
            else{
                rightVertices.add(null);
            }
        }

        this.setPolygons(rightPolygons);
        this.setVertices(rightVertices);
    }

    public Model removePolygon(Polygon polygon, Boolean leaveHangingVertices){
        ArrayList<Polygon> polygons = this.getPolygons();
        polygons.remove(polygon);
        this.setPolygons(polygons);

        ArrayList<Integer> hangingVertices = new ArrayList<>();
        ArrayList<Integer> removedVertices = polygon.getVertexIndices();
        ArrayList<Polygon> newPolygons = this.getPolygons();

        if (leaveHangingVertices == false){
            for (int i = 0; i < removedVertices.size(); i++) {
                int count = 0;
                for (int j = 0; j < newPolygons.size(); j++) {
                    if (newPolygons.get(j).getVertexIndices().contains(removedVertices.get(i))) {
                        count += 1;
                    }
                }
                if (count == 0) {
                    hangingVertices.add(removedVertices.get(i));
                }
            }

            ArrayList<Vector3f> newVertices = new ArrayList<>();
            for (int i = 0; i < this.vertices.size(); i++){
                if (hangingVertices.contains(i)){
                    newVertices.add(null);
                }
                else{
                    newVertices.add(this.vertices.get(i));
                }
            }
            this.setVertices(newVertices);
        }
        return this;
    }
}