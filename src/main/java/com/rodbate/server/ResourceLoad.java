package com.rodbate.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResourceLoad {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoad.class);

    private ResourceLoad () {
        init();
    }

    private static ResourceLoad instance;

    public synchronized static ResourceLoad getInstance(){
        if (instance == null) {
            instance = new ResourceLoad();
        }
        return instance;
    }

    private static ConcurrentMap<String, Map<Object, Object >> properties = new ConcurrentHashMap<>();


    public void init(){
        FileListener.register(new FileUpdate() {
            @Override
            public void update(String fileName) {
                if (fileName.endsWith(".properties")) {
                    reload(fileName);
                }
            }
        });
    }

    private void load(String filePath){
        Objects.requireNonNull(filePath, "File Path can not be null");

        Map<Object, Object> kvs = new HashMap<>();
        InputStream in = null;
        Properties props = new Properties();
        try {
            in = new FileInputStream(filePath);
            props.load(in);
            Enumeration names = props.propertyNames();
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                Object value = props.get(name);
                kvs.put(name, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        properties.putIfAbsent(filePath, kvs);
    }

    private void reload(String filePath){
        LOGGER.info("the file {} auto reload", filePath);
        properties.remove(filePath);
        load(filePath);
    }

    public Object getValue(String filePath, Object key){
        Map<Object, Object> kvs = properties.get(filePath);
        if (kvs == null) {
            load(filePath);
            kvs = properties.get(filePath);
            if (kvs == null) return null;
        }
        return kvs.get(key);
    }
}
