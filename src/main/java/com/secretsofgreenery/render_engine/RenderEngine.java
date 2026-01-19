package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.*;
import com.secretsofgreenery.model.Model;
import com.secretsofgreenery.ui.ModelWrapper;
import com.secretsofgreenery.ui.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.secretsofgreenery.math.Matrix4f.multiplyMatrix4ByVector3;
import static com.secretsofgreenery.math.Point2f.distance;
import static com.secretsofgreenery.math.Point2f.vertexToPoint;
import static com.secretsofgreenery.math.Barycentric.barycentric;
import static com.secretsofgreenery.model.Normals.multiplyMatrix4ByNormal;
import static com.secretsofgreenery.render_engine.ColorUtils.multiplyColors;

public class RenderEngine {
    public static class RenderSettings {
        public boolean useTexture = true;
        public boolean drawWireframe = false;
        public boolean useLighting = true;
        public boolean useSpecular = true;
        public boolean cameraLightSource = false;
        public boolean drawGrid = false;
        public Vector3f fallbackColor = new Vector3f(0.66F, 0.66F, 0.66F); // Цвет, если текстуры нет - {r, g, b} в диапазоне [0, 1]
        public boolean darkTheme = false;
    }

    private static RenderSettings settings;

    public static void render(
            final GraphicsContext graphicsContext,
            final Scene scene,
            final int width,
            final int height)
    {
        settings = scene.getRenderSettings();
        float[] zBuffer = new float[width * height];
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);

        int[] pixelBuffer = new int[width * height];
        PixelWriter pixelWriter = graphicsContext.getPixelWriter();

        Camera camera = scene.getCurrentCamera();
        List<Light> lights = scene.getLights();
        Matrix4f viewProjectionMatrix = camera.getViewProjectionMatrix();

        for(ModelWrapper model : scene.getObjects()) {
            if (scene.getRenderSettings().cameraLightSource) {
                Light cameraLight = new Light(camera.getPosition(), new Vector3f(1.0f, 1.0f, 1.0f), 1.0f);
                ArrayList<Light> lightsWithCamera = new ArrayList<>(lights); // Не меняем исходный список, чтобы не ломать ui
                lightsWithCamera.add(cameraLight);
                renderModel(model, camera, viewProjectionMatrix, lightsWithCamera, pixelBuffer, zBuffer, width, height);
            } else {
                renderModel(model, camera, viewProjectionMatrix, lights, pixelBuffer, zBuffer, width, height);
            }
        }

        if (settings.drawGrid) {
            renderGrid(camera, pixelBuffer, zBuffer, width, height);
            renderAxes(camera, pixelBuffer, zBuffer, width, height);
        }

