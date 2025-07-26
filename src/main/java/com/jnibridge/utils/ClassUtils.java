package com.jnibridge.utils;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.nativeaccess.IPointer;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.*;
import java.util.stream.Collectors;


public class ClassUtils {

    private ClassUtils() { }

    /**
     * @param classPatterns The class-patterns to be mapped.
     * @return A List of Classes, from which JNI layers shall be created.
     */
    public static List<Class<? extends IPointer>> getClassesToMap(@NotNull final String... classPatterns) {

        // validate the class-patterns
        if (!ClassUtils.validateClassPatterns(classPatterns)) {
            throw new IllegalArgumentException("The passed class-patterns are invalid.");
        }

        try {
            List<Class<?>> loadedClasses = ClassUtils.loadClasses(classPatterns);

            //noinspection unchecked <- stream filters all classes that implement IPointer and are annotated to be mapped.
            return loadedClasses.stream()
                    .filter(IPointer.class::isAssignableFrom)
                    .filter(clazz -> clazz.isAnnotationPresent(BridgeClass.class))

                    .map(clazz -> (Class<? extends IPointer>) clazz)
                    .collect(Collectors.toList());

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("One of the passed classes have not been found", e);
        }
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
    public static List<Class<?>> loadClasses(String... classPatterns) throws ClassNotFoundException {
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
     * Scans the given package name for all non-anonymous and non-synthetic classes.
     *
     * @param packageName package to scan.
     * @return list of matching classes.
     */
    private static List<Class<?>> scanPackage(String packageName) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName, contextClassLoader))
                .setScanners(new SubTypesScanner(false))
                .addClassLoader(contextClassLoader)
        );

        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

        return allClasses.stream()
                .filter(c -> !c.isAnonymousClass() && !c.isSynthetic())
                .filter(c -> c.getPackage().getName().startsWith(packageName))
                .collect(Collectors.toList());
    }

    /**
     * Validate the passed class-patterns.
     *
     * @param classPatterns The class-patterns to be validated.
     * @return True if the class-patterns are valid, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validateClassPatterns(String... classPatterns) {
        if (classPatterns == null) { return false; }
        if (classPatterns.length == 0) { return false; }


        for (final String pattern : classPatterns) {
            if (pattern == null || pattern.isEmpty()) { return false; }
        }

        return true;
    }

}
