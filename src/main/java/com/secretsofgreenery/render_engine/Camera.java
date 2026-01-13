package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    // Кэшированные матрицы (чтобы не пересчитывать каждый кадр)
    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;

    private boolean viewChanged = true;
    private boolean projectionChanged = true;

    public Camera(
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
    }


    public void setPosition(final Vector3f position) {
        this.position = position;
        this.viewChanged = true;
    }

    public void setTarget(final Vector3f target) {
        this.target = target;
        this.viewChanged = true;
    }

    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
        this.projectionChanged = true;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getTarget() {
        return target;
    }

    public void movePosition(final Vector3f translation) {
        this.position = this.position.add(translation);
        this.viewChanged = true;
    }
    public void moveTarget(final Vector3f translation) {
        this.target = this.target.add(translation);
        this.viewChanged = true;
    }

    public Matrix4f getViewMatrix() {
        if (viewChanged) {
            this.viewMatrix = MatrixFactories.createView(position, target, new Vector3f(0, 1, 0));
            this.viewChanged = false;
        }
        return this.viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        if (projectionChanged) {
            this.projectionMatrix = MatrixFactories.createProjection(fov, aspectRatio, nearPlane, farPlane);
            this.projectionChanged = false;
        }
        return this.projectionMatrix;
    }
}