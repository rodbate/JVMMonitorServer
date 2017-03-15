package com.rodbate.server.help;


import java.io.IOException;

public class ProcessUtil {

    public static Process process(String command) throws IOException {
        return Runtime.getRuntime().exec(command);
    }
}
