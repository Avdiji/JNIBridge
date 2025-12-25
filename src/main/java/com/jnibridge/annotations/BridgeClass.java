package com.jnibridge.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that JNI-compatible code should be generated for the annotated class, interface, or enum.
 * <p>
 * This annotation is used to mark types that participate in native code generation (e.g., for C++ bindings).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BridgeClass {

    /**
     * Indicates whether the class to bridge is an enum.
     * @return True if the type to bridge is an enum, else false.
     */
    boolean isEnum() default false;

    /**
     * Native namespace for the mapped class (e.g., "core::math").
     *
     * @return The C++/native namespace in which the generated type should reside.
     */
    String namespace() default "";

    /**
     * Native class or struct name. If left empty, the Java class name will be used as the native name.
     *
     * @return The name to use in native code.
     */
    String name() default "";

    /**
     * Specifies paths to custom JNI source fragments that should be included
     * in the generated JNI output.
     * <p>
     * The contents of each referenced file will be inserted verbatim into the
     * generated JNI source file during code generation. This allows developers
     * to extend or customize the generated JNI code with handwritten logic.
     * </p>
     *
     * @return an array of resource-paths pointing to custom JNI code snippets
     * to be included in the generated JNI file
     */
    String[] customJniCodePaths() default {};
}
