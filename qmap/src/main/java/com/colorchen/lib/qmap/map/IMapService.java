package com.colorchen.lib.qmap.map;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.colorchen.lib.qmap.callback.MapCallback;
import com.colorchen.lib.qmap.callback.PlayPathListener;
import com.colorchen.lib.qmap.data.KingMarker;

import java.util.List;

public interface IMapService {

    IMapService showMap(int containerId, FragmentManager fragmentManager, MapCallback mapCallback);

    void showPath(List<KingMarker> list, boolean autoPlay, int timeInterval, PlayPathListener listener);

    void startPlayPath();

    void pausePlayPath();

    void stopPlayPath();

    void setPathProgress(int progress);

    void setAdapter(AbsMarkerAdapter adapter);

    void showMarkers(AbsMarkerAdapter adapter);

    void onPause();

    void onResume();

    void onDestroy();

    void saveInstanceState(Bundle outState);

    void setDefaultLocation(double lat,double lon);
}
