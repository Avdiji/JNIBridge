package com.jnibridge;

import com.jnibridge.mapper.TypeMapper;
import com.jnibridge.mapper.primitives.*;
import com.jnibridge.mapper.standard.StringMapper;
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
public class JniBridgeRegistry {

    /**
     * Constructor.
     */
    private JniBridgeRegistry() { }

    // type registry with default mappings...
    private static final Map<Class<?>, Class<? extends TypeMapper>> typeRegistry = new HashMap<>();

    // exception registry...
    public static final Map<String, Class<? extends Throwable>> exceptionRegistry = new HashMap<>();

    // @formatter:off
    static {

        putIntoTypeRegistry(int.class, IntMapper.class);
        putIntoTypeRegistry(int.class, IntMapper.class);
        putIntoTypeRegistry(void.class, VoidMapper.class);
        putIntoTypeRegistry(boolean.class, BoolMapper.class);
        putIntoTypeRegistry(char.class, CharMapper.class);
        putIntoTypeRegistry(double.class, DoubleMapper.class);
        putIntoTypeRegistry(float.class, FloatMapper.class);
        putIntoTypeRegistry(short.class, ShortMapper.class);
        putIntoTypeRegistry(long.class, LongMapper.class);
        putIntoTypeRegistry(String.class, StringMapper.class);

    }
    // @formatter:on

    /**
     * Adds a type-to-mapper association into the registry.
     *
     * @param clazz      the Java class (e.g., {@code int.class}, {@code String.class})
     * @param typeMapper the corresponding {@link TypeMapper} implementation class
     * @throws NullPointerException if either argument is null
     */
    public static void putIntoTypeRegistry(@NotNull final Class<?> clazz, @NotNull final Class<? extends TypeMapper> typeMapper) {
        typeRegistry.put(clazz, typeMapper);
    }

    /**
     * Retrieves the registered {@link TypeMapper} implementation class for the given type.
     *
     * @param clazz the Java class to look up
     * @return the mapped {@link TypeMapper} class, or {@code null} if none registered
     */
    @Nullable
    public static Class<? extends TypeMapper> getMapperForType(Class<?> clazz) {
        return typeRegistry.get(clazz);
    }

    /**
     * Registers a mapping between a C++ exception and a Java exception.
     * <p>
     * When the specified C++ exception is encountered, it will be translated
     * into the provided Java {@link Throwable} type.
     *
     * @param cppException the fully qualified name or identifier of the C++ exception to be mapped.
     * @param javaException the Java exception class that the C++ exception should be translated into.
     */
    @SuppressWarnings("unused")
    public static void registerException(@NotNull final String cppException, @NotNull final Class<? extends Throwable> javaException) {
        exceptionRegistry.put(cppException, javaException);
    }

}
