package com.jnibridge.generator;

import com.jnibridge.utils.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Generator {

    private Generator() { }

    public static void generateJNIInterface(@NotNull final String... classes) {

        if(!ClassUtils.validateClassPatterns(classes)) {
            throw new IllegalArgumentException("The passed class-patterns are invalid.");
        }

        List<Class<?>> classesToBeMapped;
        try {
            classesToBeMapped = ClassUtils.loadClasses(classes);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("One of the passed classes have not been found", e);
        }

        // TODO iterate through each class and map it accordingly...

    }

}
