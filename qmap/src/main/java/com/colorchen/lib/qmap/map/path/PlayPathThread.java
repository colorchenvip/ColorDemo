package com.colorchen.lib.qmap.map.path;

import android.os.Handler;
import android.os.Looper;

import com.colorchen.lib.qmap.callback.PlayPathListener;
import com.colorchen.lib.qmap.data.KingMarker;

import java.util.List;

/**
 * Created by zhangbol on 2017-11-27.
 * 这个是百度官方提供的路径播放的实现方式,优势在于可以实现平滑播放(随地图放大缩小速度不变),而且不用提供坐标点的方向
 * 通过计算前后两个坐标点的经纬度,得出当前移动坐标的方向
 * 但是进度反馈快慢会不均
 */
@Deprecated
public class PlayPathThread implements Runnable {

    public static final int SUSPEND_TIME_MILLISECONDS = 50;
    private static final int TIME_INTERVAL = 120;
    private static final double DISTANCE = 0.00002;


    private List<KingMarker> latLngs;

    private String name;
    private Thread mThread;

    private boolean autoPlay = false;

    private String TAG = getName();

    private int onPauseIndex = 0;//暂停时的索引
    private KingMarker onPauseLatLng;//暂停时的经纬度(点)

    private boolean isPause = false;

    private PlayPathListener playPathListener;

    private Handler mHandler;

    private UICallBack uiCallBack;

    public interface UICallBack {

        void setPosition(KingMarker marker, float angle, int index);
    }

    public PlayPathThread(String name, boolean autoPlay, List<KingMarker> latLngs, UICallBack callBack, PlayPathListener playPathListener) {
        this.latLngs = latLngs;
        this.autoPlay = autoPlay;
        this.name = name;
        this.uiCallBack = callBack;
        this.playPathListener = playPathListener;
        mHandler = new Handler(Looper.getMainLooper());
        if (autoPlay) {
            start();
        }
    }

    @Override
    public void run() {


        outer:
        for (int i = onPauseIndex; i < latLngs.size() - 1; i++) {
            final int currentIndex = i;
            final KingMarker startPoint = null == onPauseLatLng ? latLngs.get(i) : onPauseLatLng;
            final KingMarker endPoint = latLngs.get(i + 1);
            onPauseLatLng = null;
            onPauseIndex = 0;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != playPathListener) {
                        playPathListener.onProgress(currentIndex);
                    }
                    if (null != uiCallBack) {
                        uiCallBack.setPosition(startPoint, (float) getAngle(startPoint, endPoint), currentIndex);
                    }
                }
            });

            //计算下一次移动需要的信息
            double slope = getSlope(startPoint, endPoint);
            boolean isReverse = (startPoint.latitude > endPoint.latitude);
            double intercept = getInterception(slope, startPoint);
            double xMoveDistance = isReverse ? getXMoveDistance(slope) : -1 * getXMoveDistance(slope);

            for (double j = startPoint.latitude; !((j > endPoint.latitude) ^ isReverse); j = j - xMoveDistance) {
                KingMarker latLng = null;
                if (slope == Double.MAX_VALUE) {
                    latLng = new KingMarker(j, startPoint.longitude);
                } else {
                    latLng = new KingMarker(j, (j - intercept) / slope);
                }

                final KingMarker finalLatLng = latLng;
                if (isPause) {//突然暂停
                    onPauseIndex = i;
                    onPauseLatLng = finalLatLng;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != playPathListener) {
                                playPathListener.onPause();
                            }
                        }
                    });
                    break outer;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (null != uiCallBack) {
                            uiCallBack.setPosition(finalLatLng, (float) getAngle(startPoint, finalLatLng), currentIndex);
                        }
                    }
                });
                try {
                    Thread.sleep(TIME_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (null != onPauseLatLng) {
            return;
        }
        if (null != mThread) {
            mThread.interrupt();
            mThread = null;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != playPathListener) {
                        playPathListener.onPlayStop();
                    }
                }
            });

        }
    }


    public String getName() {
        return name;
    }


    public Thread getT() {
        return mThread;
    }


    public void start() {
        isPause = false;
        if (null == mThread) {
            mThread = new Thread(this, name);
            mThread.start();
        }
    }


    public void pause() {
        isPause = true;
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    public void stop() {
        pause();
        onPauseIndex = 0;
        onPauseLatLng = null;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null != uiCallBack) {
                    uiCallBack.setPosition(latLngs.get(0), 0,  0);
                }
                if (null != playPathListener) {
                    playPathListener.onPlayStop();
                }
            }
        });

    }

    public void setProgress(int progress) {
        pause();
        onPauseIndex = progress;
        onPauseLatLng = null;
        if ((progress + 1) >= latLngs.size()) {
            //设置到了结束位置
            if (null != playPathListener) {
                playPathListener.onPlayStop();
                if (null != uiCallBack) {
                    uiCallBack.setPosition(latLngs.get(progress), -1, progress);
                }
            }
            return;
        }

        if (null != uiCallBack) {
            uiCallBack.setPosition(latLngs.get(progress), (float) getAngle(latLngs.get(progress), latLngs.get(progress + 1)), progress);
        }
    }

    /**
     * 根据点获取图标转的角度
     */
    private double getAngle(List<KingMarker> markers, int startIndex) {
        if ((startIndex + 1) >= markers.size()) {
            throw new RuntimeException("index out of bonds");
        }
        KingMarker startPoint = markers.get(startIndex);
        KingMarker endPoint = markers.get(startIndex + 1);
        return getAngle(startPoint, endPoint);
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(KingMarker fromPoint, KingMarker toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }


    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, KingMarker point) {

        double interception = point.latitude - slope * point.longitude;
        return interception;
    }

    /**
     * 算斜率
     */
    private double getSlope(KingMarker fromPoint, KingMarker toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;

    }

    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }
}
