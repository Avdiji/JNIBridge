package com.jnibridge.generator.scanner;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.exception.JniBridgeException;
import com.jnibridge.nativeaccess.IPointer;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scans provided class names or packages for classes annotated with {@link BridgeClass}.
 * <p>
 * Only classes that are either utility classes (all-static methods) or implement {@link IPointer}
 * are included for mapping.
 */
@Getter
public class ClassScanner {

    private ClassScanner() {}

    /**
     * Initializes the scanner by loading classes based on the input patterns
     * and filtering them according to the presence of {@link BridgeClass}.
     *
     * @param classPatterns class or package patterns to scan
     *
     * @throws IllegalArgumentException if the classPatterns are invalid.
     * @throws IllegalArgumentException if one of the classes within the pattern can not be found.
     */
    @NotNull
    public static List<Class<?>> getClassesToMap(@NotNull final String... classPatterns) {

        // validate class patterns
        if (!validateClassPatterns(classPatterns)) {
            throw new JniBridgeException("The passed class-patterns are invalid.");
        }

        List<Class<?>> classesToMap = new ArrayList<>();
        try {

            List<Class<?>> loadedClasses = loadClasses(classPatterns);
            for (Class<?> clazz : loadedClasses) {

                // only map annotated classes
                if (clazz.isAnnotationPresent(BridgeClass.class)) {

                    // handle enum-classes
                    final BridgeClass annotation = clazz.getAnnotation(BridgeClass.class);
                    if(annotation.isEnum()) {
                        checkEnumClass(clazz);
                        classesToMap.add(clazz);
                        continue;
                    }

                    // map only classes that extend IPointer, or classes which contain only static functions
                    boolean isIPointer = IPointer.class.isAssignableFrom(clazz);
                    boolean isUtilityClass = Arrays.stream(clazz.getDeclaredMethods()).allMatch(m -> Modifier.isStatic(m.getModifiers()) || m.isSynthetic());
                    boolean containsNativeMethods = Arrays.stream(clazz.getDeclaredMethods()).anyMatch(method -> Modifier.isNative(method.getModifiers()));

                    // implements IPointer = instance class
                    if (isIPointer || (isUtilityClass && containsNativeMethods)) {
                        classesToMap.add(clazz);
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            throw new JniBridgeException("One of the passed classes have not been found", e);
        }

        return classesToMap;
    }

    /**
     * Check whether the enum to be mapped declares the proper methods.
     * @param clazz The enum-class to check.
     */
    private static void checkEnumClass(Class<?> clazz) {
        Method[] declaredMethods = clazz.getDeclaredMethods();

        // Check whether the enum contains a static, fromInt Method.
        boolean fromIntMethodExists = Arrays.stream(declaredMethods)
                .filter(method -> !Modifier.isNative(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .anyMatch(method -> method.getName().equals("fromInt"));

        // Check whether the enum contains a non-static, toInt instance Method.
        boolean toIntExists = Arrays.stream(declaredMethods)
                .filter(method ->  !Modifier.isNative(method.getModifiers()))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .anyMatch(method -> method.getName().equals("toInt"));

        if(!fromIntMethodExists) {
            throw new JniBridgeException("Mapped enums must declare a static,native 'fromInt' method.");
        }

        if(!toIntExists) {
            throw new JniBridgeException("Mapped enums must declare a non-static,native 'toInt' method.");
        }
    }

    /**
     * Validates that the provided class patterns are not null, empty, or malformed.
     *
     * @param classPatterns the class or package patterns to validate
     * @return true if valid, false otherwise
     */
    private static boolean validateClassPatterns(String... classPatterns) {
        if (classPatterns == null) { return false; }
        if (classPatterns.length == 0) { return false; }


        for (final String pattern : classPatterns) {
            if (pattern == null || pattern.isEmpty()) { return false; }
        }

        return true;
    }

    /**
     * Loads classes from the given patterns. Supports exact class names and
     * package patterns ending in {@code .*}.
     *
     * @param classPatterns class names or package patterns
     * @return list of loaded classes
     * @throws ClassNotFoundException if a class cannot be found
     */
    @NotNull
    private static List<Class<?>> loadClasses(String... classPatterns) throws ClassNotFoundException {
        List<Class<?>> loadedClasses = new ArrayList<>();
        for (final String pattern : classPatterns) {

            // add all classes in package.
            if (pattern.endsWith(".*")) {
                String packageName = pattern.substring(0, pattern.length() - 2);
                loadedClasses.addAll(scanPackage(packageName));

            } else {
                loadedClasses.add(Class.forName(pattern));
            }
        }
        return loadedClasses;
    }

    /**
     * Recursively scans a package and returns all non-anonymous, non-synthetic classes within it.
     *
     * @param packageName the base package to scan
     * @return list of matching classes
     */
    @NotNull
    private static List<Class<?>> scanPackage(String packageName) {
        try (ScanResult scanResult = new ClassGraph().enableClassInfo().acceptPackages(packageName).scan()) {

            // get all scanned classes
            Stream<? extends Class<?>> classStream = scanResult.getAllClasses().stream()
                    .map(classInfo -> {
                        try {
                            return classInfo.loadClass();
                        } catch (Throwable ignored) {
                            return null;
                        }
                    });

            // filter all classes in package (recursively)
            return classStream
                    .filter(Objects::nonNull)
                    .filter(clazz -> !clazz.isAnonymousClass())
                    .filter(clazz -> !clazz.isSynthetic())
                    .filter(clazz -> clazz.getPackage().getName().startsWith(packageName))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new JniBridgeException(String.format("Unable to find classes in package '%s'", packageName), e);
        }
    }

}
