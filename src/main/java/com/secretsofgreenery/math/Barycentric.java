package com.secretsofgreenery.math;

public class Barycentric {
//    public static Vector3f solveEquation(Vector2f vtA, Vector2f vtB, Vector2f vtC, Vector2f texture){
//        float xA = vtA.getX();
//        float yA = vtA.getY();
//        float xB = vtB.getX();
//        float yB = vtB.getY();
//        float xC = vtC.getX();
//        float yC = vtC.getY();
//
//        float x = texture.getX();
//        float y = texture.getY();
//
//        float[][] A = {{xA, xB, xC}, {yA, yB, yC}, {1, 1, 1}};
//        float[] B = {x, y, 1};
//        float[] roots = LinearAlgebra.solveSystem(A, B);
//
//        Vector3f result = new Vector3f(roots[0], roots[1], roots[2]);
//        return result;
//    }

    // Более эффективный метод, не использующий решение системы (подставляем альфу, раскрываем скобки и группируем)
    // alpha = 1 - beta - gamma
    // xP = alpha*xA + beta*xB + gamma*xC <=> beta*(xB - xA) + gamma*(xC - xA) + 1*(xA - xP) = 0 <=> скалярное произведение перпендикулярных W(beta, gamma, 1) и U(xB - xA, xC - xA, xA - xP)
    // yP = alpha*yA + beta*yB + gamma*yC <=> beta*(yB - yA) + gamma*(yC - yA) + 1*(yA - yP) = 0 <=> скалярное произведение перпендикулярных W(beta, gamma, 1) и V(yB - yA, yC - yA, yA - yP)
    // находим k*W(k*beta, k*gamma, k) как векторное произведение U и V (перпендикулярен обоим) и нормализуем
    public static Vector3f barycentric(Point2f p, Point2f a, Point2f b, Point2f c) {
        float x1 = b.getX() - a.getX();
        float y1 = c.getX() - a.getX();
        float z1 = a.getX() - p.getX();
        float x2 = b.getY() - a.getY();
        float y2 = c.getY() - a.getY();
        float z2 = a.getY() - p.getY();

        Vector3f u = new Vector3f(x1, y1, z1).cross(new Vector3f(x2, y2, z2));

        if (Math.abs(u.getZ()) < 1) { // треугольник вырожден
            return new Vector3f(-1, 1, 1);
        }
        float beta = u.getX() / u.getZ();
        float gamma = u.getY() / u.getZ();
        float alpha = 1 - beta - gamma;

        return new Vector3f(alpha, beta, gamma);
    }
}
