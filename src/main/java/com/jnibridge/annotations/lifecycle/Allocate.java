package com.jnibridge.annotations.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as performing a native resource allocation.
 *
 * <p>This annotation is typically used in conjunction with
 * {@link Deallocate} to indicate where the corresponding cleanup occurs.</p>
 *
 * @see Deallocate
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Allocate {

    /**
     * Specifies a custom native allocation function template-path (alloc as raw-ptrs).
     *
     * <p>
     * The referenced template file must contain valid C/C++ JNI code. During code
     * generation, the placeholders listed below are replaced with context-specific
     * values derived from the Java method and its parameters.
     * </p>
     *
     * <h4>Available placeholders</h4>
     * <ul>
     *   <li><code>${mangledFuncName}</code> – The JNI-mangled name of the corresponding Java method.</li>
     *   <li><code>${jniParams}</code> – Additional JNI parameters appended to the function signature.</li>
     *   <li><code>${fullJPath}</code> – The fully qualified Java class path (slash-separated).</li>
     *   <li><code>${paramInMapping}</code> – JNI input parameter mapping code.</li>
     *   <li><code>${cType}</code> – The native C++ type to be allocated.</li>
     *   <li><code>${functionCallParams}</code> – C++ constructor arguments derived from JNI parameters.</li>
     * </ul>
     *
     * @return the classpath-relative location of the allocation function template
     */
    String allocTemplate() default "com/jnibridge/internals/alloc/alloc.raw.template";

}
