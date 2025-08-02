package com.jnibridge.generator.compose;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A component responsible for generating or "composing" a string representation
 * of some code, signature, or descriptor.
 */
public interface Composer {

    /**
     * Composes and returns the string representation of any element.
     *
     * @return the composed string
     */
    String compose();

    /**
     * Returns a map of placeholder-to-value pairs used to replace
     * the placeholders defined in the type-mapping templates.
     *
     * @return a map of template placeholders to their resolved replacement values
     */
    @NotNull
    Map<String, String> getReplacements();
}
