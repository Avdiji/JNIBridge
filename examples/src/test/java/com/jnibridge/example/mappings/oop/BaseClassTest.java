package com.jnibridge.example.mappings.oop;

import com.jnibridge.examples.mappings.oop.AnotherClass;
import com.jnibridge.examples.mappings.oop.BaseClass;
import org.junit.jupiter.api.Test;

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

    @Test void testAnotherAllocation() {
        AnotherClass anotherClass = new AnotherClass();
        anotherClass.destruct();
    }

}
