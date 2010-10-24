package com.alphadog.tribe.helpers;

public class StringHelpers {
    public static boolean isBlank(String s) {
        return ((s == null) || (s.trim().length() == 0));
    }
}
