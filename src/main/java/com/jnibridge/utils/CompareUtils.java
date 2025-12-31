package com.jnibridge.utils;

public class CompareUtils {

    public static int depth(Class<?> c) {
        int d = 0;
        for (Class<?> cur = c; cur != null; cur = cur.getSuperclass()) d++;
        return d;
    }

}
