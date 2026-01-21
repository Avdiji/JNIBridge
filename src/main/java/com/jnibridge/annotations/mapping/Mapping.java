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
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
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
     * Getter for the jni mapping template paths.
     *
     * @return The mapping template paths.
     */
    MappingTemplate templates();

    /**
     * Annotation specifies input/output/cleanup template paths, as well as template parameter types.
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface MappingTemplate {

        /**
         * Declares the C++ template arguments of {@link #cType()} in left-to-right order.
         *
         * <p>This is used when {@link #cType()} denotes a C++ template instantiation such as. </p>
         *
         * <p>These values are consumed by the code generator and exposed in mapping templates via:
         * <ul>
         *   <li>{@code ${cTemplateType_0}}, {@code ${cTemplateType_1}}, ...</li>
         *   <li>{@code ${cTemplateTypeUnderscore_0}}, {@code ${cTemplateTypeUnderscore_1}}, ...</li>
         * </ul>
         *
         * <h4>When required</h4>
         * <p>This attribute is required whenever {@link #cType()} represents a templated type
         * and the mapping templates reference template placeholders (e.g. {@code ${cTemplateType_0}}).
         * If omitted in such cases, code generation will fail or produce invalid code.</p>
         *
         * <h4>Example</h4>
         * <pre>{@code
         * @Mapping(
         *   cType = "std::optional<com::jnibridge::Foo>",
         *   cTemplateArgumentTypes = { "com::jnibridge::Foo" },
         *   ...
         * )
         * }</pre>
         */
        String[] cTemplateArgumentTypes() default {};

        /**
         * Declares the Java type arguments that correspond to the C++ template arguments of {@link #cTemplateArgumentTypes()}.
         *
         * <p>The array index corresponds to the template parameter position and must match {@link #cTemplateArgumentTypes()} exactly
         *
         * <p>These values are consumed by the code generator and exposed in mapping templates via:
         * <ul>
         *   <li>{@code ${fullJTemplatePath_0}}, {@code ${fullJTemplatePath_1}}, ... for JNI slash paths.
         * </ul>
         *
         * <h4>When required</h4>
         * Lookup {@link MappingTemplate#cTemplateArgumentTypes()}
         *
         * <h4>Example</h4>
         * <pre>{@code
         * @Mapping(
         *   cType = "std::optional<com::jnibridge::Foo>",
         *   cTemplateArgumentTypes = { "com::jnibridge::Foo" },
         *   jTemplateArgumentTypes = { "com.jnibridge.Foo.class" },
         *   ...
         * )
         * }</pre>
         *
         * @return Java template argument classes, ordered by parameter position.
         */
        Class<?>[] jTemplateArgumentTypes() default {};


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
}
