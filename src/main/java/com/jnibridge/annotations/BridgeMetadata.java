package com.jnibridge.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines metadata that can be inherited by other {@link BridgeClass}-annotated types.
 * <p>
 * This annotation is intended to support reusable, composable metadata for JNI-compatible code generation.
 * It allows specification of native includes, JNI source paths, and metadata inheritance.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface BridgeMetadata {

    /**
     * Other classes from which to inherit metadata.
     * <p>
     * These classes must also be annotated with {@link BridgeClass}.
     * All metadata from inherited classes will be recursively merged into the current class.
     *
     * @return An array of classes whose metadata should be inherited.
     */
    Class<?>[] inheritFrom() default {};

    /**
     * A list of native include files required by the generated binding.
     * <p>
     * These might correspond to C++ header files (e.g., {@code "Vector3.h"}), and will typically
     * be inserted into the generated native headers or source files.
     *
     * @return An array of include file paths.
     */
    String[] includes() default {};

    /**
     * A list of custom JNI source file paths to be included during code generation.
     * <p>
     * These may point to `.cpp` or `.c` files that contain native implementations or support code.
     *
     * @return An array of custom JNI source file paths.
     */
    String[] customJNICodePaths() default {};
}
