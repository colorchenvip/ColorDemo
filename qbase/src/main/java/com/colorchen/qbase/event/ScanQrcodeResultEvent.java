package com.colorchen.qbase.event;

/**
 * Created by wangsye on 2017-11-23.
 */

public class ScanQrcodeResultEvent {
    public enum Status {Success, fail}

    public Status status;
    public int scanType;
    public String result;

    public ScanQrcodeResultEvent(Status staus, int scanType, String result) {
        this.status = staus;
        this.scanType = scanType;
        this.result = result;
    }
}
