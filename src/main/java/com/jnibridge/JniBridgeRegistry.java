package com.jnibridge;

import com.jnibridge.mapper.TypeMapper;
import com.jnibridge.mapper.primitives.*;
import com.jnibridge.mapper.standard.filesystem.PathMapper;
import com.jnibridge.mapper.standard.string.StringMapper;
import com.jnibridge.utils.CompareUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
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
    private static final Map<String, Class<? extends Throwable>> exceptionRegistry = new HashMap<>();

    // @formatter:off
    static {

        registerTypeMapper(int.class, IntMapper.class);
        registerTypeMapper(void.class, VoidMapper.class);
        registerTypeMapper(boolean.class, BoolMapper.class);
        registerTypeMapper(char.class, CharMapper.class);
        registerTypeMapper(double.class, DoubleMapper.class);
        registerTypeMapper(float.class, FloatMapper.class);
        registerTypeMapper(short.class, ShortMapper.class);
        registerTypeMapper(long.class, LongMapper.class);
        registerTypeMapper(String.class, StringMapper.class);
        registerTypeMapper(Path.class, PathMapper.class);

    }
    // @formatter:on

    /**
     * Adds a type-to-mapper association into the registry.
     *
     * @param clazz      the Java class (e.g., {@code int.class}, {@code String.class})
     * @param typeMapper the corresponding {@link TypeMapper} implementation class
     * @throws NullPointerException if either argument is null
     */
    public static void registerTypeMapper(@NotNull final Class<?> clazz, @NotNull final Class<? extends TypeMapper> typeMapper) {
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
     * @param cppException  the fully qualified name or identifier of the C++ exception to be mapped.
     * @param javaException the Java exception class that the C++ exception should be translated into.
     */
    @SuppressWarnings("unused")
    public static void registerException(@NotNull final String cppException, @NotNull final Class<? extends Throwable> javaException) {
        exceptionRegistry.put(cppException, javaException);
    }

    /**
     * Method sorts the exception-entries, by their inheritance chain and returns a corresponding Collection.
     *
     * @return The sorted entries of the exception-registry.
     */
    public static LinkedList<Map.Entry<String, Class<? extends Throwable>>> getSortedExceptionEntries() {
        LinkedList<Map.Entry<String, Class<? extends Throwable>>> sortedEntries =
                new LinkedList<>(exceptionRegistry.entrySet());

        sortedEntries.sort(Map.Entry.comparingByValue(
                Comparator.<Class<? extends Throwable>>comparingInt(CompareUtils::depth)
                        .reversed()
                        .thenComparing(Class::getName)
        ));
        return sortedEntries;
    }

}
