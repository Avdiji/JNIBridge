package com.jnibridge.generator.compose.jni;


import com.jnibridge.generator.compose.MethodInfoComposer;
import com.jnibridge.generator.model.MethodInfo;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

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

        boolean isStatic = getMethodInfo().isStatic();

        // TODO handle non static methods...
        String result = isStatic ? ResourceUtils.load("com/jnibridge/templates/methods/static_method.template") : "";
        return TemplateUtils.substitute(result, getReplacements());
    }
}
