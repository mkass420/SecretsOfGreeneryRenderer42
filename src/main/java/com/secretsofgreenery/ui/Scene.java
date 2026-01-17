package com.secretsofgreenery.ui;

import com.secretsofgreenery.math.Vector3f;
import com.secretsofgreenery.render_engine.Camera;
import com.secretsofgreenery.render_engine.RenderEngine;
import com.secretsofgreenery.render_engine.Light;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<ModelWrapper> objects;
    private List<Light> lights;
    private  List<Camera> cameras;
    private Camera currentCamera;
    private RenderEngine.RenderSettings renderSettings;

    public Scene() {
        this.objects = new ArrayList<>();
        this.lights = new ArrayList<>();
        this.cameras = new ArrayList<>();
        this.cameras.add(new Camera(
                new Vector3f(0, 5, 10),
                new Vector3f(0, 0, 0),
                1.0F,
                1,
                0.01F,
                100,
                new Vector3f(0, 0, 0),
                "mainCam"
                )
        );
        this.currentCamera = this.cameras.get(0);
        }

    // Getters

    public List<ModelWrapper> getObjects() {
        return objects;
    }

    public List<Light> getLights() {
        return lights;
    }

    public List<Camera> getCameras() {
        return  cameras;
    }

    public Camera getCurrentCamera() {
        return currentCamera;
    }

    public RenderEngine.RenderSettings getRenderSettings() {
        return renderSettings;
    }


    // Setters

    public void setObjects(List<ModelWrapper> objects) {
        this.objects = objects;
    }

    public void setLights(List<Light> lights) {
        this.lights = lights;
    }

    public  void setCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }

    public void setCurrentCamera(Camera currentCamera) {
        this.currentCamera = currentCamera;
    }

    public void setRenderSettings(RenderEngine.RenderSettings renderSettings) {
        this.renderSettings = renderSettings;
    }

    // Helper methods
    public void addObject(ModelWrapper object) {
        this.objects.add(object);
    }

    public void addLight(Light light) {
        this.lights.add(light);
    }

    public  void  addCamera(Camera camera) {
        this.cameras.add(camera);
    }

    // Wrappers

    public static class CameraWrapper {
        String name;
        Camera camera;
        public CameraWrapper(String name, Camera camera) {
            this.name = name;
            this.camera = camera;
        }
    }

}
