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
        baseClass.destruct();
    }

    @Test
    public void testGetAString() {
        A a = new A();
        System.out.println(a.getString());
        a.destruct();
    }

    @Test
    public void testGetBString() {
        B b = new B();
        System.out.println(b.getString());
        b.destruct();
    }

}
