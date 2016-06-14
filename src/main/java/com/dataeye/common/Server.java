package com.dataeye.common;


import org.slf4j.Logger;

import java.io.File;

public class Server {

    private final Logger LOGGER = LogUtil.getLogger("stdout");

    private final Logger _LOGGER_FILE = LogUtil.getLogger("jvmserver");

    private static final Logger _LOGGER_STDOUT = LogUtil.getLogger("stdout");

    private int port;

    private int pid;

    private String user;

    private Process process;

    private volatile long lastRequest;

    private ServerMgr mgr = ServerMgr.getInstance();


    public synchronized void request(){
        lastRequest = System.currentTimeMillis();
    }


    public static synchronized Server launchServer(int pid){
        ServerMgr mg = ServerMgr.getInstance();
        Server server = mg.getServerByPid(pid);

        _LOGGER_STDOUT.info("the current pid is {}", pid);
        _LOGGER_STDOUT.info("get server {} from server pool", server);

        if (server == null) {
            server = new Server(pid);
            server.start();
        }

        return server;
    }
    private Server(int pid) {
        this.pid = pid;
        this.user = CommonUtil.getUserByPid(String.valueOf(pid));
        port = mgr.getPort();
    }

    private void start(){
        try {

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
                    //log
                    _LOGGER_FILE.info(ExceptionStackUtil.print(e));
                }

                Thread.sleep(20);
            }


            mgr.serverPool.putIfAbsent(pid, this);
            LOGGER.info("pid is {}, server pool size {}", pid, mgr.serverPool.size());
            mgr.portInUsing.add(port);
            mgr.portAvailable.remove(port);

            CommonUtil.writePortToFile(String.valueOf(port));
            //LOGGER.info("server pool size is {} ",mgr.getServerPool().size());

        } catch (Exception e) {
            //log
            _LOGGER_FILE.info(ExceptionStackUtil.print(e));
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



    public synchronized long getLastRequest() {
        return lastRequest;
    }


    public int getPid() {
        return pid;
    }


    public int getPort() {
        return port;
    }

}