        pixelWriter.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixelBuffer, 0, width);
    }

    private static void renderModel(
            final ModelWrapper model,
            final Camera camera,
            final Matrix4f viewProjectionMatrix,
            //final Matrix4f mvpMatrix,
            final List<Light> lights,
            final int[] pixelBuffer,
            final float[] zBuffer,
            final int width,
            final int height)
    {
        Model mesh = model.getOriginalModel();
        Matrix4f modelMatrix = model.getModelMatrix();
        Matrix4f mvpMatrix = viewProjectionMatrix.multiply(modelMatrix);
        final int nPolygons = mesh.getPolygons().size();

        List<Integer> selectedPolygonQueue = new ArrayList<>();

        Vector3f cameraPos = camera.getPosition();

        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            ArrayList<Integer> vertexIndices = mesh.getPolygons().get(polygonInd).getVertexIndices();
            ArrayList<Integer> textureVertexIndices = mesh.getPolygons().get(polygonInd).getTextureVertexIndices();
            ArrayList<Integer> normalIndices = mesh.getPolygons().get(polygonInd).getNormalIndices();

            boolean isSelectedPoly = model.getSelectedPolygonIndices().contains(polygonInd);

            boolean drawTexture = settings.useTexture
                    && model.getTexture() != null
                    && textureVertexIndices != null
                    && textureVertexIndices.size() == 3;

            Vector3f v1Local = mesh.getVertices().get(vertexIndices.get(0));
            Vector3f v2Local = mesh.getVertices().get(vertexIndices.get(1));
            Vector3f v3Local = mesh.getVertices().get(vertexIndices.get(2));

            Vector3f v1World = multiplyMatrix4ByVector3(modelMatrix, v1Local);
            Vector3f v2World = multiplyMatrix4ByVector3(modelMatrix, v2Local);
            Vector3f v3World = multiplyMatrix4ByVector3(modelMatrix, v3Local);

            // Вектор взгляда НА полигон (от камеры к точке)
            Vector3f viewDir = v1World.subtract(cameraPos).normalize();

            // Геометрическая нормаль треугольника (по правилу правой руки)
            // (V2 - V1) cross (V3 - V1)
            Vector3f edge1 = v2World.subtract(v1World);
            Vector3f edge2 = v3World.subtract(v1World);
            Vector3f faceNormal = edge1.cross(edge2).normalize();

            // Проверяем видимость
            // Если нормаль и взгляд направлены навстречу друг другу (dot < 0), грань видима.
            // Если они смотрят в одну сторону (dot > 0), грань отвернута.
            // Примечание: Знак зависит от порядка вершин в модели (CCW или CW).
            // Для стандартных OBJ файлов обычно используется CCW, и условие видимости dot < 0.

            if (faceNormal.dot(viewDir) > 0) {
                continue; // Пропускаем отрисовку этой грани (и её сетки!)
            }

            Vector4f v1Screen4f = mvpMatrix.multiplyByVector(new Vector4f(v1Local, 1.0f));
            Vector4f v2Screen4f = mvpMatrix.multiplyByVector(new Vector4f(v2Local, 1.0f));
            Vector4f v3Screen4f = mvpMatrix.multiplyByVector(new Vector4f(v3Local, 1.0f));

            float w1 = v1Screen4f.getW();
            float w2 = v2Screen4f.getW();
            float w3 = v3Screen4f.getW();

            Vector3f v1Screen = new Vector3f(v1Screen4f.getX()/w1, v1Screen4f.getY()/w1, v1Screen4f.getZ()/w1);
            Vector3f v2Screen = new Vector3f(v2Screen4f.getX()/w2, v2Screen4f.getY()/w2, v2Screen4f.getZ()/w2);
            Vector3f v3Screen = new Vector3f(v3Screen4f.getX()/w3, v3Screen4f.getY()/w3, v3Screen4f.getZ()/w3);

            Point2f p1 = vertexToPoint(v1Screen, width, height);
            Point2f p2 = vertexToPoint(v2Screen, width, height);
            Point2f p3 = vertexToPoint(v3Screen, width, height);

            Vector2f t1 = drawTexture ? mesh.getTextureVertices().get(textureVertexIndices.get(0)) : null;
            Vector2f t2 = drawTexture ? mesh.getTextureVertices().get(textureVertexIndices.get(1)) : null;
            Vector2f t3 = drawTexture ? mesh.getTextureVertices().get(textureVertexIndices.get(2)) : null;

            Vector3f n1Local = mesh.getNormals().get(normalIndices.get(0));
            Vector3f n2Local = mesh.getNormals().get(normalIndices.get(1));
            Vector3f n3Local = mesh.getNormals().get(normalIndices.get(2));

            Vector3f n1World = multiplyMatrix4ByNormal(modelMatrix, n1Local);
            Vector3f n2World = multiplyMatrix4ByNormal(modelMatrix, n2Local);
            Vector3f n3World = multiplyMatrix4ByNormal(modelMatrix, n3Local);

            rasterizeTriangle(
                    p1, p2, p3,
                    v1Screen.getZ(), v2Screen.getZ(), v3Screen.getZ(),
                    w1, w2, w3,
                    v1World, v2World, v3World,
                    n1World, n2World, n3World,
                    t1, t2, t3,
                    lights,
                    drawTexture,
                    model.getTexture(),
                    zBuffer, width, height,
                    pixelBuffer,
                    cameraPos
            );


            if (isSelectedPoly) {
                selectedPolygonQueue.add(polygonInd);
                continue;
            }

            if (settings.drawWireframe) {
                int defaultColor = 0xFF111111;
                drawLine(p1, p2, v1Screen.getZ(), v2Screen.getZ(), pixelBuffer, zBuffer, width, height, defaultColor);
                drawLine(p2, p3, v2Screen.getZ(), v3Screen.getZ(), pixelBuffer, zBuffer, width, height, defaultColor);
                drawLine(p3, p1, v3Screen.getZ(), v1Screen.getZ(), pixelBuffer, zBuffer, width, height, defaultColor);
            }

            // 3. Рисуем вершины, если они выделены вручную (даже если полигон не выделен)
            for (int i = 0; i < 3; i++) {
                int vertexIndex = vertexIndices.get(i);
                // Рисуем точку только если вершина выделена явно ИЛИ включен режим сетки
                boolean isSelectedVertex = model.getSelectedVertexIndices().contains(vertexIndex);
                if (isSelectedVertex || settings.drawWireframe) {
                    int pointColor = isSelectedVertex ? 0xfffc6203 : 0xFF000000;
                    Point2f p = (i == 0) ? p1 : (i == 1) ? p2 : p3;
                    float z = (i == 0) ? v1Screen.getZ() : (i == 1) ? v2Screen.getZ() : v3Screen.getZ();

                    // Если вершина не выделена, но есть сетка - рисуем черным. Если выделена - оранжевым.
                    drawVertexPoint(p, z, pixelBuffer, zBuffer, width, height, pointColor);
                }
            }
        }

        // --- ПРОХОД 2: Рисуем выделенные полигоны поверх всего ---
        for (Integer polygonInd : selectedPolygonQueue) {
            ArrayList<Integer> vertexIndices = mesh.getPolygons().get(polygonInd).getVertexIndices();
            Vector3f v1Local = mesh.getVertices().get(vertexIndices.get(0));
            Vector3f v2Local = mesh.getVertices().get(vertexIndices.get(1));
            Vector3f v3Local = mesh.getVertices().get(vertexIndices.get(2));

            Vector4f v1Screen4f = mvpMatrix.multiplyByVector(new Vector4f(v1Local, 1.0f));
            Vector4f v2Screen4f = mvpMatrix.multiplyByVector(new Vector4f(v2Local, 1.0f));
            Vector4f v3Screen4f = mvpMatrix.multiplyByVector(new Vector4f(v3Local, 1.0f));

            float w1 = v1Screen4f.getW();
            float w2 = v2Screen4f.getW();
            float w3 = v3Screen4f.getW();

            Vector3f v1Screen = new Vector3f(v1Screen4f.getX()/w1, v1Screen4f.getY()/w1, v1Screen4f.getZ()/w1);
            Vector3f v2Screen = new Vector3f(v2Screen4f.getX()/w2, v2Screen4f.getY()/w2, v2Screen4f.getZ()/w2);
            Vector3f v3Screen = new Vector3f(v3Screen4f.getX()/w3, v3Screen4f.getY()/w3, v3Screen4f.getZ()/w3);

            Point2f p1 = vertexToPoint(v1Screen, width, height);
            Point2f p2 = vertexToPoint(v2Screen, width, height);
            Point2f p3 = vertexToPoint(v3Screen, width, height);

            int selectionColor = 0xfffca503;
            drawLine(p1, p2, v1Screen.getZ(), v2Screen.getZ(), pixelBuffer, zBuffer, width, height, selectionColor);
            drawLine(p2, p3, v2Screen.getZ(), v3Screen.getZ(), pixelBuffer, zBuffer, width, height, selectionColor);
            drawLine(p3, p1, v3Screen.getZ(), v1Screen.getZ(), pixelBuffer, zBuffer, width, height, selectionColor);

            int vertexColor = 0xfffc6203;
            drawVertexPoint(p1, v1Screen.getZ(), pixelBuffer, zBuffer, width, height, vertexColor);
            drawVertexPoint(p2, v2Screen.getZ(), pixelBuffer, zBuffer, width, height, vertexColor);
            drawVertexPoint(p3, v3Screen.getZ(), pixelBuffer, zBuffer, width, height, vertexColor);
        }
    }

    private static void rasterizeTriangle(
            Point2f p1, Point2f p2, Point2f p3,
            float z1, float z2, float z3,
            float w1, float w2, float w3,
            Vector3f v1World, Vector3f v2World, Vector3f v3World,
            Vector3f n1World, Vector3f n2World, Vector3f n3World,
            Vector2f t1, Vector2f t2, Vector2f t3,
            List<Light> lights,
            boolean drawTexture,
            Texture texture,
            float[] zBuffer, int width, int height,
            int[] pixelBuffer,
            Vector3f cameraPos)
    {
        // Bounding Box
        int minX = (int) Math.max(0, Math.min(p1.getX(), Math.min(p2.getX(), p3.getX())));
        int maxX = (int) Math.min(width - 1, Math.max(p1.getX(), Math.max(p2.getX(), p3.getX())));
        int minY = (int) Math.max(0, Math.min(p1.getY(), Math.min(p2.getY(), p3.getY())));
        int maxY = (int) Math.min(height - 1, Math.max(p1.getY(), Math.max(p2.getY(), p3.getY())));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2f p = new Point2f(x, y);
                Vector3f barycentric = barycentric(p, p1, p2, p3);
                float alpha = barycentric.getX();
                float beta = barycentric.getY();
                float gamma = barycentric.getZ();

                // Если пиксель внутри треугольника
                if (alpha >= 0 && beta >= 0 && gamma >= 0) {

                    float reciprocalW = (alpha / w1) + (beta / w2) + (gamma / w3);

                    // Интерполяция глубины
                    float z = alpha * z1 + beta * z2 + gamma * z3;

                    int index = y * width + x;

                    if (z < zBuffer[index]) {

                        // Интерполяция Мировой Позиции пикселя
                        // P_pixel = alpha * A + beta * B + gamma * C
                        float px = alpha * v1World.getX() / w1 + beta * v2World.getX() / w2 + gamma * v3World.getX() / w3;
                        float py = alpha * v1World.getY() / w1 + beta * v2World.getY() / w2 + gamma * v3World.getY() / w3;
                        float pz = alpha * v1World.getZ() / w1 + beta * v2World.getZ() / w2 + gamma * v3World.getZ() / w3;
                        Vector3f pixelPos = new Vector3f(px / reciprocalW, py / reciprocalW, pz / reciprocalW);

                        // Интерполяция Нормали
                        float nx = alpha * n1World.getX() / w1 + beta * n2World.getX() / w2 + gamma * n3World.getX() / w3;
                        float ny = alpha * n1World.getY() / w1 + beta * n2World.getY() / w2 + gamma * n3World.getY() / w3;
                        float nz = alpha * n1World.getZ() / w1 + beta * n2World.getZ() / w2 + gamma * n3World.getZ() / w3;
                        Vector3f pixelNormal = new Vector3f(nx / reciprocalW, ny / reciprocalW, nz / reciprocalW);

                        // Нормализуем ненулевые нормали
                        if (pixelNormal.length() > 1e-5) {
                            pixelNormal = pixelNormal.normalize();
                        }

                        Vector3f colorVec = pixelShader(
                                barycentric,
                                t1, t2, t3,
                                w1, w2, w3,
                                drawTexture,
                                texture,
                                pixelPos,
                                pixelNormal,
                                lights,
                                cameraPos
                        );

                        zBuffer[index] = z;
                        pixelBuffer[index] = ColorUtils.vectorToInt(colorVec);


                    }
                }
            }
        }
    }

    private static Vector3f pixelShader(
            Vector3f barycentric, // alpha, beta, gamma
            Vector2f t1, Vector2f t2, Vector2f t3,
            float w1, float w2, float w3,
            boolean drawTexture,
            Texture texture,
            Vector3f pixelPos,
            Vector3f pixelNormal,
            List<Light> lights,
            Vector3f cameraPos)
    {
        Vector3f objectColor = settings.fallbackColor;

        if (drawTexture) {
            float alpha = barycentric.getX();
            float beta = barycentric.getY();
            float gamma = barycentric.getZ();

            float reciprocalW = (alpha / w1) + (beta / w2) + (gamma / w3);

            float u = (alpha * t1.getX() / w1 + beta * t2.getX() / w2 + gamma * t3.getX() / w3) / reciprocalW;
            float v = (alpha * t1.getY() / w1 + beta * t2.getY() / w2 + gamma * t3.getY() / w3) / reciprocalW;

            objectColor = texture.getPixel(u, v);
        }

        if (settings.useLighting && lights != null && !lights.isEmpty()) {
            Vector3f diffuseSum = new Vector3f(0.2F, 0.2F, 0.2F); // Ambient
            Vector3f specularSum = new Vector3f(0, 0, 0);

            Vector3f viewDir = cameraPos.subtract(pixelPos).normalize();

            // Параметры материала (можно вынести в настройки в будущем)
            float specularStrength = 0.5f; // Яркость блика
            int shininess = 32;            // Степень блеска (больше = меньше пятно)

            for(Light light : lights){
                Vector3f lightVector = light.getPosition().subtract(pixelPos);
                float distance = lightVector.length();
                Vector3f L = lightVector.normalize();
                Vector3f N = pixelNormal;

                float diff = Math.max(0, N.dot(L)); // Diffuse
                float att = light.getAttenuationCoefficient(distance); // Затухание

                Vector3f lightColor = light.getColor();
                float intensity = light.getIntensity();

                Vector3f currentDiffuse = lightColor.multiply(intensity * diff * att);
                diffuseSum = diffuseSum.add(currentDiffuse);

                if(settings.useSpecular && diff > 0){
                    Vector3f R = N.multiply(2 * N.dot(L)).subtract(L);
                    float specAngle = Math.max(0, viewDir.dot(R));
                    float specFactor = (float) Math.pow(specAngle, shininess);

                    Vector3f currentSpecular = lightColor.multiply(intensity * specFactor * specularStrength * att);
                    specularSum = specularSum.add(currentSpecular);
                }
            }
            Vector3f totalLight = diffuseSum;
            Vector3f resultColor = multiplyColors(objectColor, totalLight).add(specularSum);
            return resultColor;
        }
        return objectColor;
    }

    /**
     * Рисует линию с учетом Z-буфера.
     */
    private static void drawLine(
            Point2f p1, Point2f p2,
            float z1, float z2,
            int[] pixelBuffer, float[] zBuffer, int width, int height,
            int color)
    {
        int x1 = (int) p1.getX();
        int y1 = (int) p1.getY();
        int x2 = (int) p2.getX();
        int y2 = (int) p2.getY();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        // Длина линии в пикселях (для интерполяции Z)
        float totalDist = (float) Math.sqrt(dx * dx + dy * dy);
        if (totalDist == 0) totalDist = 1;

        int currentX = x1;
        int currentY = y1;

        while (true) {
            // Проверка границ экрана
            if (currentX >= 0 && currentX < width && currentY >= 0 && currentY < height) {

                // Интерполяция Z
                // Считаем, какую долю пути мы прошли (t от 0 до 1)
                float dist = (float) Math.sqrt((currentX - x1)*(currentX - x1) + (currentY - y1)*(currentY - y1));
                float t = dist / totalDist;

                // Линейная интерполяция глубины (грубая, без перспективной коррекции, но для сетки сойдет)
                // Для идеальной сетки нужно интерполировать 1/w, но это усложнит код.
                float z = z1 * (1 - t) + z2 * t;

                // Z-Test с небольшим смещением (bias), чтобы сетка была чуть-чуть ближе полигона
                // и не мерцала (z-fighting)
                int idx = currentY * width + currentX;

                float offset = 2e-6f + 1e-9f / Math.abs(z);
                //float epsilon = 1e-9f / z;
                if (z < zBuffer[idx] + offset) { // bias зависит от масштаба Z
                    pixelBuffer[idx] = color;
                    // Не обновляем zBuffer, чтобы точки вершин могли нарисоваться поверх
                }
            }

            if (currentX == x2 && currentY == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                currentX += sx;
            }
            if (e2 < dx) {
                err += dx;
                currentY += sy;
            }
        }
    }

    private static void drawVertexPoint(
            Point2f p, float z,
            int[] pixelBuffer, float[] zBuffer, int width, int height,
            int color)
    {
        int cx = (int) p.getX();
        int cy = (int) p.getY();
        int size = 2; // Радиус точки (итоговый размер 5x5)

        for (int y = cy - size; y <= cy + size; y++) {
            for (int x = cx - size; x <= cx + size; x++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int idx = y * width + x;
                    // Z-Test с еще большим смещением, чтобы точки были поверх линий
                    float offset = 2e-6f + 2e-9f / Math.abs(z);
                    //float epsilon = 2*1e-9f/z;
                    if (z < zBuffer[idx] + offset) {
                        pixelBuffer[idx] = color;
                    }
                }
            }
        }
    }

    private static void renderGrid(Camera camera, int[] pixelBuffer, float[] zBuffer, int width, int height) {
        int gridSize = 20; // Размер сетки (от -10 до +10)
        int step = 1;      // Шаг сетки
        int gridColor = settings.darkTheme ? (0xFF333333 ^ 0x00FFFFFF) : 0xFF333333;

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f vpMatrix = projectionMatrix.multiply(viewMatrix); // Model matrix = Identity, поэтому просто VP

        // Линии вдоль оси X (изменяется Z)
        for (int z = -gridSize; z <= gridSize; z += step) {
            drawProjectedLine(new Vector3f(-gridSize, 0, z), new Vector3f(gridSize, 0, z),
                    vpMatrix, pixelBuffer, zBuffer, width, height, gridColor);
        }

        // Линии вдоль оси Z (изменяется X)
        for (int x = -gridSize; x <= gridSize; x += step) {
            drawProjectedLine(new Vector3f(x, 0, -gridSize), new Vector3f(x, 0, gridSize),
                    vpMatrix, pixelBuffer, zBuffer, width, height, gridColor);
        }
    }

    private static void renderAxes(Camera camera, int[] pixelBuffer, float[] zBuffer, int width, int height) {
        float axisLength = 20;
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f vpMatrix = projectionMatrix.multiply(viewMatrix);

        Vector3f origin = new Vector3f(0, 0, 0);

        drawProjectedLine(origin, new Vector3f(axisLength, 0, 0), vpMatrix, pixelBuffer, zBuffer, width, height, 0xFFFF0000);
        drawProjectedLine(origin, new Vector3f(0, axisLength, 0), vpMatrix, pixelBuffer, zBuffer, width, height, 0xFF00FF00);
        drawProjectedLine(origin, new Vector3f(0, 0, axisLength), vpMatrix, pixelBuffer, zBuffer, width, height, 0xFF0000FF);
    }

    private static void drawProjectedLine(Vector3f v1World, Vector3f v2World, Matrix4f vpMatrix,
                                          int[] pixelBuffer, float[] zBuffer, int width, int height, int color) {

        Vector4f p1Clip = vpMatrix.multiplyByVector(new Vector4f(v1World, 1.0f));
        Vector4f p2Clip = vpMatrix.multiplyByVector(new Vector4f(v2World, 1.0f));

        float nearPlaneW = 1e-5f;
        boolean p1Visible = p1Clip.getW() > nearPlaneW;
        boolean p2Visible = p2Clip.getW() > nearPlaneW;

        if (!p1Visible && !p2Visible) return;

        if (p1Visible != p2Visible) {
            float t = (nearPlaneW - p1Clip.getW()) / (p2Clip.getW() - p1Clip.getW());
            float newX = p1Clip.getX() + t * (p2Clip.getX() - p1Clip.getX());
            float newY = p1Clip.getY() + t * (p2Clip.getY() - p1Clip.getY());
            float newZ = p1Clip.getZ() + t * (p2Clip.getZ() - p1Clip.getZ());
            Vector4f intersection = new Vector4f(newX, newY, newZ, nearPlaneW);

            if (!p1Visible) p1Clip = intersection;
            else p2Clip = intersection;
        }

        Vector3f v1NDC = new Vector3f(p1Clip.getX() / p1Clip.getW(), p1Clip.getY() / p1Clip.getW(), p1Clip.getZ() / p1Clip.getW());
        Vector3f v2NDC = new Vector3f(p2Clip.getX() / p2Clip.getW(), p2Clip.getY() / p2Clip.getW(), p2Clip.getZ() / p2Clip.getW());

        Point2f p1Screen = vertexToPoint(v1NDC, width, height);
        Point2f p2Screen = vertexToPoint(v2NDC, width, height);

        float x1 = p1Screen.getX();
        float y1 = p1Screen.getY();
        float x2 = p2Screen.getX();
        float y2 = p2Screen.getY();
        float z1 = v1NDC.getZ();
        float z2 = v2NDC.getZ();

        int outcode0 = computeOutCode(x1, y1, width, height);
        int outcode1 = computeOutCode(x2, y2, width, height);
        boolean accept = false;

        while (true) {
            if ((outcode0 | outcode1) == 0) {
                // Обе точки внутри экрана
                accept = true;
                break;
            } else if ((outcode0 & outcode1) != 0) {
                // Обе точки снаружи с одной стороны (например, обе слева)
                break;
            } else {
                // Часть линии внутри, часть снаружи. Нужно резать.
                float x = 0, y = 0, z = 0;
                // Выбираем точку снаружи
                int outcodeOut = (outcode0 != 0) ? outcode0 : outcode1;

                // Формулы пересечения с границами прямоугольника
                // Также интерполируем Z, чтобы Z-буфер работал корректно
                if ((outcodeOut & BOTTOM) != 0) {
                    float t = (height - 1 - y1) / (y2 - y1);
                    x = x1 + (x2 - x1) * t;
                    y = height - 1;
                    z = z1 + (z2 - z1) * t;
                } else if ((outcodeOut & TOP) != 0) {
                    float t = (0 - y1) / (y2 - y1);
                    x = x1 + (x2 - x1) * t;
                    y = 0;
                    z = z1 + (z2 - z1) * t;
                } else if ((outcodeOut & RIGHT) != 0) {
                    float t = (width - 1 - x1) / (x2 - x1);
                    y = y1 + (y2 - y1) * t;
                    x = width - 1;
                    z = z1 + (z2 - z1) * t;
                } else if ((outcodeOut & LEFT) != 0) {
                    float t = (0 - x1) / (x2 - x1);
                    y = y1 + (y2 - y1) * t;
                    x = 0;
                    z = z1 + (z2 - z1) * t;
                }

                if (outcodeOut == outcode0) {
                    x1 = x; y1 = y; z1 = z;
                    outcode0 = computeOutCode(x1, y1, width, height);
                } else {
                    x2 = x; y2 = y; z2 = z;
                    outcode1 = computeOutCode(x2, y2, width, height);
                }
            }
        }

        if (accept) {
            drawLine(new Point2f(x1, y1), new Point2f(x2, y2), z1, z2, pixelBuffer, zBuffer, width, height, color);
        }
    }

    // --- Константы для алгоритма Коэна-Сазерленда ---
    private static final int INSIDE = 0; // 0000
    private static final int LEFT   = 1; // 0001
    private static final int RIGHT  = 2; // 0010
    private static final int BOTTOM = 4; // 0100
    private static final int TOP    = 8; // 1000

    private static int computeOutCode(float x, float y, float w, float h) {
        int code = INSIDE;
        if (x < 0)           code |= LEFT;
        else if (x >= w)     code |= RIGHT;
        if (y < 0)           code |= TOP;
        else if (y >= h)     code |= BOTTOM;
        return code;
    }

    // Возвращает массив: [Index полигона, Index вершины (или -1)]
    // Если ничего не найдено, возвращает {-1, -1}
    public static int[] pick(
            int mouseX, int mouseY,
            Scene scene,
            int width, int height)
    {
        Camera camera = scene.getCurrentCamera();
        Matrix4f viewProjectionMatrix = camera.getViewProjectionMatrix();

        float minZ = Float.POSITIVE_INFINITY;
        int bestPolygonIndex = -1;
        int bestVertexIndex = -1;
        ModelWrapper bestModel = null;

        for (ModelWrapper model : scene.getObjects()) {
            if (!model.getIsVisibleProp()) continue;

            Model mesh = model.getOriginalModel();
            Matrix4f mvpMatrix = viewProjectionMatrix.multiply(model.getModelMatrix());

            for (int i = 0; i < mesh.getPolygons().size(); i++) {
                ArrayList<Integer> indices = mesh.getPolygons().get(i).getVertexIndices();

                Vector3f v1Local = mesh.getVertices().get(indices.get(0));
                Vector3f v2Local = mesh.getVertices().get(indices.get(1));
                Vector3f v3Local = mesh.getVertices().get(indices.get(2));

                Vector4f v1Clip = mvpMatrix.multiplyByVector(new Vector4f(v1Local, 1.0f));
                Vector4f v2Clip = mvpMatrix.multiplyByVector(new Vector4f(v2Local, 1.0f));
                Vector4f v3Clip = mvpMatrix.multiplyByVector(new Vector4f(v3Local, 1.0f));

                if (v1Clip.getW() <= 0 || v2Clip.getW() <= 0 || v3Clip.getW() <= 0) continue;

                Vector3f v1NDC = new Vector3f(v1Clip.getX()/v1Clip.getW(), v1Clip.getY()/v1Clip.getW(), v1Clip.getZ()/v1Clip.getW());
                Vector3f v2NDC = new Vector3f(v2Clip.getX()/v2Clip.getW(), v2Clip.getY()/v2Clip.getW(), v2Clip.getZ()/v2Clip.getW());
                Vector3f v3NDC = new Vector3f(v3Clip.getX()/v3Clip.getW(), v3Clip.getY()/v3Clip.getW(), v3Clip.getZ()/v3Clip.getW());

                Point2f p1 = vertexToPoint(v1NDC, width, height);
                Point2f p2 = vertexToPoint(v2NDC, width, height);
                Point2f p3 = vertexToPoint(v3NDC, width, height);
                Point2f mouseP = new Point2f(mouseX, mouseY);

                Vector3f bar = barycentric(mouseP, p1, p2, p3);
                if (bar.getX() >= 0 && bar.getY() >= 0 && bar.getZ() >= 0) {

                    float currentZ = bar.getX() * v1NDC.getZ() + bar.getY() * v2NDC.getZ() + bar.getZ() * v3NDC.getZ();

                    if (currentZ < minZ) {
                        minZ = currentZ;
                        bestPolygonIndex = i;
                        bestVertexIndex = -1; // Сбрасываем вершину, так как нашли новый лучший полигон

                        // 4. Проверяем, не кликнули ли мы прямо в вершину (с радиусом 10px)
                        float dist1 = distance(p1, mouseP);
                        float dist2 = distance(p2, mouseP);
                        float dist3 = distance(p3, mouseP);
                        float threshold = 15.0f;

                        if (dist1 < threshold && dist1 < dist2 && dist1 < dist3) bestVertexIndex = indices.get(0);
                        else if (dist2 < threshold && dist2 < dist3) bestVertexIndex = indices.get(1);
                        else if (dist3 < threshold) bestVertexIndex = indices.get(2);
                    }
                }
            }
        }
        return new int[]{bestPolygonIndex, bestVertexIndex};
    }

}