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

public class GuiController {
    //ЭТО БЛЯТЬ ЗНАЧИТ ВОТ ТАКАЯ ХУЙНЯ ВНУТРИ КЛАССА
    private CameraWrapper currentCameraWrapper;

    final private float TRANSLATION = 0.5F;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isDragging = false;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML private ListView<SceneObject> modelsList;
    @FXML private ListView<CameraWrapper> camerasList;

    // Spinners for Transformation
    @FXML private Spinner<Double> spTranslateX, spTranslateY, spTranslateZ;
    @FXML private Spinner<Double> spRotateX, spRotateY, spRotateZ;
    @FXML private Spinner<Double> spScaleX, spScaleY, spScaleZ;

    @FXML private TextField tfCamPosX, tfCamPosY, tfCamPosZ;
    @FXML private TextField tfCamTargetX, tfCamTargetY, tfCamTargetZ;

    @FXML private TextField tfDeleteIndex;
    @FXML private CheckBox cbGrid;
    @FXML private ToggleButton tbTheme;

    private ObservableList<SceneObject> sceneObjects = FXCollections.observableArrayList();
    private ObservableList<CameraWrapper> cameras = FXCollections.observableArrayList();
    private RenderSettings settings = new RenderSettings();

    private Timeline timeline;

    public static class CameraWrapper {
        String name;
        Camera camera;
        public CameraWrapper(String name, Camera camera) {
            this.name = name;
            this.camera = camera;
        }
        @Override public String toString() { return name; }
    }

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldV, newV) -> canvas.setWidth(newV.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldV, newV) -> canvas.setHeight(newV.doubleValue()));

        setupMouseHandlers();

        // FIX: Allow canvas to receive focus so arrow keys work for camera instead of UI navigation
        canvas.setFocusTraversable(true);
        canvas.setOnMouseClicked(e -> canvas.requestFocus());

        setupSpinners();

        tfCamPosX.setEditable(false); tfCamPosY.setEditable(false); tfCamPosZ.setEditable(false);
        tfCamTargetX.setEditable(false); tfCamTargetY.setEditable(false); tfCamTargetZ.setEditable(false);

        Camera mainCam = new Camera(new Vector3f(0, 5, 10), new Vector3f(0, 0, 0), 1.0F, 1, 0.01F, 100, new Vector3f(0, 0, 0));
        cameras.add(new CameraWrapper("Main Camera", mainCam));
        camerasList.setItems(cameras);

        camerasList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                this.currentCameraWrapper = newV;
                updateCameraUI(newV);
            }
        });

        camerasList.getSelectionModel().selectFirst();

        modelsList.setItems(sceneObjects);
        modelsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updateTransformUI(newV));

        setTheme(false);

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(30), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

            if (currentCameraWrapper == null) return;

            currentCameraWrapper.camera.setAspectRatio((float) (width / height));

            Model compositeModel = new Model();

            Image textureToUse = null;
            for (SceneObject obj : sceneObjects) {
                mergeModels(compositeModel, obj.getOriginalModel(), obj.getModelMatrix());
                if (obj.getTexture() != null && textureToUse == null) {
                    textureToUse = obj.getTexture();
                }
            }

            RenderEngine.render(canvas.getGraphicsContext2D(), currentCameraWrapper.camera, compositeModel, (int) width, (int) height, textureToUse, settings);
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

                currentCameraWrapper.camera.processMouseDrag(deltaX, deltaY);

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
            currentCameraWrapper.camera.processMouseScroll((float)event.getDeltaY());
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

    @FXML
    public void handleCameraForward(){
        if(currentCameraWrapper != null) {
            currentCameraWrapper.camera.handleCameraForward(new ActionEvent(), TRANSLATION);
            updateCameraUI(currentCameraWrapper);
        }
    }

    @FXML
    public void handleCameraBackward(){
        if(currentCameraWrapper != null){
            currentCameraWrapper.camera.handleCameraBackward(new ActionEvent(), TRANSLATION);
            updateCameraUI(currentCameraWrapper);
        }
    }

    @FXML
    public void handleCameraLeft(){
        if(currentCameraWrapper != null){
            currentCameraWrapper.camera.handleCameraLeft(new ActionEvent(), TRANSLATION);
            updateCameraUI(currentCameraWrapper);
        }
    }

    @FXML
    public void handleCameraRight(){
        if(currentCameraWrapper != null){
            currentCameraWrapper.camera.handleCameraRight(new ActionEvent(), TRANSLATION);
            updateCameraUI(currentCameraWrapper);
        }
    }

    @FXML
    public void handleCameraUp(){
        if(currentCameraWrapper != null){
            currentCameraWrapper.camera.handleCameraUp(new ActionEvent(), TRANSLATION);
            updateCameraUI(currentCameraWrapper);
        }
    }

    @FXML
    public void handleCameraDown(){
        if(currentCameraWrapper != null){
            currentCameraWrapper.camera.handleCameraDown(new ActionEvent(), TRANSLATION);
            updateCameraUI(currentCameraWrapper);
        }
    }

    @FXML
    public void handleCameraReset() {
        if(currentCameraWrapper != null){
            currentCameraWrapper.camera.reset();
            updateCameraUI(currentCameraWrapper);
        }
    }

    private void updateCameraUI(CameraWrapper wrapper) {
        if(wrapper == null) return;
        Vector3f p = wrapper.camera.getPosition();
        Vector3f t = wrapper.camera.getTarget();
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
        SceneObject selected = modelsList.getSelectionModel().getSelectedItem();
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

    private void updateTransformUI(SceneObject obj) {
        if (obj == null) return;
        Vector3f t = obj.getPosition();
        Vector3f r = obj.getRotation();
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

    // --- Logic ---

    private void mergeModels(Model target, Model source, Matrix4f matrix) {
        if (source == null) return;
        int vertexOffset = target.getVertices().size();
        int textureOffset = target.getTextureVertices().size();
        int normalOffset = target.getNormals().size();

        for (Vector3f v : source.getVertices()) {
            target.getVertices().add(Matrix4f.multiplyMatrix4ByVector3(matrix, v));
        }

        target.getTextureVertices().addAll(source.getTextureVertices());
        target.getNormals().addAll(source.getNormals());

        for (Polygon p : source.getPolygons()) {
            Polygon newPoly = new Polygon();
            ArrayList<Integer> vInds = new ArrayList<>();
            for (Integer i : p.getVertexIndices()) vInds.add(i + vertexOffset);
            newPoly.setVertexIndices(vInds);

            if (p.getTextureVertexIndices() != null && !p.getTextureVertexIndices().isEmpty()) {
                ArrayList<Integer> tInds = new ArrayList<>();
                for (Integer i : p.getTextureVertexIndices()) tInds.add(i + textureOffset);
                newPoly.setTextureVertexIndices(tInds);
            }

            if (p.getNormalIndices() != null && !p.getNormalIndices().isEmpty()) {
                ArrayList<Integer> nInds = new ArrayList<>();
                for (Integer i : p.getNormalIndices()) nInds.add(i + normalOffset);
                newPoly.setNormalIndices(nInds);
            }
            target.getPolygons().add(newPoly);
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

            SceneObject obj = new SceneObject(file.getName(), mesh);
            sceneObjects.add(obj);
            modelsList.getSelectionModel().select(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRemoveModel() {
        SceneObject selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected != null) sceneObjects.remove(selected);
    }

    @FXML
    private void onLoadTexture() {
        SceneObject selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg"));
        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            selected.setTexture(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void onRemoveTexture() {
        SceneObject selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected != null) selected.setTexture(null);
    }

    @FXML
    private void onAddCamera() {
        Camera newCam = new Camera(new Vector3f(0, 0, 10), new Vector3f(0, 0, 0), 1.0F, 1, 0.01F, 100, new Vector3f(0, 0, 0));
        cameras.add(new CameraWrapper("Camera " + (cameras.size() + 1), newCam));
    }

    @FXML
    private void onDeleteVertex() {
        SceneObject obj = modelsList.getSelectionModel().getSelectedItem();
        if (obj == null) return;
        try {
            int idx = Integer.parseInt(tfDeleteIndex.getText());
            if (idx >= 0 && idx < obj.getOriginalModel().getVertices().size()) {
                obj.getOriginalModel().removeVertex(idx);
            }
        } catch (Exception e) {}
    }

    @FXML
    private void onDeletePolygon() {
        SceneObject obj = modelsList.getSelectionModel().getSelectedItem();
        if (obj == null) return;
        try {
            int idx = Integer.parseInt(tfDeleteIndex.getText());
            if (idx >= 0 && idx < obj.getOriginalModel().getPolygons().size()) {
                Polygon p = obj.getOriginalModel().getPolygons().get(idx);
                obj.getOriginalModel().removePolygon(p, false);
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
