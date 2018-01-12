package com.colorchen.lib.qjsbridge;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.colorchen.lib.qjsbridge.bridge.CompletionHandler;
import com.colorchen.lib.qjsbridge.bridge.YTWebView;
import com.colorchen.lib.qjsbridge.location.ContinuityMapBean;
import com.colorchen.lib.qjsbridge.location.LocalActivity;
import com.colorchen.lib.qjsbridge.location.LocationResult;
import com.colorchen.lib.qjsbridge.location.MapBean;
import com.colorchen.lib.qrcode.activity.CommonCaptureActivity;
import com.colorchen.qbase.model.AppLogInfo;
import com.colorchen.qbase.utils.RxUtils;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

/**
 * name：JsApi
 * @author: ChenQ
 * @date: 2018-1-5
 */
public class JsApi {
    public static final int REQUEST_QR_CODE = 1;
    public static final int REQUEST_CODE_TAKE_PICTURE_FROM_CAMERA = 2;
    public static final int REQUEST_CODE_TAKE_PICTURE_FROM_GALLERY = 3;
    public Activity activity;
    public YTWebView webView;
    public Context context;
    public CompletionHandler scanQrcodeHandler;
    public CompletionHandler imagePickerHandler;
    public CompletionHandler locationHandler;
    public int processBackPressed;
    public JsApiCallBackListener jsApiCallBackListener;
    public CompletionHandler userInfoHandler;
    public CompletionHandler continuityLocationHandler;

    /**
     * 权限判断默认6.0以下都是有权限
     *
     * @return
     */


    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    public void setJsApiCallBackListener(JsApiCallBackListener listener) {
        this.jsApiCallBackListener = listener;
    }

