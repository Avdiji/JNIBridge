package com.jnibridge.generator.compose.jni;

import com.jnibridge.annotations.modifiers.Const;
import com.jnibridge.generator.compose.TypeInfoComposer;
import com.jnibridge.generator.model.TypeInfo;
import com.jnibridge.utils.TemplateUtils;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Composes JNI-specific string representations of {@link TypeInfo} objects.
 */
public class TypeInfoJNIComposer extends TypeInfoComposer {


    /**
     * Creates a new JNI type composer for the given {@link TypeInfo}.
     *
     * @param typeInfo the type information to compose (must not be {@code null})
     */
    public TypeInfoJNIComposer(@NonNull TypeInfo typeInfo) { super(typeInfo); }

    @Override
    public @NotNull String compose() {
        final TypeInfo typeInfo = getTypeInfo();

        // fetch all critical metadata
        final boolean isReturnValue = typeInfo.getId() == null;

        // fetch the right (incoming/outgoing) mapping template
        String result = isReturnValue ? typeInfo.getOutMapping() : typeInfo.getInMapping();
        result = TemplateUtils.substitute(result, getReplacements());

        return result;
    }


}
