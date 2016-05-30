package com.dataeye;


import com.xunlei.netty.httpserver.Bootstrap;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class Launcher {

    public static void main(String[] args) throws IOException {

        Bootstrap.main(args, new Runnable() {
            @Override
            public void run() {
                System.out.println("start ..............");
            }
        }, "classpath:applicationContext.xml");
    }
}
