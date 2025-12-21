package com.jnibridge.example.mappings.diamond;

import com.jnibridge.examples.mappings.diamond.Bat;
import org.junit.jupiter.api.Test;

public class BatTest {

    static {
        String dllPath = System.getProperty("user.dir") + "/build/jni/JNIBridgeExamples.dll";
        System.load(dllPath);
    }

    @Test
    public void testEat() {
        Bat bat = new Bat();
        bat.eat();
        bat.close();
    }

}
