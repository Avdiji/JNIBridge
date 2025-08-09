package com.jnibridge.annotations.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as performing a native resource deallocation.
 *
 * <p>Intended for methods that release or free a native resource
 * (e.g., memory, file handle, native object) previously obtained
 * through a method annotated with {@link Allocate}.</p>
 *
 * <p>This annotation can help code generation, static analysis, or runtime
 * tooling track resource lifecycles across the JNI boundary and ensure
 * proper cleanup.</p>
 *
 * @see Allocate
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Deallocate {
}
