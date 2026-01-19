package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector3f;

public class MatrixFactories {
    public static Matrix4f createScale(float x, float y, float z){
        float[][] scale_matrix = {{x, 0, 0, 0},
                {0, y, 0, 0},
                {0, 0, z, 0},
                {0, 0, 0, 1}};
        return new Matrix4f(scale_matrix);
    }

    public static Matrix4f createTranslation(float x, float y, float z){
        float[][] translate_matrix = {{1, 0, 0, x},
                {0, 1, 0, y},
                {0, 0, 1, z},
                {0, 0, 0, 1}};
        return new Matrix4f(translate_matrix);
    }

    public static Matrix4f createRotationX(float angleRadians){
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);
        float[][] rotate_matrix = {{1,  0, 0, 0},
                {0,  c, s, 0},
                {0, -s, c, 0},
                {0,  0, 0, 1}};
        return new Matrix4f(rotate_matrix);
    }

    public static Matrix4f createRotationY(float angleRadians){
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);
        float[][] rotate_matrix = {{ c, 0, s, 0},
                { 0, 1, 0, 0},
                {-s, 0, c, 0},
                { 0, 0, 0, 1}};
        return new Matrix4f(rotate_matrix);
    }

    public static Matrix4f createRotationZ(float angleRadians){
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);
        float[][] rotate_matrix = {{ c, s, 0, 0},
                {-s, c, 0, 0},
                { 0, 0, 1, 0},
                { 0, 0, 0, 1}};
        return new Matrix4f(rotate_matrix);
    }

    public static Matrix4f createView(Vector3f eye, Vector3f target) {
        return createView(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f createView(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultZ = eye.subtract(target);
        Vector3f resultX = up.cross(resultZ);
        Vector3f resultY = resultZ.cross(resultX);

        resultX = resultX.normalize();
        resultY = resultY.normalize();
        resultZ = resultZ.normalize();

        float[][] view_matrix = new float[][]{
                {resultX.getX(), resultX.getY(), resultX.getZ(), -resultX.dot(eye)},
                {resultY.getX(), resultY.getY(), resultY.getZ(), -resultY.dot(eye)},
                {resultZ.getX(), resultZ.getY(), resultZ.getZ(), -resultZ.dot(eye)},
                {0, 0, 0, 1}
        };
        return new Matrix4f(view_matrix);
    }

    public static Matrix4f createProjection(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));
        float[][] projection_matrix = new float[4][4];

        projection_matrix[0][0] = tangentMinusOnDegree / aspectRatio;
        projection_matrix[1][1] = tangentMinusOnDegree;
        projection_matrix[2][2] = -(farPlane + nearPlane) / (farPlane - nearPlane);
        projection_matrix[2][3] = 2 * (nearPlane * farPlane) / (nearPlane - farPlane);
        projection_matrix[3][2] = -1.0F;

        return new Matrix4f(projection_matrix);
    }
}
