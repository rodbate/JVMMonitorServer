package com.dataeye.core;

import com.dataeye.core.util.FeatureCodec;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.dataeye.core.util.GaReflectUtils.*;
import static com.dataeye.core.util.GaStringUtils.newString;
import static java.lang.reflect.Modifier.isStatic;

/**
 * 配置类
 *
 * @author oldmanpushcart@gmail.com
 */
public class Configure {

    private String targetIp;                // 目标主机IP
    private int targetPort;                 // 目标进程号
    private int javaPid;                    // 对方java进程号
    private int connectTimeout = 6000;      // 连接超时时间(ms)
    private String greysCore;
    private String greysAgent;

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public int getJavaPid() {
        return javaPid;
    }

    public void setJavaPid(int javaPid) {
        this.javaPid = javaPid;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getGreysAgent() {
        return greysAgent;
    }

    public void setGreysAgent(String greysAgent) {
        this.greysAgent = greysAgent;
    }

    public String getGreysCore() {
        return greysCore;
    }

    public void setGreysCore(String greysCore) {
        this.greysCore = greysCore;
    }

    // 对象的编码解码器
    private final static FeatureCodec codec = new FeatureCodec(';', '=');

    /**
     * 序列化成字符串
     *
     * @return 序列化字符串
     */
    @Override
    public String toString() {

        final Map<String, String> map = new HashMap<String, String>();
        for (Field field : getFields(Configure.class)) {

            // 过滤掉静态类
            if (isStatic(field.getModifiers())) {
                continue;
            }

            // 非静态的才需要纳入非序列化过程
            try {
                map.put(field.getName(), newString(getValue(this, field)));
            } catch (Throwable t) {
                //
            }

        }

        return codec.toString(map);
    }

    /**
     * 反序列化字符串成对象
     *
     * @param toString 序列化字符串
     * @return 反序列化的对象
     */
    public static Configure toConfigure(String toString) {
        final Configure configure = new Configure();
        final Map<String, String> map = codec.toMap(toString);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                final Field field = getField(Configure.class, entry.getKey());
                if (null != field
                        && !isStatic(field.getModifiers())) {
                    setValue(field, valueOf(field.getType(), entry.getValue()), configure);
                }
            } catch (Throwable t) {
                //
            }
        }
        return configure;
    }

}
