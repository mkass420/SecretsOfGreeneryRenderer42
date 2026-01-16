package com.secretsofgreenery.render_engine;

import com.secretsofgreenery.math.Vector3f;
import javafx.scene.paint.Color;

public class ColorUtils {

    // JavaFX Color -> Vector3f
    public static Vector3f colorToVector(Color c){
        return new Vector3f((float)c.getRed(), (float)c.getGreen(), (float)c.getBlue());
    }

    // Vector3f -> int ARGB
    public static int vectorToInt(Vector3f v){
        // Ограничиваем диапазон [0..1]
        float r = Math.min(1.0f, Math.max(0.0f, v.getX()));
        float g = Math.min(1.0f, Math.max(0.0f, v.getY()));
        float b = Math.min(1.0f, Math.max(0.0f, v.getZ()));

        // Первый байт - прозрачность - всегда 255, цвета из float [0,1] переводим в int [0, 255]
        return  (0xFF << 24) |
                ((int) (r * 0xFF) << 16) |
                ((int) (g * 0xFF) << 8) |
                ((int) (b * 0xFF));
    }

    // c1(r1, g1, b1), c2(r2, g2, b2) -> c(r1*r2, g1*g2, b1*b2)
    // Нужен для расчета освещения (особенно цветного)
    public static Vector3f multiplyColors(Vector3f c1, Vector3f c2){
        return new Vector3f(
                c1.getX() * c2.getX(),
                c1.getY() * c2.getY(),
                c1.getZ() * c2.getZ()
                );
    }
}