package com.jnibridge.annotations.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Java type as having a custom mapping to a corresponding C++ and JNI type
 * for use in JNI bridge code generation.
 * <p>
 * This annotation provides metadata for tools that generate native interop code,
 * defining how a Java type is represented in C++ and how data is marshalled
 * between Java, JNI, and native code.
 *
 * <p>Each mapping includes:
 * <ul>
 *   <li>The corresponding C++ type (e.g., {@code int}, {@code std::string})</li>
 *   <li>The JNI type used in native method signatures (e.g., {@code jint}, {@code jstring})</li>
 *   <li>A resource path to a Java-to-C++ transformation template</li>
 *   <li>A resource path to a C++-to-Java transformation template</li>
 * </ul>
 *
 * <strong>Example usage:</strong>
 * <pre>{@code
 * @Mapping(
 *     cType = "int",
 *     jniType = "jint",
 *     inPath = "/templates/integer_in.mapping",
 *     outPath = "/templates/integer_out.mapping"
 * )
 * public class IntMapper implements TypeMapper { }
 * }</pre>
 *
 * <p>This annotation must be applied to classes or interfaces and is retained at runtime
 * for reflective access by code generation tools.
 *
 * @see com.jnibridge.mapper.TypeMapper
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {

    /**
     * Getter for the native type of this mapper.
     *
     * @return The C-Type of the type to be mapped.
     */
    String cType();

    /**
     * Getter for the jni type of this mapper.
     *
     * @return The jni of the type to be mapped.
     */
    String jniType();

    /**
     * Resource path of the mapping-template for incoming (Java -> C++) mappings.
     *
     * @return The resource path to the Java-to-C++ mapping template.
     * This template defines how Java variables and method arguments should be translated into C++ code.
     *
     * <br>
     * <p><strong>Supported placeholders:</strong>
     * <ul>
     *   <li><code>${cType}</code> – the C++ Type to be mapped</li>
     *   <li><code>${jniType}</code> – the JNI Type to be mapped</li>
     *
     *   <li><code>${jniVar}</code> – the name of the JNI variable/parameter</li>
     *   <li><code>${cVar}</code> – the name of the C++ variable receiving the cast or transformation</li>
     *
     *   <li><code>${id}</code> – a method-wide unique identifier (e.g. to create unique custom variable-names).</li>
     * </ul>
     *
     * <p>Example substitution:
     * <pre>{@code
     * int ${cVar} = static_cast<${cType}>(${jniVar});
     * }</pre>
     */
    String inPath();

    /**
     * Resource path of the mapping-template for outgoing (C++ -> Java) mappings.
     *
     * @return The resource path to the C++-to-Java mapping template.
     * This template defines how native C++ values or return types are converted back into Java.
     * <br>
     * <p><strong>Supported placeholders:</strong>
     * <ul>
     *   <li><code>${cType}</code> – the C++ Type to be mapped</li>
     *   <li><code>${jniType}</code> – the JNI Type to be mapped</li>
     *
     *  <li><code>${functionCall}</code> – the C++ function or expression being evaluated</li>
     * </ul>
     *
     * <p>Example substitution:
     * <pre>{@code
     * return static_cast<${jniType}>($functionCall);
     * }</pre>
     */
    String outPath();

    /**
     * Resource path of the mapping-template for cleanup functionality.
     *
     * <br>
     * <p><strong>Supported placeholders:</strong>
     * <ul>
     *   <li><code>${cType}</code> – the C++ Type to be mapped</li>
     *   <li><code>${jniType}</code> – the JNI Type to be mapped</li>
     *
     *   <li><code>${jniVar}</code> – the name of the JNI variable/parameter</li>
     *   <li><code>${cVar}</code> – the name of the C++ variable receiving the cast or transformation</li>
     *
     *   <li><code>${id}</code> – a method-wide unique identifier (e.g. to create unique custom variable-names).</li>
     * </ul>
     */
    String cleanupPath() default "";
}
