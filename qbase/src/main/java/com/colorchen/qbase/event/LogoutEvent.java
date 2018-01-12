package com.colorchen.qbase.event;

/**
 * Created by wangsye on 2017-8-7.
 */

public class LogoutEvent {
    public enum Status {Success, Fail}

    private Status status;
    private String statusMsg;
    private String url;

    public LogoutEvent(Status status) {
        this.status = status;
    }

    public LogoutEvent(Status status, String statusMsg, String url) {
        this.status = status;
        this.statusMsg = statusMsg;
        this.url = url;
    }

    public Status getState() {
        return status;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public String getUrl() {
        return url;
    }
}
