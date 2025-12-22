package com.jnibridge.exception;

/**
 * Base runtime exception type thrown by the JNI bridge.
 * <p>
 * {@code JniBridgeException} represents errors originating from the native
 * (C++) side of the application that are propagated into the Java runtime
 * through JNI.
 * <p>
 * Typical causes include:
 * <ul>
 *   <li>Invalid or destroyed native handles</li>
 *   <li>Type or ownership mismatches in native handles</li>
 *   <li>Failures during native-to-Java object conversion</li>
 *   <li>Unexpected internal bridge errors</li>
 * </ul>
 */
public class JniBridgeException extends RuntimeException {

    // @formatter:off
    public JniBridgeException() { }
    public JniBridgeException(String message) { super(message); }
    public JniBridgeException(String message, Throwable cause) { super(message, cause); }
    public JniBridgeException(Throwable cause) { super(cause); }
    // @formatter:on
}
