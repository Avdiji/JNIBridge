package com.jnibridge.annotations.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Shared {

    String allocTemplate() default "com/jnibridge/internals/alloc/alloc.shared.template";
    String inMapping() default "com/jnibridge/mappings/bridged_classes/shared/jnibridge.shared.in.mapping";
    String outMapping() default "com/jnibridge/mappings/bridged_classes/shared/jnibridge.shared.out.mapping";
}
