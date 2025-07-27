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
 * Utility - Class handles resource loading.
 */
public class ResourceUtils {

    private ResourceUtils() { }

    /**
     * Fetch the content of the resource from the given path (with the corresponding ClassLoader).
     *
     * @param path        The path to the resource.
     * @return The content of the resource as a String.
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
