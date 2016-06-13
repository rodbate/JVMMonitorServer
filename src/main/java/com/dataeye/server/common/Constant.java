package com.dataeye.server.common;


import com.dataeye.server.FileListener;
import com.dataeye.server.FileUpdate;
import com.dataeye.server.ResourceLoad;
import com.dataeye.server.common.annotation.AutoUpdate;
import com.dataeye.server.common.annotation.Inject;
import org.slf4j.Logger;

import java.io.File;

@Inject
public class Constant {

    public static final String USER_DIR = System.getProperty("user.dir");

    public static final String CONF_DIR = USER_DIR + File.separator + "conf";

    public static ResourceLoad RESOURCE_LOAD = ResourceLoad.getInstance();

    @AutoUpdate
    public static volatile int DURATION_TIME_MIN = Integer.parseInt((String) RESOURCE_LOAD.getValue(CONF_DIR + File.separator +
            "jvmserver.properties", "durationTime"));

    public static final String ACCROSS_DOMAIN = (String) RESOURCE_LOAD.getValue(CONF_DIR + File.separator +
            "jvmserver.properties", "Access-Control-Allow-Origin");


    private static final Logger _LOGGER_STDOUT = LogUtil.getLogger("stdout");

    static {

        /*FileListener.register(new FileUpdate() {
            @Override
            public void update(String fileName) {
                if (fileName.endsWith("jvmserver.properties")) {

                    try {
                        _LOGGER_STDOUT.info("Before injection CONSTANT DURATION TIME is {}", Constant.DURATION_TIME_MIN);

                        _LOGGER_STDOUT.info("current duration time is {}",
                                Integer.parseInt((String) Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator +
                                        "jvmserver.properties", "durationTime")));
                        DURATION_TIME_MIN = Integer.parseInt((String) Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator +
                                "jvmserver.properties", "durationTime"));

                        _LOGGER_STDOUT.info("After injection CONSTANT DURATION TIME is {}", Constant.DURATION_TIME_MIN);
                    } catch (Exception e) {
                        //_LOGGER_FILE.info(ExceptionStackUtil.print(e));
                    }
                }
            }
        });*/
    }

}
