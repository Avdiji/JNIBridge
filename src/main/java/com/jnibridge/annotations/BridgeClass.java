package com.jnibridge.annotations;

import com.jnibridge.annotations.mapping.Mapping;
import com.jnibridge.mapper.TypeMapper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that JNI-compatible code should be generated for the annotated class, interface, or enum.
 * <p>
 * This annotation is used to mark types that participate in native code generation (e.g., for C++ bindings).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BridgeClass {

    /**
     * Indicates whether the class to bridge is an enum.
     * @return True if the type to bridge is an enum, else false.
     */
    boolean isEnum() default false;

    /**
     * Native namespace for the mapped class (e.g., "core::math").
     *
     * @return The C++/native namespace in which the generated type should reside.
     */
    String namespace() default "";

    /**
     * Native class or struct name. If left empty, the Java class name will be used as the native name.
     *
     * @return The name to use in native code.
     */
    String name() default "";

    /**
     * Defines type-to-mapper associations that apply at the class level.
     *
     * <p>
     * These mappings participate in a well-defined resolution hierarchy that determines
     * how Java types are translated into their native representations during code generation.
     * The resolution order is as follows (from highest to lowest priority):
     * </p>
     *
     * <ol>
     *   <li>
     *     {@link com.jnibridge.annotations.mapping.UseMapping} —
     *     takes precedence over all other mapping definitions.
     *   </li>
     *   <li>
     *     {@link MappingEntry} —
     *     applies to the annotated class and overrides globally registered mappings.
     *   </li>
     *   <li>
     *     {@link com.jnibridge.JniBridgeRegistry#registerTypeMapper} —
     *     global fallback mappings used when no more specific configuration is present.
     *   </li>
     * </ol>
     *
     * <p>
     * This mechanism allows fine-grained control over type mapping behavior while still
     * providing sensible global defaults.
     * </p>
     *
     * @return an array of {@link MappingEntry} defining type mappings local to this class
     */
    MappingEntry[] typeMappers() default {};

    /**
     * Specifies paths to custom JNI source fragments that should be included
     * in the generated JNI output.
     * <p>
     * The contents of each referenced file will be inserted verbatim into the
     * generated JNI source file during code generation. This allows developers
     * to extend or customize the generated JNI code with handwritten logic.
     * </p>
     *
     * @return an array of resource-paths pointing to custom JNI code snippets
     * to be included in the generated JNI file
     */
    String[] customJniCodePaths() default {};

    /**
     * Specify custom mappings for this IPointer instance.
     * Mainly used to specify custom in/out mappings of the IPointer instance, setting cType and jniType will have no effect.
     * @return Custom Mappings
     */
    Mapping.MappingTemplate templates() default @Mapping.MappingTemplate(inPath = "", outPath = "");

    /**
     * Declares a class-wide type-to-mapper association used during JNI bridge generation.
     * <p>
     * Each {@code @MappingEntry} defines how a specific Java type should be
     * converted to and from its native representation by associating it with
     * a {@link TypeMapper} implementation.
     * </p>
     *
     * @see TypeMapper
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface MappingEntry {

        /**
         * The Java type that should be mapped.
         *
         * @return the Java class for which a native mapping is defined
         */
        Class<?> type();

        /**
         * The {@link TypeMapper} implementation responsible for converting
         * between the Java type and its native representation.
         *
         * @return the mapper class used for this type
         */
        Class<? extends TypeMapper> mapper();


    }
}
