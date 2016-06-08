package com.dataeye;


import com.dataeye.common.Server;
import com.dataeye.common.ServerMgr;
import com.xunlei.netty.httpserver.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@Service
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);



    public static void main(String[] args) throws IOException {


        Bootstrap.main(args, new Runnable() {
            @Override
            public void run() {
                System.out.println("start ..............");
                ResourceLoad.getInstance();
            }
        }, "classpath:applicationContext.xml");
    }
}
