package com.secretsofgreenery.objreader;

import com.secretsofgreenery.math.Vector3f;
import com.secretsofgreenery.objreader.ObjReader;
import com.secretsofgreenery.objreader.ObjReaderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class ObjReaderTest {

    @Test
    public void testParseVertex01() {
        final ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.01", "1.02", "1.03"));
        final Vector3f result = com.secretsofgreenery.objreader.ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        final Vector3f expectedResult = new Vector3f(1.01f, 1.02f, 1.03f);
        Assertions.assertEquals(result, expectedResult);
    }

    @Test
    public void testParseVertex02() {
        final ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("ab", "o", "ba"));
        try {
            com.secretsofgreenery.objreader.ObjReader.parseVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();

        } catch (com.secretsofgreenery.objreader.ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Failed to parse float value.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseVertex03() {
        final ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0"));
        try {
            com.secretsofgreenery.objreader.ObjReader.parseVertex(wordsInLineWithoutToken, 10);
            Assertions.fail();

        } catch (com.secretsofgreenery.objreader.ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Too few vertex arguments.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseVertex04() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0", "3.0", "4.0"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
        } catch (ObjReaderException exception) {
            String expectedError = "";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }
}