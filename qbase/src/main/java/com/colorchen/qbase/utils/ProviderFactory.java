package com.colorchen.qbase.utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by wangsye on 2017-6-19.
 */

public class ProviderFactory {
    private static Application context;
    private static String regFileName;
    private static SharedPreferencesUtils preferencesUtils;

    private ProviderFactory() {
    }

    public static void init(Application context, String regFileName) {
        ProviderFactory.context = context;
        ProviderFactory.regFileName = regFileName;
    }

    /**
     * 获取配置文件实例
     *
     * @return 配置文件实例
     */
    public synchronized static SharedPreferencesUtils getSharedPreferenceUtils() {
        if (preferencesUtils == null) {
            preferencesUtils = new SharedPreferencesUtils(context, regFileName);
        }
        return preferencesUtils;
    }

    public static Context getApplicationContext() {
        return context;
    }


}
