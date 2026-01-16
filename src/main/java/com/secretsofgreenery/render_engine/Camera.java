package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector3f;
import javafx.event.ActionEvent;

public class Camera {
    Sensitivity sen = new Sensitivity();
    private float mouseSensitivity = sen.mouseSensitivity;
    private float zoomSensitivity = sen.zoomSensitivity;

    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;
    private Vector3f pointOfRotation;

    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private float distance;

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
            final float farPlane,
            final Vector3f pointOfRotation
    ) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        this.pointOfRotation = pointOfRotation;

        updateCameraState();
    }

    private void updateCameraState() {
        this.distance = position.subtract(pointOfRotation).length();

        Vector3f direction = pointOfRotation.subtract(position).normalize();
        this.yaw = (float)Math.toDegrees(Math.atan2(direction.getZ(), direction.getX()));
        this.pitch = (float)Math.toDegrees(Math.asin(direction.getY()));
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

    public void handleCameraForward(ActionEvent actionEvent, float TRANSLATION) {
        Vector3f direction = target.subtract(position).normalize();
        Vector3f translation = direction.multiply(TRANSLATION);
        this.movePosition(translation);
        this.moveTarget(translation);
    }

    public void handleCameraBackward(ActionEvent actionEvent, float TRANSLATION) {
        Vector3f direction = target.subtract(position).normalize();
        Vector3f translation = direction.multiply(-TRANSLATION);
        this.movePosition(translation);
        this.moveTarget(translation);
    }

    public void handleCameraLeft(ActionEvent actionEvent, float TRANSLATION) {
        Vector3f direction = target.subtract(position).normalize();
        Vector3f right = direction.cross(new Vector3f(0, 1, 0)).normalize();
        Vector3f translation = right.multiply(TRANSLATION);
        this.movePosition(translation);
        this.moveTarget(translation);
    }

    public void handleCameraRight(ActionEvent actionEvent, float TRANSLATION) {
        Vector3f direction = target.subtract(position).normalize();
        Vector3f right = direction.cross(new Vector3f(0, 1, 0)).normalize();
        Vector3f translation = right.multiply(-TRANSLATION);
        this.movePosition(translation);
        this.moveTarget(translation);
    }

    public void handleCameraUp(ActionEvent actionEvent, float TRANSLATION) {
        this.movePosition(new Vector3f(0, TRANSLATION, 0));
        this.moveTarget(new Vector3f(0, TRANSLATION, 0));
    }

    public void handleCameraDown(ActionEvent actionEvent, float TRANSLATION) {
        this.movePosition(new Vector3f(0, -TRANSLATION, 0));
        this.moveTarget(new Vector3f(0, -TRANSLATION, 0));
    }

    public void rotate(float deltaYaw, float deltaPitch){
        yaw += deltaYaw;
        pitch += deltaPitch;

        pitch = Math.max(-85.0f, Math.min(85.0f, pitch)); // ограничение чтоб камера не перевернулась

        updateCameraPosition();
    }

    private void updateCameraPosition() {
        float yawRad = (float)Math.toRadians(yaw);
        float pitchRad = (float)Math.toRadians(pitch);

        float horizontalDistance = distance * (float)Math.cos(pitchRad);
        float verticalDistance = distance * (float)Math.sin(pitchRad);

        float posX = pointOfRotation.getX() + horizontalDistance * (float)Math.cos(yawRad);
        float posY = pointOfRotation.getY() + verticalDistance;
        float posZ = pointOfRotation.getZ() + horizontalDistance * (float)Math.sin(yawRad);

        this.position = new Vector3f(posX, posY, posZ);
        this.target = pointOfRotation;
        this.viewChanged = true;
    }

    public void zoom(float delta) {
        distance = Math.max(1.0f, Math.min(200.0f, distance - delta * zoomSensitivity)); //там тоже ограничение есть

        updateCameraPosition();
    }

    public void processMouseDrag(float deltaX, float deltaY) {
        rotate(deltaX * mouseSensitivity * -1, deltaY * mouseSensitivity);
    }

    public void processMouseScroll(float deltaY) {
        zoom(deltaY);
    }

    public void setRotationPoint(Vector3f newRotationPoint) {
        this.pointOfRotation = newRotationPoint;
        updateCameraPosition();
    }

    public void moveRotationPoint(Vector3f translation) {
        this.pointOfRotation = this.pointOfRotation.add(translation);
        this.target = this.target.add(translation);
        updateCameraPosition();
    }

    public void reset() {
        this.position = new Vector3f(0, 0, 100);
        this.target = new Vector3f(0, 0, 0);
        this.pointOfRotation = new Vector3f(0, 0, 0);
        updateCameraState();
        updateCameraPosition();
    }
}