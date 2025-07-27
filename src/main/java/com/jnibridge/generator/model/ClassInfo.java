package com.jnibridge.generator.model;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.InheritableMetadata;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a model of a Java class annotated with {@link BridgeClass}, used in JNI-compatible
 * code generation.
 */
@Getter
public class ClassInfo {

    // metadata
    private String namespace;
    private String name;

    // inheritable metadata
    private final Set<String> includes;
    private final Set<String> customJNICodePaths;

    /**
     * Constructs a {@code ClassInfo} from the given class, extracting all metadata from its {@link BridgeClass}
     * annotation and recursively resolving inherited metadata.
     *
     * @param clazz the class to process, which must be annotated with {@link BridgeClass}
     * @throws IllegalArgumentException if the class is not properly annotated or inheritance is invalid
     */
    public ClassInfo(@NotNull final Class<?> clazz) {
        includes = new HashSet<>();
        customJNICodePaths = new HashSet<>();

        initMetadata(clazz);
        initInheritableMetadata(clazz);
    }

    /**
     * Initializes core metadata fields from the given class's {@link BridgeClass} annotation.
     *
     * @param clazz the Java class annotated with {@link BridgeClass}
     */
    private void initMetadata(@NotNull final Class<?> clazz) {
        final BridgeClass annotation = clazz.getAnnotation(BridgeClass.class);

        namespace = annotation.namespace();
        name = annotation.name().isEmpty() ? clazz.getSimpleName() : annotation.name();
    }

    /**
     * Initializes inheritable metadata from the provided class's metadata,
     * and recursively resolves inherited metadata from any classes listed in {@link InheritableMetadata#inheritFrom()}.
     *
     * @param clazz the class to extract metadata from
     */
    private void initInheritableMetadata(@NotNull final Class<?> clazz) {
        final BridgeClass annotation = clazz.getAnnotation(BridgeClass.class);
        InheritableMetadata metadata = annotation.metadata();

        includes.addAll(Arrays.asList(metadata.includes()));
        customJNICodePaths.addAll(Arrays.asList(metadata.customJNICodePaths()));

        Set<Class<?>> visited = new HashSet<>();
        visited.add(clazz);

        for (Class<?> inheritClass : metadata.inheritFrom()) {
            initInheritableMetadata(inheritClass, visited);
        }
    }

    /**
     * Recursively processes inherited metadata from the given class, avoiding cycles and ensuring
     * all referenced classes are annotated with {@link BridgeClass}.
     *
     * @param inheritClass the class to process
     * @param visited      a set of classes already visited to prevent circular inheritance
     * @throws IllegalArgumentException if an inherited class is missing the {@link BridgeClass} annotation
     */
    private void initInheritableMetadata(@NotNull final Class<?> inheritClass, @NotNull final Set<Class<?>> visited) {
        // prevent recursion on circular inheritance
        if (!visited.add(inheritClass)) { return; }

        // must be annotated
        BridgeClass inheritedAnnotation = inheritClass.getAnnotation(BridgeClass.class);
        if (inheritedAnnotation == null) {
            throw new IllegalArgumentException(String.format("Cannot inherit metadata from class '%s': @BridgeClass annotation missing.", inheritClass.getName()));
        }

        // add inherited metadata
        InheritableMetadata metadata = inheritedAnnotation.metadata();
        includes.addAll(Arrays.asList(metadata.includes()));
        customJNICodePaths.addAll(Arrays.asList(metadata.customJNICodePaths()));

        for (Class<?> next : metadata.inheritFrom()) { initInheritableMetadata(next, visited); }
    }

}
