package com.secretsofgreenery.math;

public class Baricentric {
    public static Vector3f solveEquation(Vector2f vtA, Vector2f vtB, Vector2f vtC, Vector2f texture){
        float xA = vtA.getX();
        float yA = vtA.getY();
        float xB = vtB.getX();
        float yB = vtB.getY();
        float xC = vtC.getX();
        float yC = vtC.getY();

        float x = texture.getX();
        float y = texture.getY();

        float[][] A = {{xA, xB, xC}, {yA, yB, yC}, {1, 1, 1}};
        float[] B = {x, y};
        float[] roots = LinearAlgebra.solveSystem(A, B);

        Vector3f result = new Vector3f(roots[0], roots[1], roots[2]);
        return result;
    }
}
