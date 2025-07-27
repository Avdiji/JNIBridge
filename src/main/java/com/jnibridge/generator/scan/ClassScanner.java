package com.jnibridge.generator.scan;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.nativeaccess.IPointer;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassScanner {

    private final List<Class<? extends IPointer>> instanceClasses;
    private final List<Class<?>> utilityClasses;

    public ClassScanner(@NotNull final String... classPatterns) {
        instanceClasses = new ArrayList<>();
        utilityClasses = new ArrayList<>();

        initClassScanner(classPatterns);
    }

    public List<Class<? extends IPointer>> getInstanceClasses() { return instanceClasses; }

    public List<Class<?>> getUtilityClasses() { return utilityClasses; }


    /**
     * Method initializes this ClassScanner.
     *
     * @param classPatterns The class-patterns to be mapped.
     */
    private void initClassScanner(@NotNull final String... classPatterns) {

        // validate the class-patterns
        if (!validateClassPatterns(classPatterns)) {
            throw new IllegalArgumentException("The passed class-patterns are invalid.");
        }

        try {
            List<Class<?>> loadedClasses = loadClasses(classPatterns);

            for (Class<?> clazz : loadedClasses) {
                if (clazz.isAnnotationPresent(BridgeClass.class)) { // <- only map if annotated

                    // implements IPointer = instance class
                    if (IPointer.class.isAssignableFrom(clazz)) {
                        //noinspection unchecked
                        instanceClasses.add((Class<? extends IPointer>) clazz);

                        // only static methods = utilityClass
                    } else if (Arrays.stream(clazz.getDeclaredMethods()).allMatch(m -> Modifier.isStatic(m.getModifiers()) || m.isSynthetic())) {
                        utilityClasses.add(clazz);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("One of the passed classes have not been found", e);
        }
    }

    /**
     * Validate the passed class-patterns.
     *
     * @param classPatterns The class-patterns to be validated.
     * @return True if the class-patterns are valid, otherwise false.
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
     * Loads all classes matching the given class names or package wildcards.
     *
     * @param classPatterns varargs of fully-qualified class names or package patterns ending with {@code .*}
     * @return a list of {@code Class<?>} objects
     *
     * <p>
     * @throws ClassNotFoundException if any specific class name is not found
     */
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
     * Scans the given package name for all non-anonymous and non-synthetic classes (recursively).
     *
     * @param packageName package to scan.
     * @return list of matching classes.
     */
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
            throw new RuntimeException(String.format("Unable to find classes in package '%s'", packageName), e);
        }
    }

}
