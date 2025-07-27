package com.jnibridge.generator.model;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.BridgeMetaData;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public class ClassInfo {

    private final BridgeClass annotation;

    private final boolean isStruct;
    private final String namespace;
    private final String name;

    // metadata
    private final Set<String> includes;
    private final Set<String> customJNICodePaths;

    public ClassInfo(@NotNull final Class<?> clazz) {
        includes = new HashSet<>();
        customJNICodePaths = new HashSet<>();

        annotation = clazz.getAnnotation(BridgeClass.class);

        isStruct = annotation.metadata().isStruct();
        namespace = annotation.namespace();

        final String annotatedName = getAnnotation().name();
        name = annotatedName.isEmpty() ? clazz.getSimpleName() : annotatedName;

        initializeMetadata(clazz);
    }

    private void initializeMetadata(@NotNull final Class<?> clazz) {
        BridgeMetaData metadata = annotation.metadata();

        includes.addAll(Arrays.asList(metadata.includes()));
        customJNICodePaths.addAll(Arrays.asList(metadata.customJNICodePaths()));

        Set<Class<?>> visited = new HashSet<>();
        visited.add(clazz);

        for (Class<?> inheritClass : metadata.inheritFrom()) {
            initializeMetadataFrom(inheritClass, visited);
        }
    }

    private void initializeMetadataFrom(@NotNull final Class<?> inheritClass, @NotNull final Set<Class<?>> visited) {
        // prevent recursion on circular inheritance
        if (!visited.add(inheritClass)) { return; }

        // must be annotated
        BridgeClass inheritedAnnotation = inheritClass.getAnnotation(BridgeClass.class);
        if (inheritedAnnotation == null) {
            throw new IllegalArgumentException(String.format("Cannot inherit metadata from class '%s': @BridgeClass annotation missing.", inheritClass.getName()));
        }

        // add inherited metadata
        BridgeMetaData metadata = inheritedAnnotation.metadata();
        includes.addAll(Arrays.asList(metadata.includes()));
        customJNICodePaths.addAll(Arrays.asList(metadata.customJNICodePaths()));

        for (Class<?> next : metadata.inheritFrom()) { initializeMetadataFrom(next, visited); }
    }

}
