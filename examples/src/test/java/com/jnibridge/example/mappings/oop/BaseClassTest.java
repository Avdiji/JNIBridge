package com.jnibridge.example.mappings.oop;

import com.jnibridge.examples.mappings.oop.A;
import com.jnibridge.examples.mappings.oop.B;
import com.jnibridge.examples.mappings.oop.BaseClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseClassTest {

    static {
        String dllPath = System.getProperty("user.dir") + "/build/jni/JNIBridgeExamples.dll";
        System.load(dllPath);
    }

    @Test
    public void testGetString() {
        BaseClass baseClass = new BaseClass();
        assertEquals("BaseClass-String", baseClass.getString());
        baseClass.close();
    }

    @Test
    public void testGetAString() {
        A a = new A();
        assertEquals("A-String", a.getString());
        a.close();
    }

    @Test
    public void testGetBString() {
        B b = new B();
        assertEquals("B-String", b.getString());
        b.close();
    }

}
