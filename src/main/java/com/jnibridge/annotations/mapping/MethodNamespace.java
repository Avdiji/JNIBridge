package com.jnibridge.annotations.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies an alternative native namespace for the annotated <code>method</code>.
 * <p>
 * This annotation is useful when the method should be placed in a different namespace
 * in the generated native code (e.g., C++).
 * <p>
 * Using this annotation results in completely disregarding the class-namespace.
 *
 * <p><b>Example:</b>
 * <pre>
 * {@literal @}Namespace(nativeNamespace = "core::internal")
 *  public native void setup();
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodNamespace {

    /**
     * The fully qualified native namespace for the method (e.g., C++ namespace).
     *
     * @return The native namespace to associate with this method.
     */
    String value();
}
