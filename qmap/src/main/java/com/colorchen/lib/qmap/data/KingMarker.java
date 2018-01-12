package com.colorchen.lib.qmap.data;

import android.support.annotation.NonNull;

import com.baidu.mapapi.model.LatLng;

/**
 * name：地图点的数据结构
 * @author: ChenQ
 * @date: 2018-1-4
 */
public class KingMarker<T> {

    private String id;
    public double latitude;
    public double longitude;
    public float rotate;
    private String title;

    private T oriData;


    public KingMarker(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public KingMarker(com.google.android.gms.maps.model.LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }
    public KingMarker(com.amap.api.maps2d.model.LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public KingMarker(String id,double lat, double lng) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lng;
    }

    public KingMarker(double lat, double lng) {
        this.latitude = lat;
        this.longitude = lng;
    }

    public KingMarker(String id, @NonNull double lat, double lng, String title, float rotate) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.title = title;
        this.rotate = rotate;
    }

    public LatLng toBDLatLng() {
        return new LatLng(getLatitude(), getLongitude());
    }

    public com.google.android.gms.maps.model.LatLng toGGLatLng() {
        return new com.google.android.gms.maps.model.LatLng(getLatitude(), getLongitude());
    }

    public com.amap.api.maps2d.model.LatLng toGDLatLng() {
        return new com.amap.api.maps2d.model.LatLng(getLatitude(), getLongitude());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getRotate() {
        return rotate;
    }

    public void setRotate(float rotate) {
        this.rotate = rotate;
    }


    public T getOriData() {
        return oriData;
    }

    public void setOriData(T oriData) {
        this.oriData = oriData;
    }
}
