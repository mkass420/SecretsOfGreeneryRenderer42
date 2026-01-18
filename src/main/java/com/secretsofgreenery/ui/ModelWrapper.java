package com.secretsofgreenery.ui;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector3f;
import com.secretsofgreenery.model.Model;
import com.secretsofgreenery.model.Normals;
import com.secretsofgreenery.model.Polygon;
import com.secretsofgreenery.render_engine.AffineTransform;
import com.secretsofgreenery.render_engine.Texture;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ModelWrapper {
    private String name;
    private Model originalModel;
    private Texture texture;
    private AffineTransform transform;
    private boolean isVisible;

    private Set<Integer> selectedPolygonIndices = new HashSet<>();
    private Set<Integer> selectedVertexIndices = new HashSet<>();

    public ModelWrapper(String name, Model model) {
        this.name = name;
        this.originalModel = model;
        this.transform = new AffineTransform();
        this.isVisible = true;

        transform.setTranslation(new Vector3f(0, 0, 0));
        transform.setRotation(new Vector3f(0, 0, 0));
        transform.setScaling(new Vector3f(1, 1, 1));
        transform.apply();
    }

    public Set<Integer> getSelectedPolygonIndices() { return selectedPolygonIndices; }
    public Set<Integer> getSelectedVertexIndices() { return selectedVertexIndices; }

    public void handlePolygonSelection(int index, boolean isMultiSelect) {
        if (index == -1) {
            if (!isMultiSelect) {
                selectedPolygonIndices.clear();
                selectedVertexIndices.clear(); // Сброс вершин при сбросе полигонов
            }
            return;
        }

        if (isMultiSelect) {
            if (selectedPolygonIndices.contains(index)) {
                selectedPolygonIndices.remove(index);
                // При снятии выделения с полигона, логично НЕ снимать выделение с вершин,
                // так как они могут принадлежать другим выделенным полигонам.
            } else {
                selectedPolygonIndices.add(index);
                // При выделении полигона - выделяем его вершины
                selectVerticesOfPolygon(index);
            }
        } else {
            selectedPolygonIndices.clear();
            selectedVertexIndices.clear();

            selectedPolygonIndices.add(index);
            selectVerticesOfPolygon(index);
        }
    }

    public void handleVertexSelection(int index, boolean isMultiSelect) {
        if (index == -1) {
            if (!isMultiSelect) {
                selectedVertexIndices.clear();
                selectedPolygonIndices.clear();
            }
            return;
        }

        if (isMultiSelect) {
            if (selectedVertexIndices.contains(index)) {
                selectedVertexIndices.remove(index);
            } else {
                selectedVertexIndices.add(index);
            }
        } else {
            selectedVertexIndices.clear();
            selectedPolygonIndices.clear(); // Одиночный клик по вершине сбрасывает полигоны
            selectedVertexIndices.add(index);
        }

        checkPolygonsByVertices();
    }

    private void selectVerticesOfPolygon(int polygonIndex) {
        Polygon p = originalModel.getPolygons().get(polygonIndex);
        selectedVertexIndices.addAll(p.getVertexIndices());
    }

    private void checkPolygonsByVertices() {
        for (int i = 0; i < originalModel.getPolygons().size(); i++) {
            Polygon p = originalModel.getPolygons().get(i);
            ArrayList<Integer> indices = p.getVertexIndices();

            // Проверяем, содержатся ли все 3 вершины полигона в списке selectedVertexIndices
            boolean allVerticesSelected = selectedVertexIndices.containsAll(indices);

            if (allVerticesSelected) {
                selectedPolygonIndices.add(i);
            } else {
                selectedPolygonIndices.remove(i);
            }
        }
    }

    public void clearSelection() {
        selectedPolygonIndices.clear();
        selectedVertexIndices.clear();
    }

    public void translateSelectedVertices(Vector3f delta) {
        Set<Integer> verticesToMove = new HashSet<>(selectedVertexIndices);

        for (Integer polyIndex : selectedPolygonIndices) {
            if (polyIndex >= 0 && polyIndex < originalModel.getPolygons().size()) {
                verticesToMove.addAll(originalModel.getPolygons().get(polyIndex).getVertexIndices());
            }
        }

        if (verticesToMove.isEmpty()) return;

        for (Integer vertexIndex : verticesToMove) {
            Vector3f currentPos = originalModel.getVertices().get(vertexIndex);
            Vector3f newPos = currentPos.add(delta);
            originalModel.getVertices().set(vertexIndex, newPos);
        }

        Normals.recalculateVertexNormals(originalModel);
    }

    public Model getOriginalModel() { return originalModel; }
    public String getName() { return name; }
    public Texture getTexture() { return texture; }
    public Vector3f getPosition() { return transform.getTranslation(); }
    public Vector3f getRotation() {
        Vector3f rads = transform.getRotation();
        return new Vector3f((float) Math.toDegrees(rads.getX()), (float) Math.toDegrees(rads.getY()), (float) Math.toDegrees(rads.getZ()));
    }
    public Vector3f getScale() { return transform.getScaling(); }
    public Matrix4f getModelMatrix() { return transform.apply(); }
    public boolean getIsVisibleProp() { return isVisible; }

    public void setName(String name) { this.name = name; }
    public void setTexture(Image texture) { this.texture = new Texture(texture); }
    public void setTexture(Texture texture){ this.texture = texture; }
    public void setPosition(Vector3f position) { transform.setTranslation(position); }
    public void setRotation(Vector3f rotationDegrees) {
        Vector3f rads = new Vector3f((float) Math.toRadians(rotationDegrees.getX()), (float) Math.toRadians(rotationDegrees.getY()), (float) Math.toRadians(rotationDegrees.getZ()));
        transform.setRotation(rads);
    }
    public void setScale(Vector3f scale) { transform.setScaling(scale); }
    public void setVisible(boolean isVisible) {this.isVisible = isVisible;}

    @Override public String toString() { return name; }
}