package com.rodbate.server;


import com.rodbate.httpserver.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);



    public static void main(String[] args) {


        try {
            Bootstrap.main();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            System.exit(-1);
        }

    }
}
