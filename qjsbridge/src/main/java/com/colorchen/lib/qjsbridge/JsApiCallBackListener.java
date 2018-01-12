package com.colorchen.lib.qjsbridge;

import com.colorchen.qbase.model.AppLogInfo;

/**
 * Created by wangsye on 2017-8-2.
 */

public interface JsApiCallBackListener {
    void takePictureFromCamera(int needBase64);

    void takePictureFromGallery(int limit, int needBase64);

    void getUserInfo();

    void showNav();

    void hideNav();

    AppLogInfo getAppLogInfo();

    void showRightMenu(String menuTitle, String menuFunction, String imageIcon);

    void changeTitle(String title);
}
