package com.colorchen.demo;

/**
 * @author ChenQ
 * @name： leparent
 * @date 2018-1-17
 * @email： wxchenq@yutong.com
 * des：
 */
public class VideoBean {

    public String vehicleLn;
    public String startStationId = "";


    public VideoBean(String fanNumber, String money) {
        this.vehicleLn = fanNumber;
        this.startStationId = money;
    }

}
