package com.rodbate.server.common;


import com.rodbate.server.FileListener;
import com.rodbate.server.FileUpdate;
import com.rodbate.server.common.annotation.AutoUpdate;
import com.rodbate.server.common.annotation.Inject;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Set;

public class AutoUpdatePropertiesUtil {

    private static final Logger _LOGGER_STDOUT = LogUtil.getLogger("stdout");

    public static void init(){

        FileListener.register(new FileUpdate() {
            @Override
            public void update(String fileName) {
                if (fileName.endsWith("jvmserver.properties")) {

                    Set<Class<?>> classes = ClassReflection.getClassFilterByAnnotation(Inject.class);

                    for (Class c : classes) {

                        Set<Field> fields = ClassReflection.getFieldsByClassAndAnnotation(c, AutoUpdate.class);

                        for (Field f : fields) {

                            String fieldName = f.getName();

                            Object value = Constant.RESOURCE_LOAD.getValue(Constant.CONF_DIR + File.separator +
                                    "jvmserver.properties", fieldName);

                            ClassReflection.setFiledValue(f, null, value);
                        }
                    }

                }
            }
        });
    }
}
