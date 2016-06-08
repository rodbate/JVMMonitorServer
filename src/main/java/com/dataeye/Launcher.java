package com.dataeye;


import com.dataeye.common.ServerMgr;
import com.xunlei.netty.httpserver.Bootstrap;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class Launcher {

    private static ServerMgr serverMgr = ServerMgr.getInstance();

    public static void main(String[] args) throws IOException {

        Bootstrap.main(args, new Runnable() {
            @Override
            public void run() {
                System.out.println("start ..............");
                ResourceLoad.getInstance();
            }
        }, new Runnable() {
            @Override
            public void run() {
                System.out.println("stop .................");
                serverMgr.shutdown();
            }
        }, "classpath:applicationContext.xml");
    }
}
