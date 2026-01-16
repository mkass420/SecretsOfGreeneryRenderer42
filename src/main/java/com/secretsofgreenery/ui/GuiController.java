package com.secretsofgreenery.ui;

import com.secretsofgreenery.model.Model;
import com.secretsofgreenery.model.Normals;
import com.secretsofgreenery.model.Triangulation;
import com.secretsofgreenery.objreader.ObjReader;
import com.secretsofgreenery.render_engine.Camera;
import com.secretsofgreenery.render_engine.RenderEngine;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.secretsofgreenery.math.Vector3f;
import com.secretsofgreenery.render_engine.RenderEngine.RenderSettings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isDragging = false;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Model mesh = null;

    private Image texture = null;

    private Camera camera = new Camera(
            new Vector3f(0, 0, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100,
            new Vector3f(0, 0, 0)
    );

    private Timeline timeline;

    private RenderSettings settings = new RenderSettings();

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        setupMouseHandlers();

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (mesh != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) width, (int) height, texture, settings);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    private void setupMouseHandlers() {
        canvas.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                isDragging = true;
                lastMouseX = event.getX();
                lastMouseY = event.getY();
                canvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (isDragging) {
                double currentX = event.getX();
                double currentY = event.getY();

                float deltaX = (float)(currentX - lastMouseX);
                float deltaY = (float)(currentY - lastMouseY);

                camera.processMouseDrag(deltaX, deltaY);

                lastMouseX = currentX;
                lastMouseY = currentY;
            }
        });

        canvas.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                isDragging = false;
                canvas.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        canvas.setOnScroll((ScrollEvent event) -> {
            camera.processMouseScroll((float)event.getDeltaY());
        });
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);
            Triangulation.triangulate(mesh);
            Normals.recalculateVertexNormals(mesh);
            centerCameraOnModel(); // ставим камеру на центр загруженной модели
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void centerCameraOnModel() {
        if (mesh != null) {
            Vector3f sum = new Vector3f(0, 0, 0);
            for (Vector3f vertex : mesh.getVertices()) {
                sum = sum.add(vertex);
            }
            Vector3f center = sum.divide(mesh.getVertices().size());

            camera.setRotationPoint(center);
        }
    }

    @FXML
    private void onOpenTextureMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        fileChooser.setTitle("Load Texture");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file != null) {
            try {
                this.texture = new Image(Files.newInputStream(file.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleCameraForward(){
        camera.handleCameraForward(new ActionEvent(), TRANSLATION);
    }

    public void handleCameraBackward(){
        camera.handleCameraBackward(new ActionEvent(), TRANSLATION);
    }

    public void handleCameraLeft(){
        camera.handleCameraLeft(new ActionEvent(), TRANSLATION);
    }

    public void handleCameraRight(){
        camera.handleCameraRight(new ActionEvent(), TRANSLATION);
    }

    public void handleCameraUp(){
        camera.handleCameraUp(new ActionEvent(), TRANSLATION);
    }

    public void handleCameraDown(){
        camera.handleCameraDown(new ActionEvent(), TRANSLATION);
    }

    public void handleCameraReset() {
        camera.reset();
    }
}