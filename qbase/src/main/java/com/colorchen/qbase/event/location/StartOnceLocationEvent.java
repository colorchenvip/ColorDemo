package com.colorchen.qbase.event.location;

/**
 * Created by wangsye on 2017-11-24.
 */

public class StartOnceLocationEvent {
    public int times;
    public int needAddress;

    public StartOnceLocationEvent(int times, int needAddress) {
        this.times = times;
        this.needAddress = needAddress;
    }
}
