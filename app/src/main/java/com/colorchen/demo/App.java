package com.colorchen.demo;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDex;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.blankj.utilcode.util.Utils;
import com.colorchen.lib.qmap.KingMap;
import com.colorchen.qbase.utils.HawkUtils;


/**
 * @author ChenQ
 * @name： ColorDemo
 * @date 2018-1-12
 * @email： wxchenq@yutong.com
 * des：
 */
public class App extends Application {
    private static Context mContext;
    private static Thread mMainThread;//主线程
    private static long mMainThreadId;//主线程id
    private static Looper mMainLooper;//循环队列
    private static Handler mHandler;//主线程Handler

    public static Context getContext() {
        return mContext;
    }

    public static Handler getMainHandler() {
        return mHandler;
    }

    public static long getMainThreadId() {
        return mMainThreadId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //对全局属性赋值
        mContext = getApplicationContext();
        mMainThread = Thread.currentThread();
        mMainThreadId = android.os.Process.myTid();
        mHandler = new Handler();
        /*集成三方工具类*/
        Utils.init(this);
        /*加密缓存工具类*/
        HawkUtils.init(this);
        /*初始化百度地图*/
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
