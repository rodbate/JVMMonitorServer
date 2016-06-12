package com.dataeye.common;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionStackUtil {


    public static String print(Throwable e) {


        StringWriter out = new StringWriter();

        PrintWriter pw = new PrintWriter(out, true);

        e.printStackTrace(pw);

        pw.close();

        return out.toString();
    }
}
