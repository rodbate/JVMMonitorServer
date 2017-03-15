package com.rodbate.server.common;


import com.rodbate.server.ResourceLoad;
import com.rodbate.server.common.annotation.AutoUpdate;
import com.rodbate.server.common.annotation.Inject;

import java.io.File;

@Inject
public class Constant {

    public static final String USER_DIR = System.getProperty("user.dir");

    public static final String CONF_DIR = USER_DIR + File.separator + "conf";

    public static ResourceLoad RESOURCE_LOAD = ResourceLoad.getInstance();

    @AutoUpdate
    public static volatile int DURATION_TIME_MIN = Integer.parseInt((String) RESOURCE_LOAD.getValue(CONF_DIR + File.separator +
            "jvmserver.properties", "durationTime"));



}
