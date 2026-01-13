package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.*;
import com.secretsofgreenery.model.Model;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;

import static com.secretsofgreenery.math.Matrix4f.multiplyMatrix4ByVector3;
import static com.secretsofgreenery.math.Point2f.vertexToPoint;
import static com.secretsofgreenery.math.Barycentric.barycentric;

public class RenderEngine {
    public static class RenderSettings {
        public boolean useTexture = true;
        public boolean drawWireframe = false;
        public int fallbackColorARGB = 0xFFAAAAAA; // Цвет, если текстуры нет
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Image texture,
            final RenderSettings settings)
    {
        float[] zBuffer = new float[width * height];
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);

        int[] pixelBuffer = new int[width * height];
        PixelWriter pixelWriter = graphicsContext.getPixelWriter();

        Matrix4f modelMatrix = Matrix4f.identity();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = GraphicConveyor.assembleModelViewProjection(modelMatrix, viewMatrix, projectionMatrix);

        renderModel(mesh, modelViewProjectionMatrix, pixelBuffer, zBuffer, width, height, texture, settings);

        pixelWriter.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixelBuffer, 0, width);
    }

    private static void renderModel(
            final Model mesh,
            final Matrix4f mvpMatrix,
            final int[] pixelBuffer,
            final float[] zBuffer,
            final int width,
            final int height,
            final Image texture,
            final RenderSettings settings)
    {
        final int nPolygons = mesh.getPolygons().size();
        final PixelReader textureReader = (texture != null) ? texture.getPixelReader() : null;
        final int texWidth = (texture != null) ? (int) texture.getWidth() : 0;
        final int texHeight = (texture != null) ? (int) texture.getHeight() : 0;

        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            ArrayList<Integer> vertexIndices = mesh.getPolygons().get(polygonInd).getVertexIndices();
            ArrayList<Integer> textureVertexIndices = mesh.getPolygons().get(polygonInd).getTextureVertexIndices();

            boolean hasTexture = settings.useTexture
                    && texture != null
                    && textureVertexIndices != null
                    && textureVertexIndices.size() == 3;

            Vector3f v1 = mesh.getVertices().get(vertexIndices.get(0));
            Vector3f v2 = mesh.getVertices().get(vertexIndices.get(1));
            Vector3f v3 = mesh.getVertices().get(vertexIndices.get(2));

            Vector3f v1Screen = multiplyMatrix4ByVector3(mvpMatrix, v1);
            Vector3f v2Screen = multiplyMatrix4ByVector3(mvpMatrix, v2);
            Vector3f v3Screen = multiplyMatrix4ByVector3(mvpMatrix, v3);

            Point2f p1 = vertexToPoint(v1Screen, width, height);
            Point2f p2 = vertexToPoint(v2Screen, width, height);
            Point2f p3 = vertexToPoint(v3Screen, width, height);

            Vector2f t1 = hasTexture ? mesh.getTextureVertices().get(textureVertexIndices.get(0)) : new Vector2f(0,0);
            Vector2f t2 = hasTexture ? mesh.getTextureVertices().get(textureVertexIndices.get(1)) : new Vector2f(0,0);
            Vector2f t3 = hasTexture ? mesh.getTextureVertices().get(textureVertexIndices.get(2)) : new Vector2f(0,0);

            rasterizeTriangle(
                    p1, p2, p3,
                    v1Screen.getZ(), v2Screen.getZ(), v3Screen.getZ(),
                    t1, t2, t3,
                    hasTexture,
                    zBuffer, width, height,
                    pixelBuffer, textureReader, texWidth, texHeight,
                    settings
            );
        }
    }

    private static void rasterizeTriangle(
            Point2f p1, Point2f p2, Point2f p3,
            float z1, float z2, float z3,
            Vector2f t1, Vector2f t2, Vector2f t3,
            boolean hasTexture,
            float[] zBuffer, int width, int height,
            int[] pixelBuffer, PixelReader textureReader, int texWidth, int texHeight,
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

                // Если пиксель внутри треугольника
                if (barycentric.getX() >= 0 && barycentric.getY() >= 0 && barycentric.getZ() >= 0) {

                    // Интерполяция глубины
                    float z = barycentric.getX() * z1 + barycentric.getY() * z2 + barycentric.getZ() * z3;

                    int index = y * width + x;

                    if (z < zBuffer[index]) {
                        int color = pixelShader(
                                barycentric,
                                t1, t2, t3,
                                hasTexture,
                                textureReader, texWidth, texHeight,
                                settings
                        );

                        zBuffer[index] = z;
                        pixelBuffer[index] = color;
                    }
                }
            }
        }
    }

    private static int pixelShader(
            Vector3f barycentric, // alpha, beta, gamma
            Vector2f t1, Vector2f t2, Vector2f t3,
            boolean hasTexture,
            PixelReader textureReader, int texWidth, int texHeight,
            RenderSettings settings)
    {
        int finalColor = settings.fallbackColorARGB;

        if (hasTexture) {
            float alpha = barycentric.getX();
            float beta = barycentric.getY();
            float gamma = barycentric.getZ();

            float u = alpha * t1.getX() + beta * t2.getX() + gamma * t3.getX();
            float v = alpha * t1.getY() + beta * t2.getY() + gamma * t3.getY();

            v = 1 - v;
            u = 1 - u;

            int texX = (int) (u * texWidth);
            int texY = (int) (v * texHeight);

            if (texX < 0) texX = 0;
            if (texX >= texWidth) texX = texWidth - 1;
            if (texY < 0) texY = 0;
            if (texY >= texHeight) texY = texHeight - 1;

            finalColor = textureReader.getArgb(texX, texY);
        }

        return finalColor;
    }
}