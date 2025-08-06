package com.jnibridge.generator.compose.jni;

import com.jnibridge.generator.compose.ClassInfoComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import lombok.NonNull;

/**
 * Composes JNI-specific string representations of {@link ClassInfo} objects.
 */
public class ClassInfoJNIComposer extends ClassInfoComposer {

    /**
     * Creates a new JNI type composer for the given {@link ClassInfo}.
     *
     * @param classInfo the method information to compose.
     */
    public ClassInfoJNIComposer(@NonNull ClassInfo classInfo) {
        super(classInfo);
    }

    @Override
    public String compose() {
        String result = ResourceUtils.load("com/jnibridge/templates/jni_files/jni_file.template");
        return TemplateUtils.substitute(result, getReplacements());
    }
}
