package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.*;
import com.secretsofgreenery.model.Model;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;

import java.util.ArrayList;
import java.util.Arrays;

import static com.secretsofgreenery.math.Matrix4f.multiplyMatrix4ByVector3;
import static com.secretsofgreenery.math.Point2f.vertexToPoint;
import static com.secretsofgreenery.math.Barycentric.barycentric;
import static com.secretsofgreenery.model.Normals.multiplyMatrix4ByNormal;
import static com.secretsofgreenery.render_engine.ColorUtils.multiplyColors;

public class RenderEngine {
    public static class RenderSettings {
        public boolean useTexture = true;
        public boolean drawWireframe = true;
        public boolean useLighting = true;
        public boolean useSpecular = false;
        public boolean cameraLightSource = true;
        public Vector3f fallbackColor = new Vector3f(0.66F, 0.66F, 0.66F); // Цвет, если текстуры нет - {r, g, b} в диапазоне [0, 1]
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Texture texture,
            ArrayList<Light> lights,
            final RenderSettings settings)
    {
        float[] zBuffer = new float[width * height];
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);

        int[] pixelBuffer = new int[width * height];
        PixelWriter pixelWriter = graphicsContext.getPixelWriter();

        Matrix4f modelMatrix = new AffineTransform().apply();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = GraphicConveyor.assembleModelViewProjection(modelMatrix, viewMatrix, projectionMatrix);

        if(settings.cameraLightSource){
            Light cameraLight = new Light(camera.getPosition(), new Vector3f(1.0f, 1.0f, 1.0f), 1.0f);
            ArrayList<Light> lightsWithCamera = new ArrayList<>(lights); // Не меняем исходный список, чтобы не ломать ui
            lightsWithCamera.add(cameraLight);
            renderModel(mesh, camera, modelMatrix, modelViewProjectionMatrix, pixelBuffer, zBuffer, width, height, texture, lightsWithCamera, settings);
        }
        else {
            renderModel(mesh, camera, modelMatrix, modelViewProjectionMatrix, pixelBuffer, zBuffer, width, height, texture, lights, settings);
        }

