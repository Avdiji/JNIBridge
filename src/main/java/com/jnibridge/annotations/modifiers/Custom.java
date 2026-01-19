package com.jnibridge.annotations.modifiers;

import com.jnibridge.annotations.mapping.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides the default JNI code generation behavior for a method or parameter.
 * <p>
 * This annotation allows fine-grained customization of the generated native code
 * by providing explicit values for placeholders used during code generation.
 * When present, the code generator will substitute the specified fields instead
 * of relying on automatically inferred mappings.
 * </p>
 *
 * <p>
 * The annotation can be applied to either methods or parameters, allowing
 * customization at different levels of the JNI binding process.
 * </p>
 * <p>
 * All values are optional. Only the explicitly provided attributes will override
 * the default code generation behavior.
 * </p>
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Custom {

    /**
     * Specifies the native C/C++ type to be used in the generated code.
     *
     * @return the C/C++ type declaration
     */
    String cType() default "";

    /**
     * Specifies the JNI type to be used in the generated JNI function signature.
     *
     * @return the JNI type declaration
     */
    String jniType() default "";

    /**
     * Specifies the variable name to be used in generated native C/C++ code.
     *
     * @return the native variable name
     */
    String cVar() default "";

    /**
     * Specifies the variable name to be used in generated JNI code.
     *
     * @return the JNI variable name
     */
    String jniVar() default "";

    /**
     * Specifies a custom function invocation or expression to be emitted
     * in place of the default generated call.
     *
     * @return a custom function call or expression
     */
    String functionCall() default "";


    String outMappingTemplatePath() default "";
    String inMappingTemplatePath() default "";

    /**
     * @return {@link Mapping#cTemplateArgumentTypes()}
     * @see Mapping#cTemplateArgumentTypes()
     */
    String[] cTemplateArgumentTypes() default {};

    /**
     * @return {@link Mapping#jTemplateArgumentTypes()}
     * @see Mapping#jTemplateArgumentTypes() ()
     */
    Class<?>[] jTemplateArgumentTypes() default {};
}
