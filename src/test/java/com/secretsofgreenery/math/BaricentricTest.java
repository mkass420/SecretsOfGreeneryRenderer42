package com.secretsofgreenery.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaricentricTest {

    @Test
    void testBaricentric(){
        Vector2f vtA = new Vector2f(1, 2);
        Vector2f vtB = new Vector2f(2, -1);
        Vector2f vtC = new Vector2f(-1, 1);
        Vector2f texture = new Vector2f(6, 5);

        Vector3f result = Baricentric.solveEquation(vtA, vtB, vtC, texture);

        float x = 4;
        float y = -1.0F / 3.0F;
        float z = -8.0F / 3.0F;
        Vector3f expected = new Vector3f(x, y, z);

        assertEquals(result, expected);
    }
}
