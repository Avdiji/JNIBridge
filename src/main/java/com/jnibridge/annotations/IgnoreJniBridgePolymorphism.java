package com.jnibridge.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be ignored by the JNI bridge generation process.
 * <p>
 * Methods annotated with {@code @JniBridgeIgnore} will not be considered
 * when generating native bindings or mappings between Java and JNI.
 * This is useful for helper methods, internal logic, or APIs that should
 * not be exposed to native code.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreJniBridgePolymorphism {}
