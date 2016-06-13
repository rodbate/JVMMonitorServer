package com.dataeye.server;


import com.dataeye.server.common.AutoUpdatePropertiesUtil;
import com.xunlei.netty.httpserver.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);



    public static void main(String[] args) throws IOException {


        Bootstrap.main(args, new Runnable() {
            @Override
            public void run() {
                System.out.println("start ..............");
                ResourceLoad.getInstance();
                AutoUpdatePropertiesUtil.init();
            }
        }, "classpath:applicationContext.xml");
    }
}
