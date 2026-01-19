package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.Vector3f;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

public class Texture {
    private final Image image;
    private final Vector3f[] data;
    private final int width;
    private final int height;
    private final String name;

    public Texture(Image image) {
        if (image.getWidth() == 0 || image.getHeight() == 0) throw new IllegalArgumentException("Texture should not be empty");
        this.image = image;
        this.width = (int) image.getWidth();
        this.height = (int) image.getHeight();
        this.data = new Vector3f[width * height];
        this.name = image.getUrl();

        PixelReader reader = image.getPixelReader();

        // Читаем цвет каждого пикселя и конвертируем в наш Vector3f для удобной математики
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Vector3f color = ColorUtils.colorToVector(reader.getColor(x, y));
                data[y * width + x] = color;
            }
        }
    }

    public Vector3f getPixel(float u, float v) {
        // Отбрасываем целую часть (u, v теперь в диапазоне [0, 1]) - используем повторение текстур
        u = u - (float)Math.floor(u);
        v = v - (float)Math.floor(v);

        // Зеркалим текстуры
        //u = 1.0f - u;
        v = 1.0f - v;

        // Конвертируем в координаты на текстуре
        int x = (int) (u * (width - 1));
        int y = (int) (v * (height - 1));

        return data[y * width + x];
    }
}