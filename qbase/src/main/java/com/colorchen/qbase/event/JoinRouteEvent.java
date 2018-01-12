package com.colorchen.qbase.event;

/**
 * Created by wangsye on 2017-9-7.
 */

public class JoinRouteEvent {
    public enum Status {Success, Fail}

    public Status status;
    public String url;
    public String errorInfo;
    public String host;
    public String port;
    public String msg;

    public JoinRouteEvent(Status status, String errorInfo, String url, String msg) {
        this.status = status;
        this.errorInfo = errorInfo;
        this.url = url;
        this.msg = msg;
    }

    public JoinRouteEvent(Status status, String host, String port, String url, String msg) {
        this.status = status;
        this.host = host;
        this.port = port;
        this.url = url;
        this.msg = msg;
    }

}
