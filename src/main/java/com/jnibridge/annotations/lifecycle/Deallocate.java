package com.jnibridge.annotations.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as performing a native resource deallocation.
 *
 * <p>
 * The annotation ensures proper cleanup to avoid memory leaks.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Deallocate {

    /**
     * Specifies a custom native deallocation function template.
     *
     * <p>
     * The referenced template file must contain valid C/C++ JNI code responsible
     * for releasing the native resources associated with a Java object. During
     * code generation, the placeholders listed below are replaced with
     * context-specific values derived from the Java method.
     * </p>
     *
     * <h4>Available placeholders</h4>
     * <ul>
     *   <li><code>${mangledFuncName}</code> – The JNI-mangled name of the corresponding Java method.</li>
     * </ul>
     *
     * <p>
     * Implementations are expected to fully release all native resources and
     * ensure that the Java object no longer references an invalid native handle.
     * </p>
     *
     * @return the classpath-relative location of the deallocation function template
     */
    String deallocTemplate() default "com/jnibridge/internals/dealloc/dealloc.template";
}
