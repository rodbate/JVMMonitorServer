package com.dataeye.server.common;


import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

    private final Logger _LOGGER_FILE = LogUtil.getLogger("jvmserver");

    private InputStream in;
    private OutputStream out;

    private Server server;

    private Socket socket;

    public Client(Server server) {
        this.server = server;
    }

    private void connect() {

        server.request();

        socket = new Socket();
        InetSocketAddress address = new InetSocketAddress(server.getPort());
        try {
            socket.connect(address);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            //e.printStackTrace();
            //_LOGGER_FILE.info(ExceptionStackUtil.print(e));
            //ignore
        }
    }

    public String sendCmd(String command){

        connect();

        String response = "";
        if ("shutdown".equals(command)) {
            command = command + "\n";
            try {
                out.write(command.getBytes());
                response = "server [port[" + server.getPort() + "]] shut down now.......";
            } catch (IOException e) {
                //e.printStackTrace();
                _LOGGER_FILE.info(ExceptionStackUtil.print(e));
            } finally {
                close();
            }
        } else {
            command = command + "\n";
            try {
                out.write(command.getBytes("utf-8"));

                char b;
                StringBuilder sb = new StringBuilder();
                while ((b = (char) in.read()) != 0x04) {
                    sb.append(b);
                }
                response = sb.toString();
            } catch (IOException e) {
                //e.printStackTrace();
                _LOGGER_FILE.info(ExceptionStackUtil.print(e));
            } finally {
                close();
            }
        }
        return response;
    }

    private void close(){
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                //e.printStackTrace();
                _LOGGER_FILE.info(ExceptionStackUtil.print(e));
            }
        }
    }
}
