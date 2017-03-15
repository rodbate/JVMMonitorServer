package com.rodbate.server.common;


import org.slf4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import static jline.console.KeyMap.CTRL_D;

public class Client {

    private final Logger _LOGGER_FILE = LogUtil.getLogger("jvmserver");
    private final Logger LOGGER = LogUtil.getLogger("stdout");

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
            _LOGGER_FILE.info(ExceptionStackUtil.print(e));
            //ignore
        }
    }

    public String sendCmd(String command){
        connect();

        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            command = command + "\n";
            out.write(command.getBytes());
            out.flush();
            if (command.startsWith("shutdown")) {
                return "server [port[" + server.getPort() + "]] shut down now.......";
            }

            if (CommonUtil.isWaitToStopCommand(command)) {
                br = new BufferedReader(new InputStreamReader(in));


                int maxLine;
                if (command.startsWith("monitor")) {
                    maxLine = 2;
                } else {
                    maxLine = 10;
                }

                int n = 0;
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                    if ("".equals(line.trim())) {
                        n++;
                        sb.append("\n");
                    }
                    if (n > maxLine) {
                        out.write(CTRL_D);
                        out.flush();
                        break;
                    }
                }
                br.close();
                return sb.toString();
            }

            if (command.startsWith("watch")) {
                String line = "";
                int n=0;
                br = new BufferedReader(new InputStreamReader(in));
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                    n++;

                    if (n > 100) {
                        out.write(CTRL_D);
                        out.flush();
                        break;
                    }
                }
                br.close();
                return sb.toString();
            }

            if (CommonUtil.isReturnImmediatelyCmd(command)) {
                char b;
                sb = new StringBuilder();

                while ((b = (char) in.read()) != 0x04) {
                    sb.append(b);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            close();
        }finally {
            close();
        }
        return null;
    }

    private void close(){
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                _LOGGER_FILE.info(ExceptionStackUtil.print(e));
            }
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                _LOGGER_FILE.info(ExceptionStackUtil.print(e));
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                _LOGGER_FILE.info(ExceptionStackUtil.print(e));
            }
        }
    }
}
