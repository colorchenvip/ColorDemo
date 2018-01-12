package com.colorchen.lib.qmap;

import android.content.Context;

import com.colorchen.lib.qmap.callback.LbsCallback;

/**
 * name：KingMapConfig
 * @author: ChenQ
 * @date: 2018-1-3
 */
public class KingMapConfig {

    private Context context;
    private int mapType;
    private boolean needLBS;
    private LbsCallback lbsCallback;


    private KingMapConfig(Builder builder) {
        this.context = builder.context;
        this.mapType = builder.mapType;
        this.needLBS = builder.needLBS;
        this.lbsCallback = builder.lbsCallback;
    }

    public Context getContext() {
        return context;
    }

    public int getMapType() {
        return mapType;
    }

    public boolean isNeedLBS() {
        return needLBS;
    }

    public static class Builder {
        private Context context;
        private int mapType = MapType.GAODE;
        private boolean needLBS = false;
        private LbsCallback lbsCallback;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder mapType(int mapType) {
            this.mapType = mapType;
            return this;
        }

        public Builder needLBS(boolean need, LbsCallback callback) {
            this.needLBS = need;
            this.lbsCallback = callback;
            return this;
        }

        public KingMapConfig build() {
            return new KingMapConfig(this);
        }

    }
}