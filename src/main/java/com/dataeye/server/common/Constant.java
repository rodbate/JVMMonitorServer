package com.dataeye.server.common;


import com.dataeye.server.ResourceLoad;
import com.dataeye.server.common.annotation.AutoUpdate;
import com.dataeye.server.common.annotation.Inject;

import java.io.File;

@Inject
public class Constant {

    public static final String USER_DIR = System.getProperty("user.dir");

    public static final String CONF_DIR = USER_DIR + File.separator + "conf";

    public static ResourceLoad RESOURCE_LOAD = ResourceLoad.getInstance();

    @AutoUpdate
    public static volatile int DURATION_TIME_MIN = Integer.parseInt((String) RESOURCE_LOAD.getValue(CONF_DIR + File.separator +
            "jvmserver.properties", "durationTime"));

    @AutoUpdate
    public static volatile String ACROSS_DOMAIN = (String) RESOURCE_LOAD.getValue(CONF_DIR + File.separator +
            "jvmserver.properties", "Access-Control-Allow-Origin");



}
