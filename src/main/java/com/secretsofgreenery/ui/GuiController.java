package com.secretsofgreenery.ui;

import com.secretsofgreenery.math.Matrix4f;
import com.secretsofgreenery.math.Vector3f;
import com.secretsofgreenery.model.Model;
import com.secretsofgreenery.model.Normals;
import com.secretsofgreenery.model.Polygon;
import com.secretsofgreenery.model.Triangulation;
import com.secretsofgreenery.objreader.ObjReader;
import com.secretsofgreenery.objwriter.ObjWriter;
import com.secretsofgreenery.render_engine.*;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;

import static com.secretsofgreenery.math.Matrix4f.multiplyMatrix4ByVector3;
import static com.secretsofgreenery.model.Normals.multiplyMatrix4ByNormal;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean isDragging = false;

    @FXML AnchorPane anchorPane;
    @FXML private Canvas canvas;
    @FXML private StackPane canvasContainer;

    @FXML private ListView<ModelWrapper> modelsList;
    @FXML private ListView<Camera> camerasList;

    // --- LIGHT TAB CONTROLS ---
    @FXML private ListView<Light> lightsList;
    @FXML private ColorPicker cpLightColor;
    @FXML private Spinner<Double> spLightIntensity;
    @FXML private Spinner<Double> spLightX, spLightY, spLightZ;
    private ObservableList<Light> observableLights = FXCollections.observableArrayList();

    // Spinners for Transformation
    @FXML private Spinner<Double> spTranslateX, spTranslateY, spTranslateZ;
    @FXML private Spinner<Double> spRotateX, spRotateY, spRotateZ;
    @FXML private Spinner<Double> spScaleX, spScaleY, spScaleZ;

    @FXML private TextField tfCamPosX, tfCamPosY, tfCamPosZ;
    @FXML private TextField tfCamTargetX, tfCamTargetY, tfCamTargetZ;

    @FXML private TextField tfDeleteIndex;

    // --- NEW CONTROLS ---
    @FXML private CheckBox cbGrid;
    @FXML private CheckBox cbWireframe;
    @FXML private CheckBox cbTextures;
    @FXML private CheckBox cbLighting;
    @FXML private CheckBox cbCameraLight;

    @FXML private Slider slMouseSens;
    @FXML private Slider slZoomSens;
    @FXML private Spinner<Double> spFov;

    @FXML private ToggleButton tbTheme;

    // Model Saving
    @FXML private CheckBox cbApplyTransformOnSave;

    // Scene and Data
    private Scene scene;
    private ObservableList<ModelWrapper> observableModels = FXCollections.observableArrayList();
    private ObservableList<Camera> observableCameras = FXCollections.observableArrayList();

    private Sensitivity guiSensitivity = new Sensitivity();
    private Timeline timeline;

    @FXML
    private void initialize() {
        scene = new Scene();
        if (scene.getRenderSettings() == null) {
            scene.setRenderSettings(new RenderSettings());
        }

        // --- Render Settings Bindings ---
        RenderSettings settings = scene.getRenderSettings();
        cbGrid.setSelected(settings.drawGrid);
        cbWireframe.setSelected(settings.drawWireframe);
        cbTextures.setSelected(settings.useTexture);
        cbLighting.setSelected(settings.useLighting);
        cbCameraLight.setSelected(settings.cameraLightSource);

        cbGrid.selectedProperty().addListener((obs, oldV, newV) -> settings.drawGrid = newV);
        cbWireframe.selectedProperty().addListener((obs, oldV, newV) -> settings.drawWireframe = newV);
        cbTextures.selectedProperty().addListener((obs, oldV, newV) -> settings.useTexture = newV);
        cbLighting.selectedProperty().addListener((obs, oldV, newV) -> settings.useLighting = newV);
        cbCameraLight.selectedProperty().addListener((obs, oldV, newV) -> settings.cameraLightSource = newV);

        // --- FOV Spinner ---
        SpinnerValueFactory.DoubleSpinnerValueFactory fovFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(10.0, 160.0, 60.0, 1.0);
        spFov.setValueFactory(fovFactory);
        spFov.setEditable(true);
        spFov.valueProperty().addListener((obs, oldV, newV) -> {
            if (scene.getCurrentCamera() != null) {
                scene.getCurrentCamera().setFov((float) Math.toRadians(newV));
            }
        });

        // --- Sensitivity ---
        slMouseSens.valueProperty().addListener((obs, oldV, newV) -> {
            guiSensitivity.mouseSensitivity = newV.floatValue();
            updateCameraSensitivity(scene.getCurrentCamera());
        });
        slZoomSens.valueProperty().addListener((obs, oldV, newV) -> {
            guiSensitivity.zoomSensitivity = newV.floatValue();
            updateCameraSensitivity(scene.getCurrentCamera());
        });

        // --- Lights Setup ---
        setupLightControls();
        lightsList.setItems(observableLights);
        lightsList.setCellFactory(param -> new ListCell<Light>() {
            @Override
            protected void updateItem(Light item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    int index = getListView().getItems().indexOf(item) + 1;
                    setText("Light Source " + index);
                }
            }
        });
        lightsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updateLightUI(newV));


        // Bind Cameras
        observableCameras.addAll(scene.getCameras());
        camerasList.setItems(observableCameras);
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
                updateCameraSensitivity(newV);
            }
        });

        // Bind Models
        modelsList.setItems(observableModels);
        modelsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updateTransformUI(newV));

        // Layout Listeners
        anchorPane.prefWidthProperty().addListener((ov, oldV, newV) -> canvas.setWidth(newV.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldV, newV) -> canvas.setHeight(newV.doubleValue()));

        setupMouseHandlers();
        canvas.setFocusTraversable(true);
        canvas.setOnMouseClicked(e -> canvas.requestFocus());

        setupSpinners();

        tfCamPosX.setEditable(false); tfCamPosY.setEditable(false); tfCamPosZ.setEditable(false);
        tfCamTargetX.setEditable(false); tfCamTargetY.setEditable(false); tfCamTargetZ.setEditable(false);

        setTheme(false);
        updateCameraUI(scene.getCurrentCamera());
        updateCameraSensitivity(scene.getCurrentCamera());

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        KeyFrame frame = new KeyFrame(Duration.millis(30), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            Camera cam = scene.getCurrentCamera();
            if (cam == null) return;
            cam.setAspectRatio((float) (width / height));
            RenderEngine.render(canvas.getGraphicsContext2D(), scene, (int) width, (int) height);
        });
        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    // --- Light Handling ---

    private void setupLightControls() {
        setupSpinner(spLightX, 0.0, 1.0);
        setupSpinner(spLightY, 0.0, 1.0);
        setupSpinner(spLightZ, 0.0, 1.0);
        setupSpinner(spLightIntensity, 1.0, 0.1);
    }

    @FXML
    private void onAddLight() {
        Light light = new Light(new Vector3f(0, 10, 0));
        scene.addLight(light);
        observableLights.add(light);
        lightsList.getSelectionModel().select(light);
    }

    @FXML
    private void onRemoveLight() {
        Light selected = lightsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            scene.getLights().remove(selected);
            observableLights.remove(selected);
        }
    }

    @FXML
    private void onApplyLightSettings() {
        Light selected = lightsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        float x = spLightX.getValue().floatValue();
        float y = spLightY.getValue().floatValue();
        float z = spLightZ.getValue().floatValue();
        selected.setPosition(new Vector3f(x, y, z));

        Color c = cpLightColor.getValue();
        selected.setColor(ColorUtils.colorToVector(c));

        float intensity = spLightIntensity.getValue().floatValue();
        selected.setIntensity(intensity);

        // Force refresh list view text if needed
        lightsList.refresh();
    }

    private void updateLightUI(Light light) {
        if (light == null) return;
        Vector3f p = light.getPosition();
        spLightX.getValueFactory().setValue((double)p.getX());
        spLightY.getValueFactory().setValue((double)p.getY());
        spLightZ.getValueFactory().setValue((double)p.getZ());

        spLightIntensity.getValueFactory().setValue((double)light.getIntensity());

        Vector3f c = light.getColor();
        cpLightColor.setValue(new Color(c.getX(), c.getY(), c.getZ(), 1.0));
    }


    // --- Model Saving ---

    @FXML
    private void onSaveModel() {
        ModelWrapper selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("No model selected.");
            alert.show();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ File", "*.obj"));
        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());

        if (file != null) {
            try {
                Model modelToSave;
                if (cbApplyTransformOnSave.isSelected()) {
                    modelToSave = applyTransformToModel(selected);
                } else {
                    modelToSave = selected.getOriginalModel();
                }
                modelToSave.reindexVertices();
                ObjWriter.write(modelToSave, file.getAbsolutePath());

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Error saving file: " + e.getMessage());
                alert.show();
            }
        }
    }

    private Model applyTransformToModel(ModelWrapper wrapper) {
        Model original = wrapper.getOriginalModel();
        Matrix4f modelMatrix = wrapper.getModelMatrix();

        // Clone structure
        Model transformed = new Model();

        // Vertices
        ArrayList<Vector3f> newVertices = new ArrayList<>();
        for (Vector3f v : original.getVertices()) {
            if (v == null) newVertices.add(null);
            else newVertices.add(multiplyMatrix4ByVector3(modelMatrix, v));
        }
        transformed.setVertices(newVertices);

        // Normals
        ArrayList<Vector3f> newNormals = new ArrayList<>();
        for (Vector3f n : original.getNormals()) {
            if (n == null) newNormals.add(null);
            else newNormals.add(multiplyMatrix4ByNormal(modelMatrix, n));
        }
        transformed.setNormals(newNormals);

        // Texture Vertices (unchanged)
        transformed.setTextureVertices(new ArrayList<>(original.getTextureVertices()));

        // Polygons (deep copy indices, logic is same)
        ArrayList<Polygon> newPolygons = new ArrayList<>();
        for (Polygon p : original.getPolygons()) {
            Polygon newP = new Polygon();
            newP.setVertexIndices(new ArrayList<>(p.getVertexIndices()));
            if (p.getTextureVertexIndices() != null)
                newP.setTextureVertexIndices(new ArrayList<>(p.getTextureVertexIndices()));
            if (p.getNormalIndices() != null)
                newP.setNormalIndices(new ArrayList<>(p.getNormalIndices()));
            newPolygons.add(newP);
        }
        transformed.setPolygons(newPolygons);

        return transformed;
    }


    // --- Existing Methods ---

    private float getCameraFov(Camera camera) {
        if (camera == null) return 1.0f;
        try {
            Field fovField = Camera.class.getDeclaredField("fov");
            fovField.setAccessible(true);
            return fovField.getFloat(camera);
        } catch (Exception e) {
            return 1.0f;
        }
    }

    private Sensitivity getCameraSensitivity(Camera camera) {
        if (camera == null) return null;
        try {
            Field senField = Camera.class.getDeclaredField("sen");
            senField.setAccessible(true);
            return (Sensitivity) senField.get(camera);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateCameraSensitivity(Camera camera) {
        if (camera == null) return;
        try {
            Field senField = Camera.class.getDeclaredField("sen");
            senField.setAccessible(true);
            senField.set(camera, guiSensitivity);
        } catch (Exception e) {}
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
                float deltaX = (float)(event.getX() - lastMouseX);
                float deltaY = (float)(event.getY() - lastMouseY);
                if (scene.getCurrentCamera() != null) {
                    scene.getCurrentCamera().processMouseDrag(deltaX, deltaY);
                    updateCameraUI(scene.getCurrentCamera());
                }
                lastMouseX = event.getX();
                lastMouseY = event.getY();
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
                // Determine which spinner triggered action to call correct update
                if(spinner == spLightX || spinner == spLightY || spinner == spLightZ || spinner == spLightIntensity) {
                    onApplyLightSettings();
                } else {
                    onApplyTransform();
                }
            } catch (Exception ex) {
                spinner.getEditor().setText(converter.toString(factory.getValue()));
            }
        });
    }

    @FXML public void handleCameraForward(){ if(scene.getCurrentCamera()!=null){scene.getCurrentCamera().handleCameraForward(new ActionEvent(), TRANSLATION);updateCameraUI(scene.getCurrentCamera());}}
    @FXML public void handleCameraBackward(){ if(scene.getCurrentCamera()!=null){scene.getCurrentCamera().handleCameraBackward(new ActionEvent(), TRANSLATION);updateCameraUI(scene.getCurrentCamera());}}
    @FXML public void handleCameraLeft(){ if(scene.getCurrentCamera()!=null){scene.getCurrentCamera().handleCameraLeft(new ActionEvent(), TRANSLATION);updateCameraUI(scene.getCurrentCamera());}}
    @FXML public void handleCameraRight(){ if(scene.getCurrentCamera()!=null){scene.getCurrentCamera().handleCameraRight(new ActionEvent(), TRANSLATION);updateCameraUI(scene.getCurrentCamera());}}
    @FXML public void handleCameraUp(){ if(scene.getCurrentCamera()!=null){scene.getCurrentCamera().handleCameraUp(new ActionEvent(), TRANSLATION);updateCameraUI(scene.getCurrentCamera());}}
    @FXML public void handleCameraDown(){ if(scene.getCurrentCamera()!=null){scene.getCurrentCamera().handleCameraDown(new ActionEvent(), TRANSLATION);updateCameraUI(scene.getCurrentCamera());}}
    @FXML public void handleCameraReset() { if(scene.getCurrentCamera()!=null){scene.getCurrentCamera().reset();updateCameraUI(scene.getCurrentCamera());}}

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

        float currentFov = getCameraFov(camera);
        if (Math.abs((Double)spFov.getValue() - Math.toDegrees(currentFov)) > 0.1) {
            spFov.getValueFactory().setValue(Math.toDegrees(currentFov));
        }

        Sensitivity s = getCameraSensitivity(camera);
        if (s != null) {
            slMouseSens.setValue(s.mouseSensitivity);
            slZoomSens.setValue(s.zoomSensitivity);
        }
    }

    @FXML
    private void onApplyTransform() {
        ModelWrapper selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            selected.setPosition(new Vector3f(spTranslateX.getValue().floatValue(), spTranslateY.getValue().floatValue(), spTranslateZ.getValue().floatValue()));
            selected.setRotation(new Vector3f(spRotateX.getValue().floatValue(), spRotateY.getValue().floatValue(), spRotateZ.getValue().floatValue()));
            selected.setScale(new Vector3f(spScaleX.getValue().floatValue(), spScaleY.getValue().floatValue(), spScaleZ.getValue().floatValue()));
        } catch (Exception e) {}
    }

    private void updateTransformUI(ModelWrapper obj) {
        if (obj == null) return;
        Vector3f t = obj.getPosition();
        Vector3f r = obj.getRotation();
        Vector3f s = obj.getScale();
        if (t != null) { spTranslateX.getValueFactory().setValue((double)t.getX()); spTranslateY.getValueFactory().setValue((double)t.getY()); spTranslateZ.getValueFactory().setValue((double)t.getZ()); }
        if (r != null) { spRotateX.getValueFactory().setValue((double)r.getX()); spRotateY.getValueFactory().setValue((double)r.getY()); spRotateZ.getValueFactory().setValue((double)r.getZ()); }
        if (s != null) { spScaleX.getValueFactory().setValue((double)s.getX()); spScaleY.getValueFactory().setValue((double)s.getY()); spScaleZ.getValueFactory().setValue((double)s.getZ()); }
    }

    @FXML
    private void onLoadModel() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        File file = fc.showOpenDialog(canvas.getScene().getWindow());
        if (file == null) return;
        try {
            Model mesh = ObjReader.read(Files.readString(file.toPath()));
            Triangulation.triangulate(mesh);
            Normals.recalculateVertexNormals(mesh);
            ModelWrapper wrapper = new ModelWrapper(file.getName(), mesh);
            scene.addObject(wrapper);
            observableModels.add(wrapper);
            modelsList.getSelectionModel().select(wrapper);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void onRemoveModel() {
        ModelWrapper selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected != null) { scene.getObjects().remove(selected); observableModels.remove(selected); }
    }

    @FXML private void onLoadTexture() {
        ModelWrapper selected = modelsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) selected.setTexture(new Image(file.toURI().toString()));
    }

    @FXML private void onRemoveTexture() {
        ModelWrapper s = modelsList.getSelectionModel().getSelectedItem();
        if(s!=null) s.setTexture((Texture)null);
    }

    @FXML private void onAddCamera() {
        Camera c = new Camera(new Vector3f(0,0,10), new Vector3f(0,0,0), 1.0F, 1, 0.01F, 100, new Vector3f(0,0,0), "Camera "+(scene.getCameras().size()+1));
        scene.addCamera(c); observableCameras.add(c); updateCameraSensitivity(c);
    }

    @FXML private void onDeleteVertex() {
        ModelWrapper w = modelsList.getSelectionModel().getSelectedItem();
        if(w==null) return;
        try {
            int idx = Integer.parseInt(tfDeleteIndex.getText());
            if(idx>=0 && idx<w.getOriginalModel().getVertices().size()) w.getOriginalModel().removeVertex(idx);
        } catch(Exception e){}
    }

    @FXML private void onDeletePolygon() {
        ModelWrapper w = modelsList.getSelectionModel().getSelectedItem();
        if(w==null) return;
        try {
            int idx = Integer.parseInt(tfDeleteIndex.getText());
            if(idx>=0 && idx<w.getOriginalModel().getPolygons().size()) {
                Polygon p = w.getOriginalModel().getPolygons().get(idx);
                w.getOriginalModel().removePolygon(p, false);
            }
        } catch(Exception e){}
    }

    @FXML private void onToggleTheme() { setTheme(tbTheme.isSelected()); }

    private void setTheme(boolean dark) {
        try {
            RenderSettings s = scene.getRenderSettings();
            Field f = RenderSettings.class.getField("darkTheme");
            f.setBoolean(s, dark);
        } catch(Exception e){}
        if(dark) {
            anchorPane.setStyle("-fx-base: #333333; -fx-control-inner-background: #444444; -fx-text-fill: white; -fx-background-color: #333333;");
            canvasContainer.setStyle("-fx-background-color: #333333;");
            tbTheme.setText("Light Mode");
        } else {
            anchorPane.setStyle("");
            canvasContainer.setStyle("-fx-background-color: #F0F0F0;");
            tbTheme.setText("Dark Mode");
        }
    }
}
