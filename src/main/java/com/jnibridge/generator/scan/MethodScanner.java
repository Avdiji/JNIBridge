package com.jnibridge.generator.scan;

import com.jnibridge.annotations.BridgeClass;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for scanning Java classes for native methods intended for JNI bridging.
 * This class focuses on identifying native methods declared directly in a class or inherited
 * from classes annotated with {@link BridgeClass}.
 */
public class MethodScanner {

    private MethodScanner() {}

    /**
     * Retrieves all native methods relevant to the JNI bridge from the specified class.
     * <p>
     * This includes:
     * <ul>
     *     <li>All native methods declared directly in the class (regardless of visibility).</li>
     *     <li>All non-static native methods inherited from superclasses that are annotated with {@link BridgeClass}.</li>
     * </ul>
     *
     * @param clazz The class to scan.
     * @return A set of native {@link Method} objects used in JNI bridging.
     */
    public static Set<Method> getAllJNIBridgedMethods(@NotNull final Class<?> clazz) {
        Set<Method> result = new HashSet<>();
        result.addAll(getInheritedNativeMethods(clazz));
        result.addAll(getDeclaredNativeMethods(clazz));

        return result.stream()
                .filter(method -> method.getDeclaringClass().isAnnotationPresent(BridgeClass.class))
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves all native methods declared directly in the specified class.
     *
     * @param clazz The class whose declared methods will be inspected.
     * @return A set of native {@link Method} objects declared in the class.
     */
    private static Set<Method> getDeclaredNativeMethods(@NotNull final Class<?> clazz) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        return Arrays.stream(declaredMethods)
                .filter(method -> Modifier.isNative(method.getModifiers()))
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves all non-static native methods inherited from superclasses.
     * Only public methods are considered, as returned by {@link Class#getMethods()}.
     *
     * @param clazz The class whose inherited methods will be inspected.
     * @return A set of inherited native {@link Method} objects superclasses.
     */
    private static Set<Method> getInheritedNativeMethods(@NotNull final Class<?> clazz) {
        Method[] allMethods = clazz.getMethods();

        return Arrays.stream(allMethods)
                .filter(method -> !method.getDeclaringClass().equals(clazz))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> Modifier.isNative(method.getModifiers()))
                .collect(Collectors.toSet());
    }


}
