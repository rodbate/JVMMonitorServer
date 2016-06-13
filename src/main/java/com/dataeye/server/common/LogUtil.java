package com.dataeye.server.common;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LogUtil {

    private final static LoggerContext context;

    static {

        context = (LoggerContext) LoggerFactory.getILoggerFactory();

        JoranConfigurator configurator = new JoranConfigurator();

        configurator.setContext(context);

        context.reset();

        try {
            configurator.doConfigure(new File(Constant.CONF_DIR + File.separator + "logback.xml"));
        } catch (JoranException e) {
            e.printStackTrace();
        }

    }

    public static Logger getLogger(String name){
        return context.getLogger(name);
    }
}
