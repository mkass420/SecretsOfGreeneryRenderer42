package com.secretsofgreenery.render_engine;
import com.secretsofgreenery.math.*;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        return new Matrix4f(Matrix4f.identity().getData());
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultZ = target.subtract(eye);
        Vector3f resultX = up.cross(resultZ);
        Vector3f resultY = resultZ.cross(resultX);

        resultX = resultX.normalize();
        resultY = resultY.normalize();
        resultZ = resultZ.normalize();

        float[][] matrix = new float[][]{
                {resultX.getX(), resultX.getY(), resultX.getZ(), -resultX.dot(eye)},
                {resultY.getX(), resultY.getY(), resultY.getZ(), -resultY.dot(eye)},
                {resultZ.getX(), resultZ.getY(), resultZ.getZ(), -resultZ.dot(eye)},
                {0, 0, 0, 1}
        };
        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = new Matrix4f(); // Начало с нулей
        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));
        float[][] data = result.getData();

        data[0][0] = tangentMinusOnDegree / aspectRatio;
        data[1][1] = tangentMinusOnDegree;
        data[2][2] = (farPlane + nearPlane) / (farPlane - nearPlane);
        data[2][3] = 2 * (nearPlane * farPlane) / (nearPlane - farPlane);
        data[3][2] = 1.0F;

        return result;
    }

}
