package com.jnibridge.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies an alternative native name for the annotated method.
 * <p>
 * This annotation is useful when the native method name differs from the Java method name.
 * It allows precise control over symbol generation and JNI binding.
 *
 * <p><b>Example:</b>
 * <pre>
 * {@literal @}Name(nativeName = "initializeEngine")
 *  public native void init();
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {

    /**
     * The name of the method in the native language (e.g., C++).
     *
     * @return The native method name.
     */
    String nativeName();
}
