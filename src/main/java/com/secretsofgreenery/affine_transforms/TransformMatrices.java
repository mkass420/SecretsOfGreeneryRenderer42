package com.secretsofgreenery.affine_transforms;

import com.secretsofgreenery.math.*;

public class TransformMatrices{
    public static Matrix4f scale(float x, float y, float z){
        float[][] scale_matrix = {{x, 0, 0, 0},
                                   {0, y, 0, 0},
                                   {0, 0, z, 0},
                                   {0, 0, 0, 1}};
        return new Matrix4f(scale_matrix);
    }

    public static Matrix4f translate(float x, float y, float z){
        float[][] translate_matrix = {{1, 0, 0, x},
                                       {0, 1, 0, y},
                                       {0, 0, 1, z},
                                       {0, 0, 0, 1}};
        return new Matrix4f(translate_matrix);
    }

    public static Matrix4f rotateX(float angleRadians){
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);
        float[][] rotate_matrix = {{1,  0, 0, 0},
                                    {0,  c, s, 0},
                                    {0, -s, c, 0},
                                    {0,  0, 0, 1}};
        return new Matrix4f(rotate_matrix);
    }

    public static Matrix4f rotateY(float angleRadians){
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);
        float[][] rotate_matrix = {{ c, 0, s, 0},
                                    { 0, 1, 0, 0},
                                    {-s, 0, c, 0},
                                    { 0, 0, 0, 1}};
        return new Matrix4f(rotate_matrix);
    }

    public static Matrix4f rotateZ(float angleRadians){
        float c = (float) Math.cos(angleRadians);
        float s = (float) Math.sin(angleRadians);
        float[][] rotate_matrix = {{ c, s, 0, 0},
                                    {-s, c, 0, 0},
                                    { 0, 0, 1, 0},
                                    { 0, 0, 0, 1}};
        return new Matrix4f(rotate_matrix);
    }
}
