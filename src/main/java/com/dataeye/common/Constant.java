package com.dataeye.common;


import com.dataeye.ResourceLoad;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Constant {

    public static final String USER_DIR = System.getProperty("user.dir");

    public static final String CONF_DIR = CommonUtil.getLastDirectory(Constant.USER_DIR) +
            File.separator + "conf";

    public static ResourceLoad RESOURCE_LOAD = ResourceLoad.getInstance();
}
