package com.secretsofgreenery.math;

public class Quaternion extends Vector4f {
    public Quaternion(float x, float y, float z, float w){
        super(x, y, z, w);
    }
    public Quaternion(){
        super(0, 0, 0, 1);
    }

    public Quaternion(Vector4f v){
        super(v.getX(), v.getY(), v.getZ(), v.getW());
    }

    public Quaternion multiply(Quaternion r){
        float nw = w * r.w - x * r.x - y * r.y - z * r.z;
        float nx = x * r.w + w * r.x + y * r.z - z * r.y;
        float ny = y * r.w + w * r.y + z * r.x - x * r.z;
        float nz = z * r.w + w * r.z + x * r.y - y * r.x;
        return new Quaternion(nx, ny, nz, nw);
    }

    public Quaternion normalize(){
        return new Quaternion(super.normalize());
    }

    public static Quaternion fromAxisAngle(Vector3f axis, float angleRadians) {
        float sinHalfAngle = (float) Math.sin(angleRadians / 2);
        float cosHalfAngle = (float) Math.cos(angleRadians / 2);

        Vector3f normAxis = axis.normalize();

        return new Quaternion(
                normAxis.getX() * sinHalfAngle,
                normAxis.getY() * sinHalfAngle,
                normAxis.getZ() * sinHalfAngle,
                cosHalfAngle
        );
    }

    public static Quaternion fromEulerAngles(float x, float y, float z) {
        float cX = (float) Math.cos(-x / 2);
        float sX = (float) Math.sin(-x / 2);
        float cY = (float) Math.cos(y / 2);
        float sY = (float) Math.sin(y / 2);
        float cZ = (float) Math.cos(-z / 2);
        float sZ = (float) Math.sin(-z / 2);

        return new Quaternion(
                sX * cY * cZ + cX * sY * sZ,
                cX * sY * cZ - sX * cY * sZ,
                cX * cY * sZ + sX * sY * cZ,
                cX * cY * cZ - sX * sY * sZ
        );
    }
}
