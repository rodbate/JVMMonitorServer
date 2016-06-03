package com.dataeye.common;


import com.dataeye.ResourceLoad;
import com.dataeye.core.GreysLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                new GreysLauncher(args);
                mgr.getServerPool().put(pid, this);
                mgr.getPortInUsing().add(port);
                mgr.getPortAvailable().remove(port);
                lastRequest = System.currentTimeMillis();
            } else {
                port = server.getPort();
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
        String core = (String) ResourceLoad.getValue("jvmserver.properties", "corePath");
        String agent = (String) ResourceLoad.getValue("jvmserver.properties", "agentPath");
        return null;
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
