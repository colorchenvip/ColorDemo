package com.colorchen.qbase.event;


import com.colorchen.qbase.model.CheckUpgradeResult;

/**
 * Created by wangsye on 2017-8-7.
 */

public class CheckupgradeEvent {
    public enum Status {Success, Fail}

    public Status status;
    public String url;
    public String msg;
    public Throwable throwable;

    public CheckupgradeEvent(Status status, String url, String msg, Throwable throwable) {
        this.status = status;
        this.url = url;
        this.throwable = throwable;
        this.msg = msg;
    }

    public CheckupgradeEvent(Status status, CheckUpgradeResult result, String url, String msg) {
        this.status = status;
        this.checkUpgradeResult = result;
        this.url = url;
        this.msg = msg;
    }

    public CheckUpgradeResult checkUpgradeResult;
}