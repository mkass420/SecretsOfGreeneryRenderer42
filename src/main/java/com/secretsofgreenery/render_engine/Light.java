package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.Vector3f;

public class Light {
    private Vector3f position;
    private Vector3f color;
    private float intensity; // Множитель интенсивности для простой настройки яркости

    // Коэффициенты затухания (для формулы 1 / (Kc + Kl*d + Kq*d^2))
    // https://wiki.ogre3d.org/-Point+Light+Attenuation#:~:text=A%20point%20light's%20attenuation%20defines,Distance%20+%20Quadratic%20*%20Distance%5E2
//    private float constantAttenuation = 1.0f;
//    private float linearAttenuation = 0.045f;
//    private float quadraticAttenuation = 0.0075f;
    private float constantAttenuation = 1.0f;
    private float linearAttenuation = 0f;
    private float quadraticAttenuation = 0f;
    private  int attenuationDistance;

    public Light(Vector3f position) {
        this.position = position;
        this.color = new Vector3f(1, 1, 1); // Белый по умолчанию
        this.intensity = 1.0f;
    }

    public Light(Vector3f position, Vector3f color, float intensity) {
        this.position = position;
        this.color = color;
        this.intensity = intensity;
    }


    // Getters

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getIntensity() {
        return intensity;
    }

    public float getConstantAttenuation() { return constantAttenuation; }
    public float getLinearAttenuation() { return linearAttenuation; }
    public float getQuadraticAttenuation() { return quadraticAttenuation; }

    public float getAttenuationCoefficient(float distance){
        return 1.0f / (constantAttenuation + linearAttenuation * distance + quadraticAttenuation * distance * distance);
    }

    public  int getAttenuationDistance() {
        return  attenuationDistance;
    }

    // Setters

    public  void setAttenuationDistance(int attenuationDistance) {
        this.attenuationDistance = attenuationDistance;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public void setAttenuation(float constant, float linear, float quadratic) {
        this.constantAttenuation = constant;
        this.linearAttenuation = linear;
        this.quadraticAttenuation = quadratic;
    }

}