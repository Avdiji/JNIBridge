package com.jnibridge.generator.compose;

import com.jnibridge.generator.model.ClassInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A component responsible for generating or "composing" a string representation
 * of some code, signature, or descriptor.
 */
@SuppressWarnings("unused") // IDE does not seem to recognize that these functions are being used...
public interface Composer {

    /**
     * Composes and returns the string representation of any element.
     *
     * @return the composed string
     */
    String compose();

    /**
     * Returns a map of placeholder-to-value pairs used to replace
     * the placeholders defined in the type-mapping templates.
     *
     * @return a map of template placeholders to their resolved replacement values
     */
    @NotNull
    Map<String, String> getReplacements();

    /**
     * Method returns a replacement String.
     *
     * @param defaultReplacement The replacement to use if nothing else has been specified.
     * @param customReplacement  The user-defined custom replacement.
     * @return A replacement String.
     */
    static String getReplacement(@NotNull final String defaultReplacement, @Nullable final String customReplacement) {
        if (customReplacement != null && !customReplacement.isEmpty()) {
            return customReplacement;
        } else return defaultReplacement;
    }

    /**
     * Function computes the jni-helper filename for the passed type.
     *
     * @param classInfo The class to create helper functions for.
     * @return A unique filename for the generated header file.
     */
    static String getPolyHelperFilename(@NotNull final ClassInfo classInfo) {
        return String.format("%s_%s.helper.hpp",
                classInfo.getClazz().getPackage().getName().replace(".", "_"),
                classInfo.getClazz().getSimpleName());
    }
}
