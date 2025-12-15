package com.secretsofgreenery.affine_transforms;

import com.secretsofgreenery.math.Matrix4f;

public class AffineTransform{
    private Matrix4f matrix;

    AffineTransform(){
        matrix = Matrix4f.identity();
    }

    public AffineTransform scale(float x, float y, float z){
        this.matrix = this.matrix.multiply(TransformMatrices.scale(x, y, z));
        return this;
    }

    public AffineTransform translate(float x, float y, float z){
        this.matrix = this.matrix.multiply(TransformMatrices.translate(x, y, z));
        return this;
    }
    
    public AffineTransform rotateX(float angleRadians){
        this.matrix = this.matrix.multiply(TransformMatrices.rotateX(angleRadians));
        return this;
    }
    
    public AffineTransform rotateY(float angleRadians){
        this.matrix = this.matrix.multiply(TransformMatrices.rotateY(angleRadians));
        return this;
    }
    
    public AffineTransform rotateZ(float angleRadians){
        this.matrix = this.matrix.multiply(TransformMatrices.rotateZ(angleRadians));
        return this;
    }

    public Matrix4f getMatrix(){
        return this.matrix;
    }
}
