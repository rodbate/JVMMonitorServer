package com.dataeye;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceLoad {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoad.class);

    private static Map<String, Map<Object, Object >> properties = new ConcurrentHashMap<>();

    static {
        init();
    }

    public static void init(){
        FileListener.register(new FileUpdate() {
            @Override
            public void update(String fileName) {
                if (fileName.endsWith(".properties")) {
                    reload(fileName);
                }
            }
        });
    }

    private static void load(String filePath){
        Objects.requireNonNull(filePath, "File Path can not be null");
        InputStream file = ResourceLoad.class.getClassLoader().getResourceAsStream(filePath);
        Map<Object, Object> kvs = new HashMap<>();

        Properties props = new Properties();
        try {
            props.load(file);
            Enumeration names = props.propertyNames();
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                Object value = props.get(name);
                kvs.put(name, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        properties.put(filePath, kvs);
    }

    private static void reload(String filePath){
        LOGGER.info("the file {} auto reload", filePath);
        properties.remove(filePath);
        load(filePath);
    }

    public static Object getValue(String filePath, Object key){
        Map<Object, Object> kvs = properties.get(filePath);
        if (kvs == null) {
            load(filePath);
            kvs = properties.get(filePath);
            if (kvs == null) return null;
        }
        return kvs.get(key);
    }
}
