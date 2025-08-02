package com.jnibridge.annotations.typemapping;

import com.jnibridge.mapper.TypeMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides the default type mapping for a specific method or parameter.
 * <p>
 * This annotation allows developers to specify a concrete {@link TypeMapper}
 * to be used for a particular method or parameter, even if the parameter's type
 * already has a default mapping.
 *
 * <p>This is useful when the same Java type can be mapped to multiple C++ representations,
 * depending on context (e.g., pointer vs. value, platform-specific encoding, etc...).
 *
 * <p><strong>Example usage:</strong>
 * <pre>{@code
 * @UseMapping(MyCustomStringMapper.class)
 * public native void process(@UseMapping(MyPointerIntMapper.class) int param);
 * }</pre>
 *
 * <p>Can be applied to methods or parameters. Must reference a {@link TypeMapper}-annotated class.
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UseMapping {

    /**
     * The {@link TypeMapper} class to use for this method or parameter.
     *
     * @return a class implementing {@link TypeMapper}, annotated with {@link Mapping}
     */
    Class<? extends TypeMapper> value();

}
