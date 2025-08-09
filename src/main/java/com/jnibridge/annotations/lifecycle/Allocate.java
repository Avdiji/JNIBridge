package com.jnibridge.annotations.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as performing a native resource allocation.
 *
 * <p>Intended for methods that create or acquire a native resource
 * (e.g., memory, file handle, native object) whose lifecycle must be
 * explicitly managed across the JNI boundary.</p>
 *
 * <p>This annotation is typically used in conjunction with
 * {@link Deallocate} to indicate where the corresponding cleanup occurs.</p>
 *
 * @see Deallocate
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Allocate {
}
