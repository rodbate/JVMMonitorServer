package com.dataeye.common;


import com.xunlei.util.concurrent.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ServerMgr {

    private final Logger _LOGGER_FILE = LogUtil.getLogger("jvmserver");

    private final Logger _LOGGER_STDOUT = LogUtil.getLogger("stdout");

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
    private final long DURATION_TIME = Constant.DURATION_TIME_MIN * 60 * 1000L;

    //进程id与启动的server一一对应
    public ConcurrentMap<Integer, Server> serverPool = new ConcurrentHashMap<>();

    //可用的端口
    public Set<Integer> portAvailable = new ConcurrentHashSet<>(25);

    //正在使用的端口
    public Set<Integer> portInUsing = new ConcurrentHashSet<>();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            _LOGGER_STDOUT.info("Monitor Server starting.....");
            // TODO: 2016/6/2 轮询服务器池中server是否过期
            while (true) {

                for (final Map.Entry<Integer, Server> entry : serverPool.entrySet()) {
                    //LOGGER.info(server.getPid() + " " + server.getPort());
                    //System.out.println(server.getPid() + " " + server.getPort());

                    final Server server = entry.getValue();

                    long lastRequest =server.getLastRequest();
                    long now = System.currentTimeMillis();
                    if ((now - lastRequest) > DURATION_TIME) {
                        server.stop();
                    }
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    _LOGGER_FILE.info(ExceptionStackUtil.print(e));
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


    public void shutdown(){
        for (Map.Entry<Integer, Server> serverMap : serverPool.entrySet()) {
            Server server = serverMap.getValue();
            server.stop();
        }
    }


}
