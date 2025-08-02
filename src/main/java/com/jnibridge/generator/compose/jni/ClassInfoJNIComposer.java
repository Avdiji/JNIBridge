package com.jnibridge.generator.compose.jni;

import com.jnibridge.generator.compose.ClassInfoComposer;
import com.jnibridge.generator.model.ClassInfo;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import lombok.NonNull;

public class ClassInfoJNIComposer extends ClassInfoComposer {

    public ClassInfoJNIComposer(@NonNull ClassInfo classInfo) {
        super(classInfo);
    }

    @Override
    public String compose() {
        String result = ResourceUtils.load("com/jnibridge/templates/classes/jni_class.template");
        return TemplateUtils.substitute(result, getReplacements());
    }
}