    public void setContext(Context context1) {
        this.context = context1;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setWebView(YTWebView webView) {
        this.webView = webView;
    }

    @JavascriptInterface
    public void callPhone(JSONObject jsonObject) throws JSONException {
        String phone = jsonObject.getString("phone");
        if (TextUtils.isEmpty(phone)) {
            return;
        }
        Uri uri = Uri.parse("tel:" + phone);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void sendSms(JSONObject jsonObject) throws JSONException {
        String phone = jsonObject.getString("phone");
        if (TextUtils.isEmpty(phone)) {
            return;
        }
        String smsContent = jsonObject.getString("body");
        if (smsContent == null) {
            smsContent = "";
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
        intent.putExtra("sms_body", smsContent);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void callPhone(JSONObject jsonObject, CompletionHandler handler) throws JSONException {
        CallBackResult<String> callBackResult = new CallBackResult<>();
        String phone = jsonObject.getString("phone");
        if (TextUtils.isEmpty(phone)) {
            callBackResult.code = 1;
            handler.complete(new Gson().toJson(callBackResult));
            return;
        }
        Uri uri = Uri.parse("tel:" + phone);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        callBackResult.code = 0;
        handler.complete(new Gson().toJson(callBackResult));
    }

    @JavascriptInterface
    public void sendSms(JSONObject jsonObject, CompletionHandler handler) throws JSONException {
        String phone = jsonObject.getString("phone");
        CallBackResult<String> callBackResult = new CallBackResult<>();
        if (TextUtils.isEmpty(phone)) {
            callBackResult.code = 1;
            handler.complete(new Gson().toJson(callBackResult));
            return;
        }
        String smsContent = jsonObject.getString("body");
        if (smsContent == null) {
            smsContent = "";
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone));
        intent.putExtra("sms_body", smsContent);
        context.startActivity(intent);
        callBackResult.code = 0;
        handler.complete(new Gson().toJson(callBackResult));
    }

    private void scanQrCode(int needInput, String hint, String title, String inputHint) {
        Intent i = new Intent(activity, CommonCaptureActivity.class);
        Bundle data = new Bundle();
        data.putInt(CommonCaptureActivity.MANUAL_INPUT_EXTRA, needInput);
        data.putString(CommonCaptureActivity.SCAN_HINT_EXTRA, hint);
        data.putString(CommonCaptureActivity.TITLE_EXTRA, title);
        data.putString(CommonCaptureActivity.INPUT_HINT_EXTRA, inputHint);
        i.putExtras(data);
        activity.startActivityForResult(i, REQUEST_QR_CODE);
    }

    /**
     * 扫描条码
     *
     * @param jsonObject
     * @param handler    回调接口
     * @throws JSONException
     */
    @JavascriptInterface
    public void scanQrCode(JSONObject jsonObject, final CompletionHandler handler) throws JSONException {
        scanQrcodeHandler = handler;
        final JSONObject param = jsonObject;
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                RxPermissions rxPermissions = new RxPermissions(activity);
                rxPermissions.request(Manifest.permission.CAMERA)
                        .compose(RxUtils.filterAndroidVersion())
                        .subscribe(aBoolean -> {
                            if (aBoolean) {
                                try {
                                    int needInput = 0;
                                    String title = "";
                                    String hint = "";
                                    String inputHint = "";
                                    try {
                                        needInput = param.getInt("manualInput");
                                        title = param.getString("title");
                                        hint = param.getString("scanHint");
                                        inputHint = param.getString("inputHint");
                                    } catch (Exception e) {
                                    }
                                    scanQrCode(needInput, hint, title, inputHint);
                                } catch (Exception e) {
                                    CallBackResult<String> callBackResult = new CallBackResult<>();
                                    callBackResult.code = 1;
                                    scanQrcodeHandler.complete(new Gson().toJson(callBackResult));
                                }
                            } else {
                                CallBackResult<String> callBackResult = new CallBackResult<>();
                                callBackResult.code = 1;
                                scanQrcodeHandler.complete(new Gson().toJson(callBackResult));
                            }
                        });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @JavascriptInterface
    public void getUser(JSONObject jsonObject, CompletionHandler handler) throws JSONException {
        userInfoHandler = handler;
        if (jsApiCallBackListener != null) {
            jsApiCallBackListener.getUserInfo();
        }
    }

    @JavascriptInterface
    public void takePhoto(JSONObject jsonObject, CompletionHandler handler) throws JSONException {
        this.imagePickerHandler = handler;
        int needBase64 = 0;
        try {
            needBase64 = jsonObject.getInt("needBase64");
            if (needBase64 < 1) {
                needBase64 = 0;
            }
        } catch (Exception e) {
            e.getMessage();
        }
        jsApiCallBackListener.takePictureFromCamera(needBase64);
    }

    @JavascriptInterface
    public void pickPictures(JSONObject jsonObject, CompletionHandler handler) throws JSONException {
        this.imagePickerHandler = handler;
        int limit = 1;
        int needBase64 = 0;
        try {
            limit = jsonObject.getInt("limit");
            if (limit < 1) {
                limit = 1;
            }
            needBase64 = jsonObject.getInt("needBase64");
            if (needBase64 < 1) {
                needBase64 = 0;
            }
        } catch (Exception e) {
        }
        jsApiCallBackListener.takePictureFromGallery(limit, needBase64);
    }

    private void location(JSONObject jsonObject) throws JSONException {
        int times = 1;
        try {
            times = jsonObject.getInt("times");
        } catch (Exception e) {
        }
        int needAddress = 1;
        try {
            needAddress = jsonObject.getInt("address");
        } catch (Exception e) {
        }
        final int shouldHasAddress = needAddress;
        MapBean mapBean = MapBean.getInstance(activity.getApplication());
        mapBean.locationOnce(new MapBean.LocationOnceCallBack() {
            @Override
            public void onResult(double longitude, double latitude, String address, String province, String city, String district, String streetName, String streetNumber) {
                CallBackResult<LocationResult> callBackResult = new CallBackResult<LocationResult>();
                if (longitude == 0 || latitude == 0 || (shouldHasAddress == 1 && TextUtils.isEmpty(address))) {
                    callBackResult.code = 1;
                } else {
                    callBackResult.code = 0;
                    LocationResult locationResult = new LocationResult();
                    locationResult.latitude = latitude;
                    locationResult.longitude = longitude;
                    locationResult.address = address;
                    locationResult.province = province;
                    locationResult.city = city;
                    locationResult.district = district;
                    locationResult.streetName = streetName;
                    locationResult.streetNumber = streetNumber;
                    callBackResult.data = locationResult;
                }
                if (locationHandler != null) {
                    String resultStr = new Gson().toJson(callBackResult);
                    Log.d("JsApi", "location: " + resultStr);
                    locationHandler.complete(resultStr);
                }
            }
        }, times);
    }

    @JavascriptInterface
    public void locationWithoutMap(final JSONObject jsonObject, CompletionHandler handler) throws JSONException {
        this.locationHandler = handler;
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                RxPermissions rxPermissions = new RxPermissions(activity);
                rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                        .compose(RxUtils.filterAndroidVersion())
                        .subscribe(aBoolean -> {
                            if (aBoolean) {
                                try {
                                    location(jsonObject);
                                } catch (Exception e) {
                                    CallBackResult<LocationResult> callBackResult = new CallBackResult<>();
                                    callBackResult.code = 1;
                                    locationHandler.complete(new Gson().toJson(callBackResult));
                                }
                            } else {
                                CallBackResult<LocationResult> callBackResult = new CallBackResult<>();
                                callBackResult.code = 1;
                                locationHandler.complete(new Gson().toJson(callBackResult));
                            }
                        });
            } catch (Throwable e) {
                e.printStackTrace();
            }

        });
    }

    @JavascriptInterface
    public void locationWithMap(final JSONObject jsonObject, CompletionHandler handler) throws JSONException {
        this.locationHandler = handler;
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                RxPermissions rxPermissions = new RxPermissions(activity);
                rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                        .compose(RxUtils.filterAndroidVersion())
                        .subscribe(aBoolean -> {
                            if (aBoolean) {
                                int changeLocation = 0;
                                try {
                                    changeLocation = jsonObject.getInt("move");
                                } catch (Exception e) {
                                }
                                LocalActivity.locationHandler = locationHandler;
                                LocalActivity.changeLocation = changeLocation;
                                Intent intent = new Intent(activity, LocalActivity.class);
                                activity.startActivity(intent);
                            } else {
                                CallBackResult<LocationResult> callBackResult = new CallBackResult<>();
                                callBackResult.code = 1;
                                locationHandler.complete(new Gson().toJson(callBackResult));
                            }
                        });
            } catch (Throwable e) {
            }
        });
    }

    @JavascriptInterface
    public void finishPage(JSONObject jsonObject) {
        if (null != activity) {
            activity.finish();
        }
    }

    @JavascriptInterface
    public void needProcessBackPressed(JSONObject jsonObject) throws JSONException {
        try {
            processBackPressed = jsonObject.getInt("process");
        } catch (Exception e) {
        }
    }


    /**
     * 停止持续定位
     *
     * @param jsonObject
     * @param completionHandler
     */
    @JavascriptInterface
    public void stopContinuityMap(JSONObject jsonObject, CompletionHandler completionHandler) {
        ContinuityMapBean.getInstance(activity.getApplication()).stopLocation(true);
        CallBackResult<LocationResult> callBackResult = new CallBackResult<>();
        callBackResult.code = 0;
        completionHandler.complete(new Gson().toJson(callBackResult));
    }

    /**
     * 获取持续定位的位置信息
     *
     * @param jsonObject
     * @param completionHandler
     */
    @JavascriptInterface
    public void continuityLocation(JSONObject jsonObject, CompletionHandler completionHandler) {
        ContinuityMapBean continuityMapBean = ContinuityMapBean.getInstance(activity.getApplication());
        continuityMapBean.getLocationInfo(completionHandler);
    }

    /**
     * 根据经纬度获取位置信息
     *
     * @param jsonObject
     * @param completionHandler
     */
    @JavascriptInterface
    public void getLocationAddress(JSONObject jsonObject, CompletionHandler completionHandler) {
        ContinuityMapBean continuityMapBean = ContinuityMapBean.getInstance(activity.getApplication());
        double lat = 0.0;
        double lng = 0.0;
        try {
            lat = jsonObject.getDouble("lat");
            lng = jsonObject.getDouble("lng");
        } catch (Exception e) {
        }
        continuityMapBean.getLocationAddress(lat, lng, completionHandler);
    }

    /**
     * 打开持续定位
     */
    private void continuityLocation() {
        ContinuityMapBean continuityMapBean = ContinuityMapBean.getInstance(activity.getApplication());
        continuityMapBean.startContinuityLocation();
        CallBackResult<LocationResult> callBackResult = new CallBackResult<>();
        callBackResult.code = 0;
        continuityLocationHandler.complete(new Gson().toJson(callBackResult));
    }

    @JavascriptInterface
    public void startContinuityMap(JSONObject jsonObject, CompletionHandler completionHandler) {
        this.continuityLocationHandler = completionHandler;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    RxPermissions rxPermissions = new RxPermissions(activity);
                    rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).compose(filterAndroidVersion())
                            .subscribe(aBoolean -> {
                                if (aBoolean) {
                                    try {
                                        continuityLocation();
                                    } catch (Exception e) {
                                        CallBackResult<LocationResult> callBackResult = new CallBackResult<>();
                                        callBackResult.code = 1;
                                        continuityLocationHandler.complete(new Gson().toJson(callBackResult));
                                    }
                                } else {
                                    CallBackResult<LocationResult> callBackResult = new CallBackResult<>();
                                    callBackResult.code = 1;
                                    continuityLocationHandler.complete(new Gson().toJson(callBackResult));
                                }
                            });
                } catch (Throwable e) {
                }
            }
        });
    }

    @JavascriptInterface
    public void showNav(JSONObject jsonObject, CompletionHandler completionHandler) {
        jsApiCallBackListener.showNav();
        CallBackResult<String> callBackResult = new CallBackResult<>();
        callBackResult.code = 0;
        completionHandler.complete(new Gson().toJson(callBackResult));
    }

    @JavascriptInterface
    public void hideNav(JSONObject jsonObject, CompletionHandler completionHandler) {
        jsApiCallBackListener.hideNav();
        CallBackResult<String> callBackResult = new CallBackResult<>();
        callBackResult.code = 0;
        completionHandler.complete(new Gson().toJson(callBackResult));
    }

    @JavascriptInterface
    public void appLogInfo(JSONObject jsonObject, CompletionHandler completionHandler) {
        CallBackResult<AppLogInfo> callBackResult = new CallBackResult<>();
        callBackResult.code = 0;
        AppLogInfo appLogInfo = jsApiCallBackListener.getAppLogInfo();
        callBackResult.data = appLogInfo;
        completionHandler.complete(new Gson().toJson(callBackResult));
    }

    /**
     * 权限判断默认6.0以下都是有权限
     *
     * @return
     */
    public static ObservableTransformer<Boolean, Boolean> filterAndroidVersion() {   //compose判断结果
        return upstream -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return Observable.just(true);
            } else {
                return upstream;
            }
        };
    }

}