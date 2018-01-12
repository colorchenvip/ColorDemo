package com.colorchen.lib.qmap.map;

import com.baidu.mapapi.map.MapView;
import com.colorchen.lib.qmap.MapType;

/**
 * name：MapViewWrapper
 * @author: ChenQ
 * @date: 2018-1-3
 * @email: wxchenq@yutong.com
 */
public class MapViewWrapper<T> {

    private int mapType;

    private T mapView;

    public MapViewWrapper(int mapType, T mapView) {
        this.mapType = mapType;
        this.mapView = mapView;
    }

    public void onPause(){
        if(checkNull()){
            return;
        }
        if(mapType == MapType.BAIDU){
            MapView bdMapView = (MapView) mapView;
            bdMapView.onPause();
        }else if(mapType == MapType.GOOGLE){
//            com.google.android.gms.maps.MapView ggMapView = (com.google.android.gms.maps.MapView) mapView;
//            ggMapView.onPause();
        }
    }

    public void onResume(){
        if(checkNull()){
            return;
        }
        if(mapType == MapType.BAIDU){
            MapView bdMapView = (MapView) mapView;
            bdMapView.onResume();
        }else if(mapType == MapType.GOOGLE){
        }
    }

    public void onDestroy(){
        if(checkNull()){
            return;
        }
        if(mapType == MapType.BAIDU){
            MapView bdMapView = (MapView) mapView;
            bdMapView.onDestroy();
        }else if(mapType == MapType.GOOGLE){
        }
    }

    private boolean checkNull(){
        return null == mapView;
    }
}
