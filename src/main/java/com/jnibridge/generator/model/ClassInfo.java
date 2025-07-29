package com.jnibridge.generator.model;

import com.jnibridge.annotations.BridgeClass;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Represents a model of a Java class annotated with {@link BridgeClass}, used in JNI-compatible
 * code generation.
 */
@Getter
@Builder
public class ClassInfo {

    // the class to be mapped
    private final Class<?> clazz;

    // metadata
    private final String nativeNamespace;
    private final String nativeName;
    private final String jName;

    // inheritable metadata
    private final InheritableMetadataInfo metadata;

    // all the native methods (also the inherited ones).
    private final List<MethodInfo> methodsToMap;

    /**
     * Represents a model of the {@link com.jnibridge.annotations.InheritableMetadata} annotation, used in JNI-compatible code generation.
     */
    @Getter
    @RequiredArgsConstructor
    public static class InheritableMetadataInfo {
        private final Set<String> includes;
        private final Set<String> customJNICodePaths;
    }
}
