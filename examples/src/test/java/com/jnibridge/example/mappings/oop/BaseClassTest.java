package com.jnibridge.example.mappings.oop;

import com.jnibridge.examples.mappings.oop.A;
import com.jnibridge.examples.mappings.oop.B;
import com.jnibridge.examples.mappings.oop.BaseClass;
import com.jnibridge.examples.mappings.oop.Color;
import com.jnibridge.exception.JniBridgeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BaseClassTest {

    static {
        String dllPath = System.getProperty("user.dir") + "/build/jni/JNIBridgeExamples.dll";
        System.load(dllPath);
    }

    @Test
    public void testEnumMapping() {
        B b = new B();

        assertEquals(Color.Red, b.getColor());
        b.setColor(Color.Blue);
        assertEquals(Color.Blue, b.getColor());
        b.close();
    }

    @Test
    public void testGetThisRef() {
        BaseClass baseClass = new BaseClass();
        BaseClass thisRef = baseClass.getThisRef();

        baseClass.close();
        assertThrows(JniBridgeException.class, thisRef::getString);
    }

    @Test
    public void testPrintFromOther() {
        B b = new B();
        assertThrows(JniBridgeException.class, () ->BaseClass.printString(b));
        b.close();
    }

    @Test
    public void testExceptionHandling() {
        try (BaseClass baseClass = new BaseClass()) {
            baseClass.throwNestedError();
        } catch (IllegalStateException e) {
            assertEquals("outer error", e.getMessage());
            assertEquals("inner error", e.getCause().getMessage());
        }
    }

    @Test
    public void testIllegalOperation() {
        BaseClass baseClass = new BaseClass();
        baseClass.close();
        assertThrows(JniBridgeException.class, baseClass::getString);
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
