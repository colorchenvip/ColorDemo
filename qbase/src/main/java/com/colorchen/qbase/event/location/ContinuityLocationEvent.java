package com.colorchen.qbase.event.location;

/**
 * Created by wangsye on 2017-11-24.
 */

public class ContinuityLocationEvent {
    public enum EventType {
        /**
         * 开启持续定位
         */
        start,
        /**
         * 停止持续定位
         */
        stop,
        /**
         * 获取定位信息
         */
        location,
        /**
         * 获取位置信息，逆地址编码
         */
        address
    }

    public EventType eventType;
    /**
     * 纬度，只有eventType为address时有效
     */
    public double lat;
    /**
     * 经度，只有eventType为address时有效
     */
    public double lng;

    public ContinuityLocationEvent(EventType eventType, double lat, double lng) {
        this.eventType = eventType;
        this.lat = lat;
        this.lng = lng;
    }
}
