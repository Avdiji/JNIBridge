package com.jnibridge.annotations.modifiers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter or method return type should be treated as {@code const}
 * in the native (C/C++) layer.
 *
 * <p>
 * <strong>NOTE!</strong>
 * <br>
 * It ONLY applies to the <code>${cType}</code> and <code>{$jniType}</code> placeholders, otherwise it will have no effect.
 * </p>
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Const {
    // TODO properly handle const annotations...
}
