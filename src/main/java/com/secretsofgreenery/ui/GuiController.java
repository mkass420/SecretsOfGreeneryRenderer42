package com.secretsofgreenery.ui;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector3f;
import com.secretsofgreenery.model.Model;
import com.secretsofgreenery.model.Normals;
import com.secretsofgreenery.model.Polygon;
import com.secretsofgreenery.model.Triangulation;
import com.secretsofgreenery.objreader.ObjReader;
import com.secretsofgreenery.render_engine.Camera;
import com.secretsofgreenery.render_engine.RenderEngine;
import com.secretsofgreenery.render_engine.RenderEngine.RenderSettings;
import com.secretsofgreenery.render_engine.Texture;
import com.secretsofgreenery.render_engine.Light;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isDragging = false;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    // Updated to use ModelWrapper instead of SceneObject
    @FXML private ListView<ModelWrapper> modelsList;
    // Updated to use Camera directly (since Camera now has a name field and toString)
    @FXML private ListView<Camera> camerasList;

    // Spinners for Transformation
    @FXML private Spinner<Double> spTranslateX, spTranslateY, spTranslateZ;
    @FXML private Spinner<Double> spRotateX, spRotateY, spRotateZ;
    @FXML private Spinner<Double> spScaleX, spScaleY, spScaleZ;

    @FXML private TextField tfCamPosX, tfCamPosY, tfCamPosZ;
    @FXML private TextField tfCamTargetX, tfCamTargetY, tfCamTargetZ;

    @FXML private TextField tfDeleteIndex;
    @FXML private CheckBox cbGrid;
    @FXML private ToggleButton tbTheme;

    // Scene and Data
    private Scene scene;
    private ObservableList<ModelWrapper> observableModels = FXCollections.observableArrayList();
    private ObservableList<Camera> observableCameras = FXCollections.observableArrayList();

    private Timeline timeline;

    @FXML
    private void initialize() {
        // Initialize Scene
        scene = new Scene();
        if (scene.getRenderSettings() == null) {
            scene.setRenderSettings(new RenderSettings());
        }

        // Bind Cameras
        observableCameras.addAll(scene.getCameras());
        camerasList.setItems(observableCameras);

        // Select the default camera
        if (scene.getCurrentCamera() != null) {
            camerasList.getSelectionModel().select(scene.getCurrentCamera());
        } else if (!observableCameras.isEmpty()) {
            scene.setCurrentCamera(observableCameras.get(0));
            camerasList.getSelectionModel().selectFirst();
        }

        camerasList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                scene.setCurrentCamera(newV);
                updateCameraUI(newV);
            }
        });

        // Bind Models
        modelsList.setItems(observableModels);
        modelsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updateTransformUI(newV));

        // Layout Listeners
        anchorPane.prefWidthProperty().addListener((ov, oldV, newV) -> canvas.setWidth(newV.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldV, newV) -> canvas.setHeight(newV.doubleValue()));

        setupMouseHandlers();

        // FIX: Allow canvas to receive focus so arrow keys work for camera
        canvas.setFocusTraversable(true);
        canvas.setOnMouseClicked(e -> canvas.requestFocus());

        setupSpinners();

        tfCamPosX.setEditable(false); tfCamPosY.setEditable(false); tfCamPosZ.setEditable(false);
        tfCamTargetX.setEditable(false); tfCamTargetY.setEditable(false); tfCamTargetZ.setEditable(false);

        setTheme(false);
        updateCameraUI(scene.getCurrentCamera());

        // Render Loop
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(30), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

            Camera cam = scene.getCurrentCamera();
            if (cam == null) return;

            cam.setAspectRatio((float) (width / height));


            RenderEngine.render(
                    canvas.getGraphicsContext2D(),
                    scene,
                    (int) width,
                    (int) height
            );
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

                if (scene.getCurrentCamera() != null) {
                    scene.getCurrentCamera().processMouseDrag(deltaX, deltaY);
                    updateCameraUI(scene.getCurrentCamera());
                }

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
            if (scene.getCurrentCamera() != null) {
                scene.getCurrentCamera().processMouseScroll((float)event.getDeltaY());
                updateCameraUI(scene.getCurrentCamera());
            }
        });
    }

    private void setupSpinners() {
        setupSpinner(spTranslateX, 0.0, 0.5);
        setupSpinner(spTranslateY, 0.0, 0.5);
        setupSpinner(spTranslateZ, 0.0, 0.5);

        setupSpinner(spRotateX, 0.0, 5.0);
        setupSpinner(spRotateY, 0.0, 5.0);
        setupSpinner(spRotateZ, 0.0, 5.0);

        setupSpinner(spScaleX, 1.0, 0.1);
        setupSpinner(spScaleY, 1.0, 0.1);
        setupSpinner(spScaleZ, 1.0, 0.1);
    }

    private void setupSpinner(Spinner<Double> spinner, double initValue, double step) {
        SpinnerValueFactory.DoubleSpinnerValueFactory factory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(-10000.0, 10000.0, initValue, step);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);

        StringConverter<Double> converter = factory.getConverter();
        spinner.getEditor().setOnAction(e -> {
            try {
                String text = spinner.getEditor().getText();
                Double value = converter.fromString(text);
                factory.setValue(value);
                onApplyTransform(); // Auto-apply on Enter
            } catch (Exception ex) {
                spinner.getEditor().setText(converter.toString(factory.getValue()));
            }
        });
    }

    // --- Camera Actions ---

    @FXML
    public void handleCameraForward(){
        if(scene.getCurrentCamera() != null) {
            scene.getCurrentCamera().handleCameraForward(new ActionEvent(), TRANSLATION);
            updateCameraUI(scene.getCurrentCamera());
        }
    }

    @FXML
    public void handleCameraBackward(){
        if(scene.getCurrentCamera() != null){
            scene.getCurrentCamera().handleCameraBackward(new ActionEvent(), TRANSLATION);
            updateCameraUI(scene.getCurrentCamera());
        }
    }

    @FXML
    public void handleCameraLeft(){
        if(scene.getCurrentCamera() != null){
            scene.getCurrentCamera().handleCameraLeft(new ActionEvent(), TRANSLATION);
            updateCameraUI(scene.getCurrentCamera());
        }
    }

    @FXML
    public void handleCameraRight(){
        if(scene.getCurrentCamera() != null){
            scene.getCurrentCamera().handleCameraRight(new ActionEvent(), TRANSLATION);
            updateCameraUI(scene.getCurrentCamera());
        }
    }

    @FXML
    public void handleCameraUp(){
        if(scene.getCurrentCamera() != null){
            scene.getCurrentCamera().handleCameraUp(new ActionEvent(), TRANSLATION);
            updateCameraUI(scene.getCurrentCamera());
        }
    }

    @FXML
    public void handleCameraDown(){
        if(scene.getCurrentCamera() != null){
            scene.getCurrentCamera().handleCameraDown(new ActionEvent(), TRANSLATION);
            updateCameraUI(scene.getCurrentCamera());
        }
    }

    @FXML
    public void handleCameraReset() {
        if(scene.getCurrentCamera() != null){
            scene.getCurrentCamera().reset();
            updateCameraUI(scene.getCurrentCamera());
        }
    }

    private void updateCameraUI(Camera camera) {
        if(camera == null) return;
        Vector3f p = camera.getPosition();
        Vector3f t = camera.getTarget();
        tfCamPosX.setText(String.format("%.2f", p.getX()));
        tfCamPosY.setText(String.format("%.2f", p.getY()));
        tfCamPosZ.setText(String.format("%.2f", p.getZ()));
        tfCamTargetX.setText(String.format("%.2f", t.getX()));
        tfCamTargetY.setText(String.format("%.2f", t.getY()));
        tfCamTargetZ.setText(String.format("%.2f", t.getZ()));
    }

    // --- Transforms ---

    @FXML
    private void onApplyTransform() {
        ModelWrapper selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            float tx = spTranslateX.getValue().floatValue();
            float ty = spTranslateY.getValue().floatValue();
            float tz = spTranslateZ.getValue().floatValue();
            selected.setPosition(new Vector3f(tx, ty, tz));

            float rx = spRotateX.getValue().floatValue();
            float ry = spRotateY.getValue().floatValue();
            float rz = spRotateZ.getValue().floatValue();
            selected.setRotation(new Vector3f(rx, ry, rz));

            float sx = spScaleX.getValue().floatValue();
            float sy = spScaleY.getValue().floatValue();
            float sz = spScaleZ.getValue().floatValue();
            selected.setScale(new Vector3f(sx, sy, sz));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Transform");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void updateTransformUI(ModelWrapper obj) {
        if (obj == null) return;
        Vector3f t = obj.getPosition();
        Vector3f r = obj.getRotation(); // Returns degrees
        Vector3f s = obj.getScale();

        if (t != null) {
            spTranslateX.getValueFactory().setValue((double)t.getX());
            spTranslateY.getValueFactory().setValue((double)t.getY());
            spTranslateZ.getValueFactory().setValue((double)t.getZ());
        }
        if (r != null) {
            spRotateX.getValueFactory().setValue((double)r.getX());
            spRotateY.getValueFactory().setValue((double)r.getY());
            spRotateZ.getValueFactory().setValue((double)r.getZ());
        }
        if (s != null) {
            spScaleX.getValueFactory().setValue((double)s.getX());
            spScaleY.getValueFactory().setValue((double)s.getY());
            spScaleZ.getValueFactory().setValue((double)s.getZ());
        }
    }

    // --- Standard Actions ---

    @FXML
    private void onLoadModel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file == null) return;

        try {
            String content = Files.readString(file.toPath());
            Model mesh = ObjReader.read(content);
            Triangulation.triangulate(mesh);
            Normals.recalculateVertexNormals(mesh);

            ModelWrapper wrapper = new ModelWrapper(file.getName(), mesh);
            scene.addObject(wrapper);
            observableModels.add(wrapper);
            modelsList.getSelectionModel().select(wrapper);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error loading model: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void onRemoveModel() {
        ModelWrapper selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            scene.getObjects().remove(selected);
            observableModels.remove(selected);
        }
    }

    @FXML
    private void onLoadTexture() {
        ModelWrapper selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            selected.setTexture(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void onRemoveTexture() {
        ModelWrapper selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected != null) selected.setTexture((Texture) null);
    }

    @FXML
    private void onAddCamera() {
        Camera newCam = new Camera(
                new Vector3f(0, 0, 10),
                new Vector3f(0, 0, 0),
                1.0F,
                1,
                0.01F,
                100,
                new Vector3f(0, 0, 0),
                "Camera " + (scene.getCameras().size() + 1)
        );
        scene.addCamera(newCam);
        observableCameras.add(newCam);
    }

    @FXML
    private void onDeleteVertex() {
        ModelWrapper wrapper = modelsList.getSelectionModel().getSelectedItem();
        if (wrapper == null) return;
        try {
            int idx = Integer.parseInt(tfDeleteIndex.getText());
            if (idx >= 0 && idx < wrapper.getOriginalModel().getVertices().size()) {
                wrapper.getOriginalModel().removeVertex(idx);
            }
        } catch (Exception e) {}
    }

    @FXML
    private void onDeletePolygon() {
        ModelWrapper wrapper = modelsList.getSelectionModel().getSelectedItem();
        if (wrapper == null) return;
        try {
            int idx = Integer.parseInt(tfDeleteIndex.getText());
            if (idx >= 0 && idx < wrapper.getOriginalModel().getPolygons().size()) {
                Polygon p = wrapper.getOriginalModel().getPolygons().get(idx);
                wrapper.getOriginalModel().removePolygon(p, false);
            }
        } catch (Exception e) {}
    }

    @FXML
    private void onToggleTheme() {
        setTheme(tbTheme.isSelected());
    }

    private void setTheme(boolean dark) {
        if (dark) {
            anchorPane.setStyle("-fx-base: #333333; -fx-control-inner-background: #444444; -fx-text-fill: white; -fx-background-color: #333333;");
            tbTheme.setText("Light Mode");
        } else {
            anchorPane.setStyle("");
            tbTheme.setText("Dark Mode");
        }
    }
}
