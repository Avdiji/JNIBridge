package com.jnibridge.generator.compose.jni;


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

        if(getMethodInfo().isAlloc()) {
            TypeInfo selfType = Optional.ofNullable(getMethodInfo().getSelfType()).orElseThrow(() -> new IllegalArgumentException("Self type must be set for the allocator"));

            Map<String, String> allocReplacements = new HashMap<>();
            allocReplacements.put(TypeInfoComposer.PLACEHOLDER_C_TYPE, selfType.getCType());

            String allocMethodTemplate = ResourceUtils.load("com/jnibridge/templates/methods/alloc_method.template");
            allocMethodTemplate = TemplateUtils.substitute(allocMethodTemplate, allocReplacements);
            return TemplateUtils.substitute(allocMethodTemplate, getReplacements());
        }

        if(getMethodInfo().isDealloc()) {
            TypeInfo selfType = Optional.ofNullable(getMethodInfo().getSelfType()).orElseThrow(() -> new IllegalArgumentException("Self type must be set for the deallocator"));

            Map<String, String> deallocReplacements = new HashMap<>();
            deallocReplacements.put(TypeInfoComposer.PLACEHOLDER_C_TYPE, selfType.getCType());

            String deallocMethodTemplate = ResourceUtils.load("com/jnibridge/templates/methods/dealloc_method.template");
            deallocMethodTemplate = TemplateUtils.substitute(deallocMethodTemplate, deallocReplacements);
            return TemplateUtils.substitute(deallocMethodTemplate, getReplacements());
        }


        if (getMethodInfo().isStatic()) {
            String staticMethodTemplate = ResourceUtils.load("com/jnibridge/templates/methods/static_method.template");
            return TemplateUtils.substitute(staticMethodTemplate, getReplacements());
        }


        String instanceMethodTemplate = ResourceUtils.load("com/jnibridge/templates/methods/instance_method.template");
        return TemplateUtils.substitute(instanceMethodTemplate, getReplacements());
    }
}
