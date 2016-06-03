package com.dataeye.common;


import com.xunlei.util.concurrent.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



public class ServerMgr {

    private final Logger LOGGER = LoggerFactory.getLogger(ServerMgr.class);

    private static ServerMgr instance;

    private ServerMgr(){
        for (int i = 46665; i < 46690; i++) {
            portAvailable.add(i);
        }
        Thread monitor = new Thread(runnable);
        monitor.setName("Detect The Expired Server Thread");
        monitor.start();
    }

    public synchronized static ServerMgr getInstance(){
        if (instance == null) {
            instance = new ServerMgr();
        }
        return instance;
    }

    //服务器空闲时间(idle)
    private final long DURATION_TIME = 1 * 30 * 1000;

    //进程id与启动的server一一对应
    private Map<Integer, Server> serverPool = new ConcurrentHashMap<>();

    //可用的端口
    private Set<Integer> portAvailable = new ConcurrentHashSet<>(25);

    //正在使用的端口
    private Set<Integer> portInUsing = new ConcurrentHashSet<>();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO: 2016/6/2 轮询服务器池中server是否过期
            while (serverPool.size() > 0) {
                Set<Integer> keys = serverPool.keySet();
                for (int key : keys) {
                    Server server = serverPool.get(key);
                    LOGGER.info(server.getPid() + " " + server.getPort());
                    long lastRequest =server.getLastRequest();
                    long now = System.currentTimeMillis();
                    if ((now - lastRequest) > DURATION_TIME) {
                        server.stop();
                    }
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    private void increment(){
        //默认添加5个
        if (portAvailable.size() == 0) {
            List<Integer> list = new ArrayList<>();
            for (int port : portInUsing) {
                list.add(port);
            }
            Collections.sort(list);
            int maxPort = list.get(list.size() - 1);
            for (int i = maxPort; i < maxPort + 5; i++) {
                portAvailable.add(i);
            }
        }
    }

    private boolean existAvailablePort(){
        return portAvailable.size() > 0;
    }

    public synchronized int getPort(){
        if (!existAvailablePort()) {
            increment();
        }
        return portAvailable.iterator().next();
    }


    //检测进程id对应的server是否存在
    public Server getServerByPid(int pid){
        return serverPool.get(pid);
    }

    public Set<Integer> getPortAvailable() {
        return portAvailable;
    }

    public void setPortAvailable(Set<Integer> portAvailable) {
        this.portAvailable = portAvailable;
    }

    public Set<Integer> getPortInUsing() {
        return portInUsing;
    }

    public void setPortInUsing(Set<Integer> portInUsing) {
        this.portInUsing = portInUsing;
    }

    public Map<Integer, Server> getServerPool() {
        return serverPool;
    }

    public void setServerPool(Map<Integer, Server> serverPool) {
        this.serverPool = serverPool;
    }
}
