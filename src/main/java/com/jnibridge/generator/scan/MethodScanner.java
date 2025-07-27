package com.jnibridge.generator.scan;

import com.jnibridge.annotations.BridgeClass;
import com.jnibridge.nativeaccess.IPointer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodScanner {


    /**
     * Scans the given class for native methods eligible for JNI binding.
     * <p>
     * The class must:
     * <ul>
     *   <li>Implement the {@link IPointer} interface</li>
     *   <li>Be annotated with {@link BridgeClass}</li>
     * </ul>
     * This method returns all native methods declared in the class, excluding the {@code destruct()} method
     * defined in {@link IPointer}.
     *
     * @param clazz the class to scan, which must implement {@link IPointer} and be annotated with {@link BridgeClass}
     * @return a list of native methods declared in the class, excluding {@code destruct()}
     */
    public static List<Method> scanNativeMethods(@NotNull final Class<? extends IPointer> clazz) {
        // validate passed class
        if (!clazz.isAnnotationPresent(BridgeClass.class)) {
            throw new IllegalArgumentException("The passed class must be annotated for JNI-Mapping.");
        }

        // scan all native functions (except for destruct).
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> Modifier.isNative(method.getModifiers()))
                .filter(method -> !method.getName().equals("destruct"))
                .collect(Collectors.toList());
    }
}
