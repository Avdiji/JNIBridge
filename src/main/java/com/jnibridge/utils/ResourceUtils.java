package com.jnibridge.utils;

import org.jetbrains.annotations.NotNull;

import javax.naming.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Utility class for loading resources from the classpath.
 * <p>
 * This class provides helper methods for reading the contents of resources (such as files in
 * {@code src/main/resources}) as UTF-8.
 * <p>
 * This class is not meant to be instantiated.
 */
public class ResourceUtils {

    // Private constructor to prevent instantiation
    private ResourceUtils() { }

    /**
     * Loads the content of a resource file from the classpath as a {@link String}.
     * <p>
     * The resource is loaded using the current thread's context {@link ClassLoader}, and its content is
     * read as UTF-8 text.
     *
     * @param path the path to the resource (relative to the classpath root), e.g., {@code "templates/codegen.stub"}
     * @return the full text content of the resource as a string
     * @throws NullPointerException     if the resource path is null or the resource cannot be found
     * @throws IllegalArgumentException if an I/O error occurs while reading the resource
     */
    @NotNull
    public static String load(@NotNull final String path) {

        // validate params
        Objects.requireNonNull(path, "Unable to fetch resource: The passed path is null.");

        // validate resource stream
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream resource = classloader.getResourceAsStream(path);
        if (resource == null) {
            throw new NullPointerException(String.format("The fetched resource for '%s' is null", path));
        }

        // read and return content of resource stream
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();


            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            return result.toString();

        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to load resource for '%s'", path), e);
        }

    }
}
