package com.dataeye.common;


import com.dataeye.core.GreysLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Server {

    private final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private int port;

    private int pid;

    private String user;

    private Process process;

    private volatile long lastRequest = System.currentTimeMillis();

    private ServerMgr mgr = ServerMgr.getInstance();


    public synchronized void request(){
        lastRequest = System.currentTimeMillis();
    }

    public Server(int pid, String user) {
        this.pid = pid;
        this.user = user;
        port = mgr.getPort();
    }

    public synchronized void start(){
        try {
            Server server = mgr.getServerByPid(pid);
            LOGGER.info("the current pid is {}", pid);
            LOGGER.info("get server {} from server pool", server);

            if (server == null) {

                String core = (String) Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator + "jvmserver.properties", "corePath");
                String agent = (String) Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator + "jvmserver.properties", "agentPath");

                String shell = Constant.CONF_DIR + File.separator + "start.sh " + core + " " + port + " " + agent + " " + pid;

                LOGGER.info("start shell path {}", shell);

                String cmd = "su " + user + " -s " + shell;

                LOGGER.info("shell cmd " + cmd);

                String[] command = new String[]{"/bin/sh", "-c", cmd};

                process = Runtime.getRuntime().exec(command);

                process.waitFor();


                while (true) {
                    try {
                        Client client = new Client(this);
                        String response = client.sendCmd("version");
                        //LOGGER.info("response " + response);
                        if (response != null) {
                            LOGGER.info("while loop break ........");
                            break;
                        }
                    }catch (Exception e) {
                        //ignore
                    }

                    Thread.sleep(20);
                }


                mgr.serverPool.putIfAbsent(pid, this);
                LOGGER.info("pid is {}, server pool size {}", pid, mgr.serverPool.size());
                mgr.portInUsing.add(port);
                mgr.portAvailable.remove(port);
                lastRequest = System.currentTimeMillis();
                CommonUtil.writePortToFile(String.valueOf(port));
                //LOGGER.info("server pool size is {} ",mgr.getServerPool().size());
            } else {
                port = server.getPort();
                LOGGER.info("" + server.getPid());
            }
        } catch (Exception e) {
            //ignore
        }
    }

    public synchronized void stop(){
        // TODO: 2016/6/2  client send 'shutdown' command
        Client client = new Client(this);
        String response = client.sendCmd("shutdown");
        LOGGER.info("server shutdown info : \n" + response);
        mgr.serverPool.remove(pid);
        mgr.portInUsing.remove(port);
        mgr.portAvailable.add(port);
        if (process != null) {
            process.destroy();
        }
        CommonUtil.removePortFromFile(String.valueOf(port));
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
