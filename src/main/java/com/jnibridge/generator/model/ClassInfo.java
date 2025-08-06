package com.jnibridge.generator.model;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.BridgeMetadata;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Represents a model of a Java class annotated with {@link BridgeClass}, used in JNI-compatible
 * code generation.
 */
@Getter
@Builder
public class ClassInfo {

    // @formatter:off

    // the class to be mapped
    @NonNull  private final Class<?> clazz;

    // metadata
    @NonNull private final String nativeNamespace;
    @NonNull private final String nativeName;
    @NonNull private final String jName;

    // inheritable metadata
    @NonNull private final InheritableMetadataInfo metadata;

    // all the native methods (also the inherited ones).
    @NonNull private final List<MethodInfo> methodsToMap;

    // @formatter:on


    /**
     * Represents a model of the {@link BridgeMetadata} annotation, used in JNI-compatible code generation.
     */
    @Getter
    @RequiredArgsConstructor
    public static class InheritableMetadataInfo {

        // @formatter:off
        @NonNull  private final Set<String> includes;
        @NonNull private final Set<String> customJNICodePaths;
        // @formatter:on
    }
}
