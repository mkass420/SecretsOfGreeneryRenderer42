package com.secretsofgreenery.ui;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector3f;
import com.secretsofgreenery.model.Model;
import com.secretsofgreenery.render_engine.AffineTransform;
import javafx.scene.image.Image;

public class SceneObject {
    private String name;
    private Model originalModel; // The model in local space
    private Image texture;
    private AffineTransform transform;

    public SceneObject(String name, Model model) {
        this.name = name;
        this.originalModel = model;
        this.transform = new AffineTransform();

        // Initialize with default values
        transform.setTranslation(new Vector3f(0, 0, 0));
        transform.setRotation(new Vector3f(0, 0, 0));
        transform.setScaling(new Vector3f(1, 1, 1));
        transform.apply();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Model getOriginalModel() { return originalModel; }

    public Image getTexture() { return texture; }
    public void setTexture(Image texture) { this.texture = texture; }

    public Vector3f getPosition() {
        return transform.getTranslation();
    }
    public void setPosition(Vector3f position) {
        transform.setTranslation(position);
    }

    public Vector3f getRotation() {
        Vector3f rads = transform.getRotation();
        return new Vector3f(
                (float) Math.toDegrees(rads.getX()),
                (float) Math.toDegrees(rads.getY()),
                (float) Math.toDegrees(rads.getZ())
        );
    }

    public void setRotation(Vector3f rotationDegrees) {
        Vector3f rads = new Vector3f(
                (float) Math.toRadians(rotationDegrees.getX()),
                (float) Math.toRadians(rotationDegrees.getY()),
                (float) Math.toRadians(rotationDegrees.getZ())
        );
        transform.setRotation(rads);
    }

    public Vector3f getScale() {
        return transform.getScaling();
    }
    public void setScale(Vector3f scale) {
        transform.setScaling(scale);
    }

    public Matrix4f getModelMatrix() {
        return transform.apply();
    }

    @Override
    public String toString() {
        return name;
    }
}
