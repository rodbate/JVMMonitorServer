package com.rodbate.server.common;

/**
 * 进程信息
 * Created by wendy on 2016/6/6.
 */
public class JpsInfo {

    private int pid;

    private String detail;

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
