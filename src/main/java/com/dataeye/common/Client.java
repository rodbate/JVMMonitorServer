package com.dataeye.common;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

    private InputStream in;
    private OutputStream out;

    private Server server;

    private Socket socket;

    public Client(Server server) {
        this.server = server;
    }

    private void connect() throws IOException {

        server.request();

        socket = new Socket();
        InetSocketAddress address = new InetSocketAddress(server.getPort());

        socket.connect(address);
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    public String sendCmd(String command) throws Exception {

        connect();

        String response = "";
        if ("shutdown".equals(command)) {
            command = command + "\n";
            try {
                out.write(command.getBytes());
                response = "server shut down now.......";
            } catch (IOException e) {
                e.printStackTrace();
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
                e.printStackTrace();
                throw new Exception(e);
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
                e.printStackTrace();
            }
        }
    }
}
