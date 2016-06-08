package com.dataeye.common;


import com.dataeye.core.GreysLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Server {

    private final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private int port;

    private int pid;

    private String[] args;

    private long lastRequest = System.currentTimeMillis();

    private ServerMgr mgr = ServerMgr.getInstance();

    public synchronized void request(){
        lastRequest = System.currentTimeMillis();
    }

    public Server(int pid) {
        this.pid = pid;
        port = mgr.getPort();
        args = buildArgs();
    }

    public void start() throws Exception {

            Server server = mgr.getServerByPid(pid);

            if (server == null) {
                new Thread("GaServer Thread") {
                    @Override
                    public void run() {
                        try {
                            new GreysLauncher(args);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.start();


                int retry = 0;
                while (true) {
                    try {
                        Client client = new Client(this);
                        String response = client.sendCmd("version");
                        if (response != null) {
                            break;
                        }
                    } catch (Exception e) {
                        if (retry++ > 5) {
                            throw new Exception(e);
                        }
                    }

                    Thread.sleep(200);
                }
                mgr.serverPool.putIfAbsent(pid, this);
                mgr.portInUsing.add(port);
                mgr.portAvailable.remove(port);
                lastRequest = System.currentTimeMillis();
            } else {
                port = server.getPort();
                LOGGER.info("" + server.getPid());
            }
    }

    public void stop(){
        // TODO: 2016/6/2  client send 'shutdown' command
        System.out.println("=============stop================");
        Client client = new Client(this);
        String response = null;
        try {
            response = client.sendCmd("shutdown");
        } catch (Exception e) {
            LOGGER.error("connect server error");
        }
        LOGGER.info("server shutdown info : \n" + response);
        mgr.serverPool.remove(pid);
        mgr.portInUsing.remove(port);
        mgr.portAvailable.add(port);
    }


    private String[] buildArgs(){
        // TODO: 2016/6/3 构造参数
        String core = (String) Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator + "jvmserver.properties", "corePath");
        String agent = (String) Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator + "jvmserver.properties", "agentPath");
        String[] args = new String[4];
        args[0] = "-p" + pid;
        args[1] = "-t127.0.0.1:" + port;
        args[2] = "-c" + core;
        args[3] = "-a"+agent;
        return args;
    }

    public long getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(long lastRequest) {
        this.lastRequest = lastRequest;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
