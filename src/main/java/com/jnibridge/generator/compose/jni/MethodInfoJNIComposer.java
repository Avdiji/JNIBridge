package com.jnibridge.generator.compose.jni;


import com.jnibridge.annotations.lifecycle.Allocate;
import com.jnibridge.annotations.lifecycle.Deallocate;
import com.jnibridge.annotations.lifecycle.Shared;
import com.jnibridge.generator.compose.MethodInfoComposer;
import com.jnibridge.generator.compose.TypeInfoComposer;
import com.jnibridge.generator.model.MethodInfo;
import com.jnibridge.generator.model.TypeInfo;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Composes JNI-specific string representations of {@link com.jnibridge.generator.model.MethodInfo} objects.
 */
public class MethodInfoJNIComposer extends MethodInfoComposer {

    /**
     * Creates a new JNI type composer for the given {@link MethodInfo}.
     *
     * @param methodInfo the method information to compose.
     */
    public MethodInfoJNIComposer(@NonNull MethodInfo methodInfo) { super(methodInfo); }

    @Override
    public @NotNull String compose() {

        // Handle Allocate / Deallocate methods
        if (getMethodInfo().getReturnType().hasAnnotation(Allocate.class)) { return composeAllocFunction(); }
        if (getMethodInfo().getReturnType().hasAnnotation(Deallocate.class)) { return composeDeallocFunction(); }

        // Handle static methods
        if (getMethodInfo().isStatic()) {
            String staticMethodTemplate = ResourceUtils.load("com/jnibridge/other/methods/static_method.template");
            return TemplateUtils.substitute(staticMethodTemplate, getReplacements());
        }

        // Handle all other methods
        String instanceMethodTemplate = ResourceUtils.load("com/jnibridge/other/methods/instance_method.template");
        return TemplateUtils.substitute(instanceMethodTemplate, getReplacements());
    }

    /**
     * compose the JNI code for allocation functions.
     * @return The JNI code for allocation functions.
     */
    private String composeAllocFunction() {
        // setup additional mappings...
        TypeInfo selfType = getMethodInfo().getSelfType();
        Objects.requireNonNull(selfType, "Self type must be set for the allocator");
        Map<String, String> allocReplacements = new HashMap<>();

        allocReplacements.put(TypeInfoComposer.PLACEHOLDER_C_TYPE, selfType.getCType());
        allocReplacements.put(TypeInfoComposer.PLACEHOLDER_FULL_J_PATH, selfType.getType().getName().replace(".", "/"));

        // extract the allocation method
        final TypeInfo returnType = getMethodInfo().getReturnType();
        final StringBuilder allocMethodTemplatePath = new StringBuilder();

        // default (raw) allocation
        Optional<Allocate> allocOpt = returnType.getAnnotation(Allocate.class);
        allocOpt.ifPresent(alloc -> allocMethodTemplatePath.append(alloc.allocTemplate()));

        // alloc as sharedPtr
        Optional<Shared> sharedOpt = returnType.getAnnotation(Shared.class);
        sharedOpt.ifPresent(alloc -> {
            allocMethodTemplatePath.setLength(0);
            allocMethodTemplatePath.append(alloc.allocTemplate());
        });

        // alloc as uniquePtr // TODO(unique support)
//        Optional<Unique> uniqueOpt = returnType.getAnnotation(Unique.class);
//        uniqueOpt.ifPresent(alloc -> {
//            allocMethodTemplatePath.setLength(0);
//            allocMethodTemplatePath.append(alloc.allocTemplate());
//        });

        // compose the allocation function...
        String allocMethodTemplate = ResourceUtils.load(allocMethodTemplatePath.toString());
        allocMethodTemplate = TemplateUtils.substitute(allocMethodTemplate, allocReplacements);
        return TemplateUtils.substitute(allocMethodTemplate, getReplacements());
    }

    /**
     * compose the JNI code for deallocation functions.
     * @return The JNI code for deallocation functions.
     */
    private String composeDeallocFunction() {
        // setup additional mappings...
        TypeInfo selfType = getMethodInfo().getSelfType();
        Objects.requireNonNull(selfType, "Self type must be set for the allocator");
        Map<String, String> deallocReplacements = new HashMap<>();
        deallocReplacements.put(TypeInfoComposer.PLACEHOLDER_C_TYPE, selfType.getCType());

        // fetch deallocation annotation
        final TypeInfo returnType = getMethodInfo().getReturnType();
        final Deallocate deallocateAnnotation = returnType.getAnnotation(Deallocate.class)
                .orElseThrow(() -> new IllegalArgumentException("Method expected to be annotated with 'Deallocate'"));

        // compose the deallocation function...
        String deallocMethodTemplate = ResourceUtils.load(deallocateAnnotation.deallocTemplate());
        deallocMethodTemplate = TemplateUtils.substitute(deallocMethodTemplate, deallocReplacements);
        return TemplateUtils.substitute(deallocMethodTemplate, getReplacements());
    }
}
