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
    public void testVoidFunction() {
        assertDoesNotThrow(SimpleStaticMappings::nativeVoidFunction);
    }





}
