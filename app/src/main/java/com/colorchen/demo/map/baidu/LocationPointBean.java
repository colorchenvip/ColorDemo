package com.colorchen.demo.map.baidu;

import com.baidu.mapapi.model.LatLng;

/**
 * @author ChenQ
 * @name： ColorDemo
 * @date 2018-3-31
 * @email： wxchenq@yutong.com
 * des：
 */
class LocationPointBean {
    public String address;
    public String addressDes;
    public LatLng latLng;

    public LocationPointBean(LatLng location, String name, String address) {
        this.latLng = location;
        this.address = name;
        this.addressDes = address;
    }
}
