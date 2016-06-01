package com.dataeye.help;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SimpleClient {

    public static Socket connect(int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(port));
        return socket;
    }
}
