package com.colorchen.qbase.event;

/**
 * Created by wangsye on 2017-8-7.
 */

public class LoginEvent {
    public enum Status {Success, Fail, BindError, NOTACTIVE, NOTREGISTERED}

    public Status status;
    public String statusMsg;
    public String url;

    public LoginEvent(Status status) {
        this.status = status;
    }

    public LoginEvent(Status status, String statusMsg, String url) {
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
