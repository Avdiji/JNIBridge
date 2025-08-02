package com.jnibridge.utils;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

/**
 * Utility class to support jni-mangling operations.
 */
public class JNIMangler {

    private JNIMangler() {}

    /**
     * Returns the JNI-mangled method name for a given Java Method.
     */
    public static String getMangledMethodDescriptor(@NotNull final Method method) {

        String methodDescriptor = Type.getMethodDescriptor(method);
        methodDescriptor = methodDescriptor.substring(0, methodDescriptor.indexOf(')') + 1);

        methodDescriptor = methodDescriptor
                // mangling as described in https://docs.oracle.com/en/java/javase/17/docs/specs/jni/design.html?
                .replace("_", "_1")     // Escape underscores
                .replace("/", "_")      // Package separator
                .replace(";", "_2")     // End of object type
                .replace("[", "_3")     // Arrays

                .replace("(", "")       // Remove parentheses
                .replace(")", "");      // Remove parentheses

        // return methodName__descriptor (if descriptor is not empty)
        String mangledMethodName = method.getName().replace("_", "_1");
        return methodDescriptor.isEmpty() ?
                mangledMethodName :
                mangledMethodName + "__" + methodDescriptor;
    }
}
