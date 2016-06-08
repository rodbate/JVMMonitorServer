package com.dataeye.common;


import com.dataeye.ResourceLoad;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Constant {

    public static final String USER_DIR = System.getProperty("user.dir");

    public static final String CONF_DIR = USER_DIR + File.separator + "conf";

    public static ResourceLoad RESOURCE_LOAD = ResourceLoad.getInstance();

    public static final int DURATION_TIME_MIN = Integer.parseInt((String) RESOURCE_LOAD.getValue(CONF_DIR + File.separator +
            "jvmserver.properties", "durationTime"));
}
