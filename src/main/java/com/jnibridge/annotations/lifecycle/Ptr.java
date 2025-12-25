package com.jnibridge.annotations.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation can be used to pass/return a parameter/return value by its pointer.
 *
 * @see Ref
 * @see Shared
 * @see Unique
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ptr {

    /**
     * Specifies how a parameter should be mapped (Java -> C++)
     *
     * <p>
     * The referenced template file must contain valid C/C++ JNI code. During code
     * generation, the placeholders listed below are replaced with context-specific
     * values derived from the Java method and its parameters.
     * </p>
     *
     * <h3>Available placeholders</h3>
     * <ul>
     *   <li><code>${cType}</code> – The C++ type to be mapped</li>
     *   <li><code>${cVar}</code> – An identifying name for the resulting C++ type.</li>
     *   <li><code>${jniVar}</code> – The jni-variable name to be mapped.</li>
     *   <li><code>${cTypeUnderscore}</code> – The full C++ namespace (where :: is replaced with _) </li>
     * </ul>
     *
     * @return the classpath-relative location of the allocation function template
     */
    String inMapping() default "com/jnibridge/mappings/bridged_classes/raw/jnibridge.ptr.in.mapping";

    /**
     * Specifies how a parameter should be mapped (C++ -> Java)
     *
     * <p>
     * The referenced template file must contain valid C/C++ JNI code. During code
     * generation, the placeholders listed below are replaced with context-specific
     * values derived from the Java method and its parameters.
     * </p>
     *
     * <h3>Available placeholders</h3>
     * <ul>
     *   <li><code>${cType}</code> – The C++ type to be mapped</li>
     *   <li><code>${functionCall}</code> – The function-call, which creates an instance of the corresponding cType.</li>
     *   <li><code>${fullJPath}</code> – The complete (semicolon separated) path of the resulting Java class</li>
     * </ul>
     *
     * @return the classpath-relative location of the allocation function template
     */
    String outMapping() default "com/jnibridge/mappings/bridged_classes/raw/jnibridge.ptr.out.mapping";

}
