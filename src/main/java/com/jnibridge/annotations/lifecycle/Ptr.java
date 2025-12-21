package com.jnibridge.annotations.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ptr {

    String inMapping() default "com/jnibridge/mappings/bridged_classes/raw/jnibridge.ptr.in.mapping";
    String outMapping() default "com/jnibridge/mappings/bridged_classes/raw/jnibridge.ptr.out.mapping";

}
