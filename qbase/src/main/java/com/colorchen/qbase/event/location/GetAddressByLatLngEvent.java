package com.colorchen.qbase.event.location;

/**
 * Created by wangsye on 2017-11-27.
 */

public class GetAddressByLatLngEvent {
    public enum Status {
        success, fail
    }

    public String address;
    public Status status;

    public GetAddressByLatLngEvent(Status status, String address) {
        this.status = status;
        this.address = address;
    }
}
