package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Quaternion;
import com.secretsofgreenery.math.Vector3f;

public class AffineTransform{
    private Matrix4f matrix;
    private Vector3f translation;
    private Vector3f rotation;
    private Quaternion rotationQuaternion;
    private Vector3f scaling;
    private Matrix4f translationMatrix = Matrix4f.identity();
    private Matrix4f rotationMatrix    = Matrix4f.identity();
    private Matrix4f scalingMatrix     = Matrix4f.identity();

    public AffineTransform() {
        this(Matrix4f.identity());
    }

    public AffineTransform(Matrix4f matrix) {
        this.matrix = new Matrix4f(matrix.getData());
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Quaternion getRotationQuaternion(){
        return rotationQuaternion;
    }

    public Vector3f getScaling() {
        return scaling;
    }

    public Matrix4f getMatrix(){
        return this.matrix;
    }

    public AffineTransform setScaling(Vector3f scaling) {
        if(scaling.getX() < 1e-5 || scaling.getY() < 1e-5 || scaling.getZ() < 1e-5){
            throw new IllegalArgumentException("Масштабирование не может быть меньше или равно нулю");
        }
        this.scaling = scaling;
        scalingMatrix = MatrixFactories.createScale(
                scaling.getX(),
                scaling.getY(),
                scaling.getZ());
        return this;
    }

    public AffineTransform setRotation(Vector3f rotation) {
        this.rotation = rotation;
//        rotationMatrix = MatrixFactories.createRotationX(rotation.getX())
//                .multiply(MatrixFactories.createRotationY(rotation.getY()))
//                .multiply(MatrixFactories.createRotationZ(rotation.getZ()));
        this.rotationQuaternion = Quaternion.fromEulerAngles(
                rotation.getX(),
                rotation.getY(),
                rotation.getZ()
        );

        rotationMatrix = MatrixFactories.createRotationQuaternion(rotationQuaternion);
        return this;
    }

    public AffineTransform setTranslation(Vector3f translation) {
        this.translation = translation;
        translationMatrix = MatrixFactories.createTranslation(
                translation.getX(),
                translation.getY(),
                translation.getZ());
        return this;
    }

    public Matrix4f apply(){
        this.matrix = Matrix4f.identity()
                .multiply(translationMatrix)
                .multiply(rotationMatrix)
                .multiply(scalingMatrix);
        return this.matrix;
    }
}
