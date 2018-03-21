package com.colorchen.demo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.blankj.utilcode.util.Utils;
import com.colorchen.qbase.utils.HawkUtils;


/**
 * @author ChenQ
 * @name： ColorDemo
 * @date 2018-1-12
 * @email： wxchenq@yutong.com
 * des：
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /*集成三方工具类*/
        Utils.init(this);
        /*加密缓存工具类*/
        HawkUtils.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
