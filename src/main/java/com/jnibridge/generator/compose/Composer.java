package com.jnibridge.generator.compose;

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

}
