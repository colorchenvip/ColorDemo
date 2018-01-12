package com.colorchen.lib.qjsbridge;

import android.app.Application;

/**
 * Created by wangsye on 2017-8-2.
 */

public class JSBridge {
    public static Application application;

    public static void init(Application application) {
        JSBridge.application = application;
    }
}
