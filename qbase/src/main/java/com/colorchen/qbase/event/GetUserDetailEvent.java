package com.colorchen.qbase.event;


import com.colorchen.qbase.model.UserDetailBean;

/**
 * Created by wangsye on 2017-8-7.
 */

public class GetUserDetailEvent {
    public enum Status {Success, Fail}

    public Status status;
    public String url;
    public Throwable throwable;
    public String msg;

    public GetUserDetailEvent(Status status, String url, String msg, Throwable throwable) {
        this.status = status;
        this.url = url;
        this.throwable = throwable;
        this.msg = msg;
    }

    public GetUserDetailEvent(Status status, UserDetailBean userDetailBean, String url, String msg) {
        this.status = status;
        this.userDetailBean = userDetailBean;
        this.url = url;
        this.msg = msg;
    }

    public UserDetailBean userDetailBean;
}
