package com.dataeye.server.common;



public class JvmException extends Exception{

    public JvmException(String message) {
        super(message);
    }

    public JvmException(String message, Throwable cause) {
        super(message, cause);
    }
}
