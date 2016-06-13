package com.dataeye.server.utils;

/**
 * Created by wendy on 2016/6/2.
 */
public class StringUtils {

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }
}
