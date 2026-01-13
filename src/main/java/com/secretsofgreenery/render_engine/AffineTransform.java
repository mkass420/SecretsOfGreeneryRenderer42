package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.Matrix4f;

public class AffineTransform{
    private Matrix4f matrix;

    public AffineTransform() {
        this(Matrix4f.identity());
    }

    public AffineTransform(Matrix4f matrix) {
        this.matrix = new Matrix4f(matrix.getData());
    }

    public AffineTransform scale(float x, float y, float z){
        this.matrix = this.matrix.multiply(MatrixFactories.createScale(x, y, z));
        return this;
    }

    public AffineTransform translate(float x, float y, float z){
        this.matrix = this.matrix.multiply(MatrixFactories.createTranslation(x, y, z));
        return this;
    }
    
    public AffineTransform rotateX(float angleRadians){
        this.matrix = this.matrix.multiply(MatrixFactories.createRotationX(angleRadians));
        return this;
    }
    
    public AffineTransform rotateY(float angleRadians){
        this.matrix = this.matrix.multiply(MatrixFactories.createRotationY(angleRadians));
        return this;
    }
    
    public AffineTransform rotateZ(float angleRadians){
        this.matrix = this.matrix.multiply(MatrixFactories.createRotationZ(angleRadians));
        return this;
    }

    public Matrix4f getMatrix(){
        return this.matrix;
    }
}
