package com.jnibridge.generator.compose.jni.helper;

import com.jnibridge.JniBridgeRegistry;
import com.jnibridge.generator.compose.Composer;
import com.jnibridge.generator.compose.Placeholder;
import com.jnibridge.utils.ResourceUtils;
import com.jnibridge.utils.TemplateUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Composes JNI-specific helper-code to handle exceptions on a C++/jni level.
 */
public class JniBridgeExceptionComposer implements Composer {
    public static final String FILENAME = "JniBridgeExceptionHandler.cpp";

    @Override
    public String compose() {
        final String exceptionHandler = ResourceUtils.load("com/jnibridge/internals/exception/JniBridgeExceptionHandler.cpp");
        return TemplateUtils.substitute(exceptionHandler, getReplacements());
    }

    @Override
    public @NotNull Map<String, String> getReplacements() {
        final Map<String, String> replacements = new HashMap<>();
        replacements.put(Placeholder.EXCEPTION_FUNC, new JniBridgeExceptionFuncComposer().compose());
        replacements.put(Placeholder.HANDLE_FILE_INCLUDE, String.format("%s", JniBridgeHandleComposer.INTERNAL_FILENAME));
        return replacements;
    }

    /**
     * Composes JNI-specific helper-code to handle exceptions on a C++/jni level.
     */
    private static class JniBridgeExceptionFuncComposer implements Composer {

        @Override
        public String compose() {
            final String exceptionHandlerFunc = ResourceUtils.load("com/jnibridge/internals/exception/MappedExceptionHandlerFunc.template");
            return TemplateUtils.substitute(exceptionHandlerFunc, getReplacements());
        }

        @Override
        public @NotNull Map<String, String> getReplacements() {
            final Map<String, String> replacements = new HashMap<>();
            replacements.put(Placeholder.EXCEPTION_FUNC_BODY, getFuncBodyReplacement());
            return replacements;
        }

        /**
         * Generate the actual Body of the MappedExceptionHandler.
         *
         * @return A replacement String, that helps the tool generate the ExceptionHandler.
         */
        private String getFuncBodyReplacement() {

            final StringBuilder result = new StringBuilder();
            boolean isFirstIteration = true;

            for (final Map.Entry<String, Class<? extends Throwable>> exceptionMappingEntry : JniBridgeRegistry.getSortedExceptionEntries()) {
                String keyword = isFirstIteration ? "if" : "else if";
                isFirstIteration = false;

                // condition
                result.append("\t\t")
                        .append(keyword)
                        .append(" (dynamic_cast<const ")
                        .append(exceptionMappingEntry.getKey())
                        .append("*>(&e)) {\n");

                // condition body
                result.append("\t\t\treturn env->FindClass(\"")
                        .append(exceptionMappingEntry.getValue().getName().replace(".", "/"))
                        .append("\");\n\t\t}\n");
            }
            return result.toString();
        }

    }
}
