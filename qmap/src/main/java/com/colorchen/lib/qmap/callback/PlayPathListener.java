package com.colorchen.lib.qmap.callback;

import android.view.View;

/**
 * Created by zhangbol on 2017-11-24.
 */

public interface PlayPathListener {

    void onStart();

    void onPause();

    void onProgress(int progress);

    void onPlayStop();

    View getView(int index, float angle);

}
