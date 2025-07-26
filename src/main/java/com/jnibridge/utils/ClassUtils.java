package com.jnibridge.utils;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.*;
import java.util.stream.Collectors;


public class ClassUtils {

    private ClassUtils() { }

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

        List<Class<?>> filtered = allClasses.stream()
                .filter(c -> !c.isAnonymousClass() && !c.isSynthetic())
                .filter(c -> c.getPackageName().startsWith(packageName))
                .collect(Collectors.toList());

        System.out.println(">> Found in package '" + packageName + "': " + filtered.size());

        return filtered;
    }


    /**
     * Validate the passed class-patterns.
     *
     * @param classPatterns The class-patterns to be validated.
     * @return True if the class-patterns are valid, otherwise false.
     */
    public static boolean validateClassPatterns(String... classPatterns) {
        if (classPatterns == null) { return false; }
        if (classPatterns.length == 0) { return false; }


        for (final String pattern : classPatterns) {
            if (pattern == null || pattern.isBlank()) { return false; }
        }

        return true;
    }

}
