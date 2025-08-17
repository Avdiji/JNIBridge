package com.jnibridge.example.mappings.oop;

import com.jnibridge.examples.mappings.oop.BaseClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseClassTest {

    static {
        String dllPath = System.getProperty("user.dir") + "/build/jni/JNIBridgeExamples.dll";
        System.load(dllPath);
    }

    @Test
    public void testAllocation() {
        BaseClass baseClass = new BaseClass();
        baseClass.destruct();
    }

    @Test
    public void testGetString() {
        BaseClass baseClass = new BaseClass();
        assertEquals("BaseClass-String", baseClass.getString());
    }

}
