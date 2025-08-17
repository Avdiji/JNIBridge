package com.jnibridge.generator.model.extractor;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.generator.model.MethodInfo;
import com.jnibridge.generator.scanner.MethodScanner;
import com.jnibridge.nativeaccess.IPointer;
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
    @NotNull
    public static ClassInfo extract(@NotNull final Class<?> clazz, @NotNull final List<Class<?>> otherClassesToMap) {
        BridgeClass annotation = clazz.getAnnotation(BridgeClass.class);
        if (annotation == null) {
            throw new IllegalArgumentException(String.format("Class '%s' must be annotated properly in order to be mapped.", clazz.getSimpleName()));
        }

        // sorted set of all the subclasses to be mapped...
        SortedSet<ClassInfo> subclasses = otherClassesToMap.stream()
                .filter(otherClazz -> !otherClazz.equals(clazz))
                .filter(clazz::isAssignableFrom)
                .map(other -> extract(other, otherClassesToMap))
                .collect(Collectors.toCollection(TreeSet::new));

        return ClassInfo.builder()
                .clazz(clazz)
                .nativeNamespace(annotation.namespace())
                .nativeName(annotation.name().isEmpty() ? clazz.getSimpleName() : annotation.name())
                .jName(clazz.getSimpleName())

                .subclasses(subclasses)

                .methodsToMap(extractMethodsToMap(clazz, annotation.namespace()))
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
    @NotNull
    private static List<MethodInfo> extractMethodsToMap(@NotNull final Class<?> clazz, @NotNull final String namespace) {
        Set<Method> allJNIBridgedMethods = MethodScanner.getAllJNIBridgedMethods(clazz);
        return allJNIBridgedMethods.stream()
                .filter(method -> method.getDeclaringClass().equals(clazz))
                .map(method -> MethodInfoExtractor.extract(method, namespace, clazz)).collect(Collectors.toList());
    }


    /**
     * Method extract the full C++ type from the given class.
     *
     * @param clazz The class to extract the full C++ type from.
     * @return The full C++ type of the passed class as a string.
     * @throws RuntimeException If the class has not been annotated properly.
     */
    public static String extractClassCType(@NotNull final Class<?> clazz) {
        if(!IPointer.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(String.format("Class '%s' must implement IPointer.", clazz.getSimpleName()));
        }

        BridgeClass annotation = clazz.getAnnotation(BridgeClass.class);
        if (annotation == null) {
            throw new RuntimeException(String.format("Class '%s' must be annotated properly, for it to be mapped properly", clazz.getSimpleName()));
        }

        String namespace = annotation.namespace();
        String name = annotation.name();

        String result = namespace.isEmpty() ? "" : namespace + "::";
        result += name.isEmpty() ? clazz.getSimpleName() : name;
        return result;
    }
}
