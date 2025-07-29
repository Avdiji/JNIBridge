package com.jnibridge.generator.model.extractor;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.annotations.InheritableMetadata;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.MethodInfo;
import com.jnibridge.generator.scanner.MethodScanner;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extracts a {@link ClassInfo} model from a Java class annotated with {@link BridgeClass}.
 */
public class ClassInfoExtractor {

    private ClassInfoExtractor() { }

    /**
     * Extracts a fully resolved {@link ClassInfo} object from a class annotated with {@link BridgeClass}.
     *
     * @param clazz the class to extract metadata and method info from
     * @return the extracted {@link ClassInfo} representation
     * @throws IllegalArgumentException if the class is not annotated with {@link BridgeClass}
     */
    public static ClassInfo extract(@NotNull final Class<?> clazz) {
        BridgeClass annotation = clazz.getAnnotation(BridgeClass.class);
        if (annotation == null) {
            throw new IllegalArgumentException(String.format("Class '%s' must be annotated properly in order to be mapped.", clazz.getSimpleName()));
        }

        return ClassInfo.builder()
                .clazz(clazz)
                .nativeNamespace(annotation.namespace())
                .nativeName(annotation.name().isEmpty() ? clazz.getSimpleName() : annotation.name())
                .jName(clazz.getSimpleName())

                .metadata(extractInheritableMetadataInfo(clazz))
                .methodsToMap(extractMethodsToMap(clazz))
                .build();
    }

    /**
     * Extracts all native methods from the given class that are relevant to JNI bridging.
     * Delegates to {@link MethodScanner#getAllJNIBridgedMethods(Class)} and transforms
     * each method into a {@link MethodInfo}.
     *
     * @param clazz the class to scan
     * @return list of {@link MethodInfo} objects representing native methods
     */
    private static List<MethodInfo> extractMethodsToMap(@NotNull final Class<?> clazz) {
        Set<Method> allJNIBridgedMethods = MethodScanner.getAllJNIBridgedMethods(clazz);
        return allJNIBridgedMethods.stream().map(MethodInfoExtractor::extract).collect(Collectors.toList());
    }

    /**
     * Resolves and flattens all inheritable metadata from the given class and its declared inheritance chain.
     * <p>
     * All metadata is accumulated into a {@link ClassInfo.InheritableMetadataInfo} instance.
     *
     * @param clazz the class to resolve metadata for; must be annotated with {@link BridgeClass}
     * @return a flattened metadata info object containing all inherited and declared values
     * @throws IllegalArgumentException if any referenced superclass is missing the {@link BridgeClass} annotation
     */
    private static ClassInfo.InheritableMetadataInfo extractInheritableMetadataInfo(@NotNull final Class<?> clazz) {
        final BridgeClass annotation = clazz.getAnnotation(BridgeClass.class);
        InheritableMetadata metadata = annotation.metadata();

        Set<String> includes = Arrays.stream(metadata.includes()).collect(Collectors.toSet());
        Set<String> customJNICodePaths = Arrays.stream(metadata.customJNICodePaths()).collect(Collectors.toSet());

        ClassInfo.InheritableMetadataInfo metadataInfo = new ClassInfo.InheritableMetadataInfo(includes, customJNICodePaths);

        Set<Class<?>> visited = new HashSet<>();
        visited.add(clazz);

        for (Class<?> inheritClass : metadata.inheritFrom()) {
            extractInheritableMetadataInfo(inheritClass, metadataInfo, visited);
        }

        return metadataInfo;
    }

    /**
     * Recursively accumulates metadata from a class annotated with {@link BridgeClass} and any of its ancestors
     * declared via {@link InheritableMetadata#inheritFrom()}. Cycles in the inheritance tree are safely ignored.
     *
     * @param inheritClass the class whose metadata should be processed
     * @param metadataInfo the mutable container to accumulate resolved metadata into
     * @param visited      a set of already-visited classes used to prevent infinite recursion on circular inheritance
     * @throws IllegalArgumentException if {@code inheritClass} is not annotated with {@link BridgeClass}
     */
    private static void extractInheritableMetadataInfo(@NotNull final Class<?> inheritClass, @NotNull final ClassInfo.InheritableMetadataInfo metadataInfo, @NotNull final Set<Class<?>> visited) {
        // prevent recursion on circular inheritance
        if (!visited.add(inheritClass)) { return; }

        // must be annotated
        BridgeClass inheritedAnnotation = inheritClass.getAnnotation(BridgeClass.class);
        if (inheritedAnnotation == null) {
            throw new IllegalArgumentException(String.format("Cannot inherit metadata from class '%s': @BridgeClass annotation missing.", inheritClass.getName()));
        }

        // add inherited metadata
        InheritableMetadata metadata = inheritedAnnotation.metadata();
        metadataInfo.getIncludes().addAll(Arrays.asList(metadata.includes()));
        metadataInfo.getCustomJNICodePaths().addAll(Arrays.asList(metadata.customJNICodePaths()));

        for (Class<?> next : metadata.inheritFrom()) { extractInheritableMetadataInfo(next, metadataInfo, visited); }
    }
}
