package com.jnibridge.utils;

import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

/**
 * Utility class for performing simple string template substitutions using named placeholders.
 */
public class TemplateUtils {

    // Private constructor to prevent instantiation
    private TemplateUtils() { }

    /**
     * Method for performing simple template substitution using named placeholders.
     *
     * @param template     The templated string.
     * @param replacements The substitution key-value pairs.
     * @param collapseConsecutiveBlanks Flag, that determines whether consecutive blanks shall be substituted with a singular newline.
     * @return The substituted template.
     */
    public static String substitute(String template, Map<String, String> replacements, final boolean collapseConsecutiveBlanks) {
        String result = new StringSubstitutor(replacements).replace(template);
        return collapseConsecutiveBlanks ? result.replaceAll("(?m)^(?:[ \\t]*\\R){2,}", "") : result;
    }


    /**
     * Method for performing simple template substitution using named placeholders.
     *
     * @param template     The templated string.
     * @param replacements The substitution key-value pairs.
     * @return The substituted template.
     */
    public static String substitute(String template, Map<String, String> replacements) {
        return substitute(template, replacements, false);
    }
}