        pixelWriter.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixelBuffer, 0, width);
    }

    private static void renderModel(
            final Model mesh,
            final Camera camera,
            final Matrix4f modelMatrix,
            final Matrix4f mvpMatrix,
            final int[] pixelBuffer,
            final float[] zBuffer,
            final int width,
            final int height,
            final Texture texture,
            ArrayList<Light> lights,
            final RenderSettings settings)
    {
        final int nPolygons = mesh.getPolygons().size();

        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            ArrayList<Integer> vertexIndices = mesh.getPolygons().get(polygonInd).getVertexIndices();
            ArrayList<Integer> textureVertexIndices = mesh.getPolygons().get(polygonInd).getTextureVertexIndices();
            ArrayList<Integer> normalIndices = mesh.getPolygons().get(polygonInd).getNormalIndices();

            boolean hasTexture = settings.useTexture
                    && texture != null
                    && textureVertexIndices != null
                    && textureVertexIndices.size() == 3;

            Vector3f v1Local = mesh.getVertices().get(vertexIndices.get(0));
            Vector3f v2Local = mesh.getVertices().get(vertexIndices.get(1));
            Vector3f v3Local = mesh.getVertices().get(vertexIndices.get(2));

            Vector3f v1World = multiplyMatrix4ByVector3(modelMatrix, v1Local);
            Vector3f v2World = multiplyMatrix4ByVector3(modelMatrix, v2Local);
            Vector3f v3World = multiplyMatrix4ByVector3(modelMatrix, v3Local);

            Vector3f cameraPos = camera.getPosition();

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

            Vector3f v1Screen = multiplyMatrix4ByVector3(mvpMatrix, v1Local);
            Vector3f v2Screen = multiplyMatrix4ByVector3(mvpMatrix, v2Local);
            Vector3f v3Screen = multiplyMatrix4ByVector3(mvpMatrix, v3Local);

            Point2f p1 = vertexToPoint(v1Screen, width, height);
            Point2f p2 = vertexToPoint(v2Screen, width, height);
            Point2f p3 = vertexToPoint(v3Screen, width, height);

            Vector2f t1 = hasTexture ? mesh.getTextureVertices().get(textureVertexIndices.get(0)) : null;
            Vector2f t2 = hasTexture ? mesh.getTextureVertices().get(textureVertexIndices.get(1)) : null;
            Vector2f t3 = hasTexture ? mesh.getTextureVertices().get(textureVertexIndices.get(2)) : null;

            Vector3f n1Local = mesh.getNormals().get(normalIndices.get(0));
            Vector3f n2Local = mesh.getNormals().get(normalIndices.get(1));
            Vector3f n3Local = mesh.getNormals().get(normalIndices.get(2));

            Vector3f n1World = multiplyMatrix4ByNormal(modelMatrix, n1Local);
            Vector3f n2World = multiplyMatrix4ByNormal(modelMatrix, n2Local);
            Vector3f n3World = multiplyMatrix4ByNormal(modelMatrix, n3Local);

            rasterizeTriangle(
                    p1, p2, p3,
                    v1Screen.getZ(), v2Screen.getZ(), v3Screen.getZ(),
                    v1World, v2World, v3World,
                    n1World, n2World, n3World,
                    t1, t2, t3,
                    hasTexture,
                    zBuffer, width, height,
                    pixelBuffer, texture,
                    lights,
                    settings
            );

            if (settings.drawWireframe) {
                // Используем vXScreen.getZ() как глубину
                float d1 = v1Screen.getZ();
                float d2 = v2Screen.getZ();
                float d3 = v3Screen.getZ();

                int edgeColor = 0xfffca503; // Оранжевый
                int pointColor = 0xfffc6203; // Еще более оранжевый

                // Ребра
                drawLine(p1, p2, d1, d2, pixelBuffer, zBuffer, width, height, edgeColor);
                drawLine(p2, p3, d2, d3, pixelBuffer, zBuffer, width, height, edgeColor);
                drawLine(p3, p1, d3, d1, pixelBuffer, zBuffer, width, height, edgeColor);

                // Вершины
                drawVertexPoint(p1, d1, pixelBuffer, zBuffer, width, height, pointColor);
                drawVertexPoint(p2, d2, pixelBuffer, zBuffer, width, height, pointColor);
                drawVertexPoint(p3, d3, pixelBuffer, zBuffer, width, height, pointColor);
            }
        }
    }

    private static void rasterizeTriangle(
            Point2f p1, Point2f p2, Point2f p3,
            float z1, float z2, float z3,
            Vector3f v1World, Vector3f v2World, Vector3f v3World,
            Vector3f n1World, Vector3f n2World, Vector3f n3World,
            Vector2f t1, Vector2f t2, Vector2f t3,
            boolean hasTexture,
            float[] zBuffer, int width, int height,
            int[] pixelBuffer, Texture texture,
            ArrayList<Light> lights,
            RenderSettings settings)
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

                    // Интерполяция глубины
                    float z = alpha * z1 + beta * z2 + gamma * z3;

                    int index = y * width + x;

                    if (z < zBuffer[index]) {

                        // Интерполяция Мировой Позиции пикселя
                        // P_pixel = alpha * A + beta * B + gamma * C
                        float px = alpha * v1World.getX() + beta * v2World.getX() + gamma * v3World.getX();
                        float py = alpha * v1World.getY() + beta * v2World.getY() + gamma * v3World.getY();
                        float pz = alpha * v1World.getZ() + beta * v2World.getZ() + gamma * v3World.getZ();
                        Vector3f pixelPos = new Vector3f(px, py, pz);

                        // Интерполяция Нормали
                        float nx = alpha * n1World.getX() + beta * n2World.getX() + gamma * n3World.getX();
                        float ny = alpha * n1World.getY() + beta * n2World.getY() + gamma * n3World.getY();
                        float nz = alpha * n1World.getZ() + beta * n2World.getZ() + gamma * n3World.getZ();
                        Vector3f pixelNormal = new Vector3f(nx, ny, nz);

                        // Нормализуем ненулевые нормали
                        if (pixelNormal.length() > 1e-5) {
                            pixelNormal = pixelNormal.normalize();
                        }

                        Vector3f colorVec = pixelShader(
                                barycentric,
                                t1, t2, t3,
                                hasTexture,
                                texture,
                                pixelPos,
                                pixelNormal,
                                lights,
                                settings
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
            boolean hasTexture,
            Texture texture,
            Vector3f pixelPos,
            Vector3f pixelNormal,
            ArrayList<Light> lights,
            RenderSettings settings)
    {
        Vector3f colorVec = settings.fallbackColor;

        if (settings.useTexture && hasTexture) {
            float alpha = barycentric.getX();
            float beta = barycentric.getY();
            float gamma = barycentric.getZ();

            float u = alpha * t1.getX() + beta * t2.getX() + gamma * t3.getX();
            float v = alpha * t1.getY() + beta * t2.getY() + gamma * t3.getY();

            colorVec = texture.getPixel(u, v);
        }

        if (settings.useLighting && lights != null && !lights.isEmpty()) {
            Vector3f lightSum = new Vector3f(0.1F, 0.1F, 0.1F); // Ambient

            for(Light light : lights){
                Vector3f lightVector = light.getPosition().subtract(pixelPos);
                float distance = lightVector.length();
                Vector3f L = lightVector.normalize();
                Vector3f N = pixelNormal;

                float diff = Math.max(0, N.dot(L)); // Diffuse
                float att = light.getAttenuationCoefficient(distance); // Затухание

                Vector3f lightColor = light.getColor();
                float intensity = light.getIntensity();

                Vector3f currentLight = lightColor.multiply(intensity * diff * att);
                lightSum = lightSum.add(currentLight);
            }
            colorVec = multiplyColors(colorVec, lightSum);
        }
        return colorVec;
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
}