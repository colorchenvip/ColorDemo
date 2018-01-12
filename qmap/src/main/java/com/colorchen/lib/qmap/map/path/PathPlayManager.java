package com.colorchen.lib.qmap.map.path;

import android.os.Handler;
import android.os.Looper;

import com.colorchen.lib.qmap.callback.PlayPathListener;
import com.colorchen.lib.qmap.data.KingMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :  zhangbol
 *         e-mail : zhangbol@yutong.com
 *         time   : 2017-12-13
 *         desc   : 描述信息
 *         version: 1.0
 * 使用Handler的定时任务来实现路径的播放
 */
public class PathPlayManager {

    private static int DEFAULT_TIME_INTERVAL = 500;

    private List<KingMarker> datas = new ArrayList<>();
    private boolean autoPlay;
    private int timeInterval = DEFAULT_TIME_INTERVAL;
    private PlayPathListener playPathListener;
    private Handler mHandler;
    private UICallBack uiCallBack;


    private int mCurrentIndex = 0;
    private boolean isPalying = false;

    public interface UICallBack {

        void setPosition(KingMarker marker, int index);
    }

    public PathPlayManager(boolean autoPlay, int timeInterval, List<KingMarker> latLngs, UICallBack callBack, PlayPathListener playPathListener) {
        this.datas = latLngs;
        this.timeInterval = timeInterval;
        this.autoPlay = autoPlay;
        this.uiCallBack = callBack;
        this.playPathListener = playPathListener;
        mHandler = new Handler(Looper.getMainLooper());
        if(autoPlay){
            start();
        }

    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (null != playPathListener) {
                playPathListener.onProgress(mCurrentIndex);
            }
            if (null != uiCallBack) {
                uiCallBack.setPosition(datas.get(mCurrentIndex), mCurrentIndex);
            }
            if (mCurrentIndex >= datas.size() - 1) {
                mCurrentIndex = 0;
                playPathListener.onPlayStop();
                isPalying = false;
                return;
            }
            mCurrentIndex++;
            mHandler.postDelayed(this, timeInterval);
        }
    };

    public void start() {
        mHandler.postDelayed(runnable, 200);
        isPalying = true;
        if (null != playPathListener) {
            playPathListener.onStart();
        }
    }


    public void pause() {
        mHandler.removeCallbacks(runnable);
        isPalying = false;
        if (null != playPathListener) {
            playPathListener.onPause();
        }
    }

    public void stop() {
        mHandler.removeCallbacks(runnable);
        isPalying = false;
        mCurrentIndex = 0;
        if (null != uiCallBack) {
            uiCallBack.setPosition(datas.get(mCurrentIndex), mCurrentIndex);
        }
        if (null != playPathListener) {
            playPathListener.onProgress(mCurrentIndex);
            playPathListener.onPlayStop();
        }
    }

    public void setProgress(int progress) {
        mHandler.removeCallbacks(runnable);
        mCurrentIndex = progress;
        if (null != uiCallBack) {
            uiCallBack.setPosition(datas.get(mCurrentIndex), mCurrentIndex);
        }
        if (null != playPathListener) {
            playPathListener.onProgress(mCurrentIndex);
        }
        if(isPalying){
            //如果之前就在播放,则继续播放
            mHandler.postDelayed(runnable,200);
        }
    }


}
