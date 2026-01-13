package com.secretsofgreenery.render_engine;
import com.secretsofgreenery.math.*;

public class GraphicConveyor {
    public static Matrix4f assembleModelViewProjection(Matrix4f modelMatrix, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        return new Matrix4f(projectionMatrix.getData())
                .multiply(viewMatrix)
                .multiply(modelMatrix);
    }
}
