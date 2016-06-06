package com.dataeye.common;


import java.nio.file.Path;
import java.nio.file.Paths;

public class CommonUtil {


    //获取上级目录(绝对路径)
    public static String getLastDirectory(String path){
        Path current = Paths.get(path);
        return current.getParent().toAbsolutePath().toString();
    }
}
