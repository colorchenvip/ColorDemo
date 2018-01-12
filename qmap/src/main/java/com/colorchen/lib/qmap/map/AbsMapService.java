package com.colorchen.lib.qmap.map;

import android.content.Context;
import android.database.DataSetObserver;

import com.colorchen.lib.qmap.callback.PlayPathListener;
import com.colorchen.lib.qmap.data.KingMarker;
import com.colorchen.lib.qmap.map.path.PathPlayManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangbol on 2017-11-22.
 */

public abstract class AbsMapService implements IMapService {

    protected final Context mContext;

    protected AbsMarkerAdapter mAdapter;

    private AdapterDataSetObserver mDataSetObserver;

    protected PlayPathListener mPlayPathListener;

    protected List<KingMarker> pathMarkers = new ArrayList<>();

    protected PathPlayManager mPathPlayManager;


    public AbsMapService(Context context) {
        this.mContext = context;
    }


    @Override
    public void setAdapter(AbsMarkerAdapter adapter) {
        this.mAdapter = adapter;
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mDataSetObserver = new AdapterDataSetObserver();
        adapter.registerDataSetObserver(mDataSetObserver);
        showMarkers(adapter);
    }

    @Override
    public void showPath(List<KingMarker> list, boolean autoPlay, int timeInterval, PlayPathListener listener) {
        if(null != list && list.equals(pathMarkers)){
            return;
        }
        this.pathMarkers = list;
        stopPlayPath();
    }

    @Override
    public void startPlayPath() {
        if(null != mPathPlayManager){
            mPathPlayManager.start();
        }
    }

    @Override
    public void pausePlayPath() {
        if(null != mPathPlayManager){
            mPathPlayManager.pause();
        }
    }

    @Override
    public void stopPlayPath() {
        if(null != mPathPlayManager){
            mPathPlayManager.stop();
        }
    }

    @Override
    public void setPathProgress(int progress) {
        if(null != mPathPlayManager){
            mPathPlayManager.setProgress(progress);
        }
    }

    private class AdapterDataSetObserver extends DataSetObserver{
        @Override
        public void onChanged() {
            super.onChanged();
            if(null != mAdapter){
                showMarkers(mAdapter);
            }
        }
    }


}
