package com.colorchen.lib.qmap;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.colorchen.lib.qmap.MapType.BAIDU;
import static com.colorchen.lib.qmap.MapType.GAODE;
import static com.colorchen.lib.qmap.MapType.GOOGLE;


/**
 * map type
 * Author ChenQ on 2017/11/20
 * email：wxchenq@yutong.com
 */
@IntDef({BAIDU,GOOGLE,GAODE})
@Retention(RetentionPolicy.SOURCE)
public @interface MapType {

    /**
     * 百度地图
     */
    int BAIDU = 0;

    /**
     * google地图
     */
    int GOOGLE = 1;

    /**
     * gao de 地图
     */
    int GAODE = 3;
}
