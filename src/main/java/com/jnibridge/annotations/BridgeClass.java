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
     * Metadata for the class mapping.
     *
     * @return An instance of {@link InheritableMetadata} containing metadata for the class.
     */
    InheritableMetadata metadata();

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
}
