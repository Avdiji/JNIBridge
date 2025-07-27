package com.jnibridge.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that JNI-compatible code should be generated for the annotated class, interface or enum.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BridgeClass {

    /**
     * Native namespace for the mapped class (e.g., "core::math").
     */
    String namespace() default "";

    /**
     * Native class or struct name. If empty, uses the Java class name.
     */
    String name() default "";
}
