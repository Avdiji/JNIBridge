package com.jnibridge.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface InheritableMetadata {

    Class<?>[] inheritFrom() default {};

    String[] includes() default {};

    String[] customJNICodePaths() default {};
}
