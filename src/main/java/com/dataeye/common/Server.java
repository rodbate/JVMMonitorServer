package com.dataeye.common;


import com.dataeye.ResourceLoad;
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

    public synchronized void start(){
        try {
            Server server = mgr.getServerByPid(pid);

            if (server == null) {
                new Thread("GaServer Thread") {
                    @Override
                    public void run() {
                        try {
                            new GreysLauncher(args);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

                while (true) {
                    try {
                        Client client = new Client(this);
                        String response = client.sendCmd("version");
                        //LOGGER.info("response " + response);
                        if (response != null) {
                            break;
                        }
                    }catch (Exception e) {
                        //ignore
                    }

                    Thread.sleep(20);
                }
                mgr.getServerPool().put(pid, this);
                mgr.getPortInUsing().add(port);
                mgr.getPortAvailable().remove(port);
                lastRequest = System.currentTimeMillis();
                //LOGGER.info("server pool size is {} ",mgr.getServerPool().size());
            } else {
                port = server.getPort();
                LOGGER.info("" + server.getPid());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void stop(){
        // TODO: 2016/6/2  client send 'shutdown' command
        Client client = new Client(this);
        String response = client.sendCmd("shutdown");
        LOGGER.info("server shutdown info : \n" + response);
        mgr.getServerPool().remove(pid);
        mgr.getPortInUsing().remove(port);
        mgr.getPortAvailable().add(port);
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
