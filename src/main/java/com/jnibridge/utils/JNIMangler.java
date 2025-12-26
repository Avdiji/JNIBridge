package com.jnibridge.utils;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

/**
 * Utility class to support jni-mangling operations.
 */
public class JNIMangler {

    private JNIMangler() { }

    /**
     * @param method The method to get the mangled version of.
     * @return The JNI-mangled method name for a given Java Method.
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
        final String mangledMethodName = method.getName().replace("_", "_1");

        final String prefix = getMangledClassDescriptor(method.getDeclaringClass());
        final String postfix = methodDescriptor.isEmpty() ?
                mangledMethodName + "__" :
                mangledMethodName + "__" + methodDescriptor;

        return String.format("%s_%s", prefix, postfix);
    }

    /**
     * @param clazz The class to get the mangled version of.
     * @return A mangled String representation of the path of the class.
     */
    public static String getMangledClassDescriptor(@NotNull final Class<?> clazz) {
        String classDescriptor = "Java_" + clazz.getName();

        classDescriptor = classDescriptor
                // mangling as described in https://docs.oracle.com/en/java/javase/17/docs/specs/jni/design.html?
                .replace(".", "_")
                .replace("$", "_00024");

        return classDescriptor;
    }
}
