package com.jnibridge.example.mappings.simple;

import com.jnibridge.examples.mappings.simple.SimpleStaticMappings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class SimpleStaticMappingsTest {

    static {
        String dllPath = System.getProperty("user.dir") + "/build/jni/JNIBridgeExamples.dll";
        System.load(dllPath);
    }


    @Test
    public void testNativeVoidFunction() {
        assertDoesNotThrow(SimpleStaticMappings::nativeVoidFunction);
    }

    @Test
    public void testNativeNumberFunctions() {
        assertEquals(11, SimpleStaticMappings.nativeIncrementInt(10));
        assertEquals(11, SimpleStaticMappings.nativeIncrementShort((short) 10));
        assertEquals(11, SimpleStaticMappings.nativeIncrementLong(10));

        assertEquals(11f, SimpleStaticMappings.nativeIncrementFloat(10f));
        assertEquals(11f, SimpleStaticMappings.nativeIncrementDouble(10f));
    }

    @Test
    public void testNativeBooleanFunction() {
        assertTrue(SimpleStaticMappings.isTrue(true));
        assertFalse(SimpleStaticMappings.isTrue(false));
    }

    @Test
    public void testNativeCharFunction() {
        assertEquals('a' + 1, SimpleStaticMappings.getNextChar('a'));
    }

    @Test
    public void testNativeStringFunction() {
        final String testString = "Message";
        assertEquals("Funny " + testString, SimpleStaticMappings.getFunnyString(testString));
    }

}
