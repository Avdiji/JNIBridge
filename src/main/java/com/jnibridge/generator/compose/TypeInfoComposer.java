package com.jnibridge.generator.compose;

import com.jnibridge.annotations.modifiers.Const;
import com.jnibridge.annotations.modifiers.Custom;
import com.jnibridge.generator.model.TypeInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Composes string representations of {@link TypeInfo} objects.
 */
@Getter
@RequiredArgsConstructor
public abstract class TypeInfoComposer implements Composer {

    // TypeInfo - related Placeholders...
    public static final String PLACEHOLDER_C_TYPE = "cType";
    public static final String PLACEHOLDER_C_TYPE_UNDERSCORE = "cTypeUnderscore";
    public static final String PLACEHOLDER_C_VAR = "cVar";

    public static final String PLACEHOLDER_FULL_J_PATH = "fullJPath";

    public static final String PLACEHOLDER_JNI_TYPE = "jniType";
    public static final String PLACEHOLDER_JNI_VAR = "jniVar";
    public static final String PLACEHOLDER_ID = "id";

    @NonNull
    private final TypeInfo typeInfo;

    @Override
    @NotNull
    public Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<>();

        final boolean isConst = typeInfo.hasAnnotation(Const.class);
        final String cTypeReplacement = String.format("%s%s", isConst ? "const " : "", typeInfo.getCType());

        Optional<Custom> custom = typeInfo.getAnnotation(Custom.class);

        replacements.put(PLACEHOLDER_C_TYPE, getReplacement(cTypeReplacement, custom.map(Custom::cType).orElse(null)));
        replacements.put(PLACEHOLDER_C_TYPE_UNDERSCORE, cTypeReplacement.replace("::", "_"));

        replacements.put(PLACEHOLDER_JNI_TYPE, getReplacement(typeInfo.getJniType(), custom.map(Custom::jniType).orElse(null)));

        String id = Optional.ofNullable(typeInfo.getId()).orElse("");
        replacements.put(PLACEHOLDER_ID, id);

        replacements.put(PLACEHOLDER_C_VAR, getReplacement(PLACEHOLDER_C_VAR + id, custom.map(Custom::cVar).orElse(null)));
        replacements.put(PLACEHOLDER_JNI_VAR, getReplacement(PLACEHOLDER_JNI_VAR + id, custom.map(Custom::jniVar).orElse(null)));

        replacements.put(PLACEHOLDER_FULL_J_PATH, typeInfo.getType().getName().replace(".", "/"));

        return replacements;
    }

    /**
     * Method returns a replacement String.
     *
     * @param defaultReplacement The replacement to use if nothing else has been specified.
     * @param customReplacement  The user-defined custom replacement.
     * @return A replacement String.
     */
    public static String getReplacement(@NotNull final String defaultReplacement, @Nullable final String customReplacement) {
        if (customReplacement != null && !customReplacement.isEmpty()) {
            return customReplacement;
        } else return defaultReplacement;
    }

}
