package com.jnibridge.generator.compose.jni;

import com.jnibridge.generator.compose.Placeholder;
import com.jnibridge.generator.compose.TypeInfoComposer;
import com.jnibridge.generator.model.TypeInfo;
import com.jnibridge.utils.TemplateUtils;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Composes JNI-specific string representations of {@link TypeInfo} objects.
 */
public class TypeInfoJNIComposer extends TypeInfoComposer {

    /**
     * Creates a new JNI type composer for the given {@link TypeInfo}.
     *
     * @param typeInfo the type information to compose.
     */
    public TypeInfoJNIComposer(@NonNull TypeInfo typeInfo) { super(typeInfo); }

    @Override
    public @NotNull String compose() {
        final TypeInfo typeInfo = getTypeInfo();

        if(typeInfo.isSelf()) {
            Map<String, String> selfReplacements = new HashMap<>();
            selfReplacements.put(Placeholder.C_VAR, "cself");
            selfReplacements.put(Placeholder.JNI_VAR, "jself");

            String selfInMapping = typeInfo.getInMapping();
            selfInMapping = TemplateUtils.substitute(selfInMapping, selfReplacements);
            return TemplateUtils.substitute(selfInMapping, getReplacements());
        }

        // fetch all critical metadata
        final boolean isReturnValue = typeInfo.getId() == null;
        String result = isReturnValue ? typeInfo.getOutMapping() : typeInfo.getInMapping();
        result = TemplateUtils.substitute(result, getReplacements());

        return result;
    }


}
