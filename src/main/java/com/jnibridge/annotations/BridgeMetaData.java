package com.jnibridge.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BridgeMetaData {

    boolean isStruct() default false;

    Class<?>[] inheritFrom() default {};

    String[] includes() default {};

    String[] customJNICodePaths() default {};
}
