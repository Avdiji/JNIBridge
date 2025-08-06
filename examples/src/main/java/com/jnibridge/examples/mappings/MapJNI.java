package com.jnibridge.examples.mappings;

import com.jnibridge.JNIBridge;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MapJNI {
    public static void main(String[] args) {

        Path outputDir = Paths.get("build/jni"); // <-- real path

        // This is the class or package pattern, do not use in Path
        String packagePattern = "com.jnibridge.examples.mappings.simple.*";

        // Pass both correctly:
        JNIBridge.generateJNIInterface(outputDir, packagePattern);

    }
}
