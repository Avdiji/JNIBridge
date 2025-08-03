package com.jnibridge.mapper;

import com.jnibridge.mapper.primitives.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A central registry for associating Java types (e.g., primitives or classes) with their
 * corresponding {@link TypeMapper} implementations.
 * <p>
 * This registry is used to support dynamic or static lookup of type mapping logic
 * for native bridging scenarios.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * MappingRegistry.putIntoRegistry(String.class, StringMapper.class);
 * }</pre>
 */
public class GlobalMapperRegistry {

    private GlobalMapperRegistry() { }

    // @formatter:off
    // registry with default mappings...
    private static final Map<Class<?>, Class<? extends TypeMapper>> registry = new HashMap<>();
    static {

        registry.put(int.class, IntMapper.class);
        registry.put(void.class, VoidMapper.class);
        registry.put(boolean.class, BoolMapper.class);
        registry.put(char.class, CharMapper.class);
        registry.put(double.class, DoubleMapper.class);
        registry.put(float.class, FloatMapper.class);
        registry.put(short.class, ShortMapper.class);
        registry.put(long.class, LongMapper.class);
        registry.put(String.class, StringMapper.class);

    }
    // @formatter:on

    /**
     * Adds a type-to-mapper association into the registry.
     *
     * @param clazz      the Java class (e.g., {@code int.class}, {@code String.class})
     * @param typeMapper the corresponding {@link TypeMapper} implementation class
     * @throws NullPointerException if either argument is null
     */
    public static void putIntoRegistry(@NotNull final Class<?> clazz, @NotNull final Class<? extends TypeMapper> typeMapper) {
        registry.put(clazz, typeMapper);
    }

    /**
     * Retrieves the registered {@link TypeMapper} implementation class for the given type.
     *
     * @param clazz the Java class to look up
     * @return the mapped {@link TypeMapper} class, or {@code null} if none registered
     */
    @Nullable
    public static Class<? extends TypeMapper> getMapperFor(Class<?> clazz) {
        return registry.get(clazz);
    }

}
