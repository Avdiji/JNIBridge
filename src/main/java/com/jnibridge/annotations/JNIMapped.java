package com.jnibridge.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JNIMapped {

    /**
     * Native namespace for the mapped class (e.g., "core::math").
     */
    String namespace() default "";

    /**
     * Native class or struct name. If empty, uses the Java class name.
     */
    String name() default "";

    /**
     * Native includes (e.g., headers) to include in generated C++/C files.
     */
    String[] includes() default "";

    /**
     * Base/native classes this type inherits from.
     */
    Class<?>[] baseClasses() default {};

    /**
     * Native interfaces this class is expected to implement.
     */
    Class<?>[] interfaces() default {};

}
