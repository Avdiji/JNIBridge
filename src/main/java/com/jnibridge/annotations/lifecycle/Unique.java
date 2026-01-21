package com.jnibridge.annotations.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation can be used to pass/return a parameter/return value as a std::unique_ptr.
 *
 * @see Ptr
 * @see Ref
 * @see Shared
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {

    /**
     * Specifies a custom native allocation function template-path (alloc as std::unique_ptr).
     * <p>
     * Only works for functions that have been annotated with {@link Allocate} as well as {@link Unique}.
     * </p>
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
    String allocTemplate() default "com/jnibridge/internals/alloc/alloc.unique.template";

    /**
     * Specifies how a parameter should be mapped (Java -> C++)
     *
     * <p>
     * The referenced template file must contain valid C/C++ JNI code. During code
     * generation, the placeholders listed below are replaced with context-specific
     * values derived from the Java method and its parameters.
     * </p>
     *
     * <h4>Available placeholders</h4>
     * <ul>
     *   <li><code>${cType}</code> – The C++ type to be mapped</li>
     *   <li><code>${cVar}</code> – An identifying name for the resulting C++ type.</li>
     *   <li><code>${jniVar}</code> – The jni-variable name to be mapped.</li>
     *   <li><code>${cTypeUnderscore}</code> – The full C++ namespace (where :: is replaced with _) </li>
     * </ul>
     *
     * @return the classpath-relative location of the allocation function template
     */
    String inMapping() default "com/jnibridge/mappings/bridged_classes/unique/jnibridge.unique.in.mapping";

    /**
     * Specifies how a parameter should be mapped (C++ -> Java)
     *
     * <p>
     * The referenced template file must contain valid C/C++ JNI code. During code
     * generation, the placeholders listed below are replaced with context-specific
     * values derived from the Java method and its parameters.
     * </p>
     *
     * <h4>Available placeholders</h4>
     * <ul>
     *   <li><code>${cType}</code> – The C++ type to be mapped</li>
     *   <li><code>${functionCall}</code> – The function-call, which creates an instance of the corresponding cType.</li>
     *   <li><code>${fullJPath}</code> – The complete (semicolon separated) path of the resulting Java class</li>
     * </ul>
     *
     * @return the classpath-relative location of the allocation function template
     */
    String outMapping() default "com/jnibridge/mappings/bridged_classes/unique/jnibridge.unique.out.mapping";
}
