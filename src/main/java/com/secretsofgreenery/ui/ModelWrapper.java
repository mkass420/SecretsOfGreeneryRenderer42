package com.secretsofgreenery.ui;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector3f;
import com.secretsofgreenery.model.Model;
import com.secretsofgreenery.render_engine.AffineTransform;
import javafx.scene.image.Image;

public class ModelWrapper {
    private String name;
    private Model originalModel; // The model in local space
    private Image texture;
    private AffineTransform transform;
    private boolean  isVisible;

    public ModelWrapper(String name, Model model) {
        this.name = name;
        this.originalModel = model;
        this.transform = new AffineTransform();
        this.isVisible = true;

        // Initialize with default values
        transform.setTranslation(new Vector3f(0, 0, 0));
        transform.setRotation(new Vector3f(0, 0, 0));
        transform.setScaling(new Vector3f(1, 1, 1));
        transform.apply();
    }

    // Getters
    public Model getOriginalModel() { return originalModel; }
    public String getName() { return name; }
    public Image getTexture() { return texture; }
    public Vector3f getPosition() {
        return transform.getTranslation();
    }

    public Vector3f getRotation() {
        Vector3f rads = transform.getRotation();
        return new Vector3f(
                (float) Math.toDegrees(rads.getX()),
                (float) Math.toDegrees(rads.getY()),
                (float) Math.toDegrees(rads.getZ())
        );
    }

    public Vector3f getScale() {
        return transform.getScaling();
    }
    public Matrix4f getModelMatrix() {
        return transform.apply();
    }
    public  boolean getIsVisibleProp() { return  isVisible;}


    //Setters
    public void setName(String name) { this.name = name; }
    public void setTexture(Image texture) { this.texture = texture; }
    public void setPosition(Vector3f position) {
        transform.setTranslation(position);
    }

    public void setRotation(Vector3f rotationDegrees) {
        Vector3f rads = new Vector3f(
                (float) Math.toRadians(rotationDegrees.getX()),
                (float) Math.toRadians(rotationDegrees.getY()),
                (float) Math.toRadians(rotationDegrees.getZ())
        );
        transform.setRotation(rads);
    }

    public void setScale(Vector3f scale) {
        transform.setScaling(scale);
    }
    public void setVisible(boolean isVisible) {this.isVisible = isVisible;}

    @Override
    public String toString() {
        return name;
    }
}
