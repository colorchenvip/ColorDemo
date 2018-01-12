package com.colorchen.lib.qmap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.colorchen.lib.qmap.callback.MapCallback;
import com.colorchen.lib.qmap.callback.PlayPathListener;
import com.colorchen.lib.qmap.data.KingMarker;
import com.colorchen.lib.qmap.map.AbsMarkerAdapter;
import com.colorchen.lib.qmap.map.BaiDuMapService;
import com.colorchen.lib.qmap.map.GaoDeMapService;
import com.colorchen.lib.qmap.map.GoogleMapService;
import com.colorchen.lib.qmap.map.IMapService;

import java.util.List;
import java.util.logging.Logger;

/**
 * name：地图主入口
 * @author: ChenQ
 * @date: 2018-1-3
 * @email: wxchenq@yutong.com
 */
public class KingMap {

    private IMapService iMapService;
    private Context mContext;
    private Bundle saveInstanceState;

    public KingMap(Context context) {
        this.mContext = context;
    }

    public IMapService showMap(int mapType, int containerId, FragmentManager fragmentManager, MapCallback mapCallback) {
        if (mapType == MapType.BAIDU) {
            iMapService = new BaiDuMapService(mContext);
        } else if (mapType == MapType.GOOGLE) {
            iMapService = new GoogleMapService(mContext);
        }else {
            iMapService = new GaoDeMapService(mContext,saveInstanceState);
        }
        return iMapService.showMap(containerId, fragmentManager, mapCallback);
    }

    public void showPath(List<KingMarker> list, boolean autoPlay, int timeInterval, PlayPathListener listener) {
        if (null == list || list.size() == 0){
            com.orhanobut.logger.Logger.e("map -- showPath","list 数据为空");
            return;
        }
        if (list.size() > 1) {
            iMapService.showPath(list, autoPlay, timeInterval, listener);
        }else{
            iMapService.setDefaultLocation(list.get(0).getLatitude(),list.get(0).getLongitude());
        }
    }

    public void startPlayPath() {
        iMapService.startPlayPath();
    }

    public void pausePlayPath() {
        iMapService.pausePlayPath();
    }
    public void stopPlayPath() {
        iMapService.stopPlayPath();
    }

    public void setPathProgress(int pathProgress) {
        iMapService.setPathProgress(pathProgress);
    }

    public void setAdapter(AbsMarkerAdapter adapter) {
        iMapService.setAdapter(adapter);
    }

    public void onResume() {
        iMapService.onResume();
    }

    public void onPause() {
        iMapService.onPause();
    }

    public void onDestroy() {
        iMapService.onDestroy();
    }

    public void onCreate(Bundle savedInstanceState){
        this.saveInstanceState = savedInstanceState;
    }

    public void onSaveInstanceState(Bundle outState) {
        iMapService.saveInstanceState(outState);
    }

    public void setDefaultLocation(double lat,double lon){
        setDefaultLocation(lat,lon);
    }
}
