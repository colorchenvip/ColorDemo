package com.colorchen.lib.qjsbridge.location;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.colorchen.lib.qjsbridge.CallBackResult;
import com.colorchen.lib.qjsbridge.bridge.CompletionHandler;
import com.google.gson.Gson;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

/**
 * name：ContinuityMapBean
 * @author: ChenQ
 * @date: 2018-1-5
 */
public class ContinuityMapBean {
    private Application context;
    private static LocationClient mLOnceClient;
    /**
     * 开启持续定位
     */
    private static boolean hasStartLocation;
    private static ContinuityMapBean instance_;
    private LocationInfo locationInfo;

    /**
     * 存储定位监听
     */


    public static ContinuityMapBean getInstance(Application context) {
        if (instance_ == null) {
            instance_ = new ContinuityMapBean(context);
        }
        return instance_;
    }

    private ContinuityMapBean(Application context) {
        this.context = context;
        init(context);
    }

    private static synchronized void init(Application context) {
        if (mLOnceClient == null) {
            LocationClientOption mOption = new LocationClientOption();
            mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
            mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
            mOption.setScanSpan(1000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
            mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
            mOption.setOpenGps(true);
            mOption.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
            mOption.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
            mOption.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
            mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
            mOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
            mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
            mOption.disableCache(true);
            mOption.setTimeOut(10000);
            mOption.setIsNeedAltitude(false);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
            mLOnceClient = new LocationClient(context);
            mLOnceClient.setLocOption(mOption);
        }
    }

    private BDLocationListener mLOnceListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            Log.d("onReceiveLocation", "onReceiveLocation");
            if (bdLocation != null) {
                try {
                    String log = bdLocation.getLongitude() + "," + bdLocation.getLatitude();
                    Log.d("BDLocationListener", "location: " + log);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                synchronized (ContinuityMapBean.this) {
                    locationInfo.lat = bdLocation.getLatitude();
                    locationInfo.lng = bdLocation.getLongitude();
                    locationInfo.address = bdLocation.getAddrStr();
                }
            } else {
                synchronized (ContinuityMapBean.this) {
                    locationInfo.lat = 0.0;
                    locationInfo.lng = 0.0;
                    locationInfo.address = "";
                }
            }
        }
    };

    public void startContinuityLocation() {
        if (hasStartLocation) {
            stopLocation(true);
        }
        locationInfo = new LocationInfo();
        hasStartLocation = true;
        init(context);
        mLOnceClient.registerLocationListener(mLOnceListener);
        if (!mLOnceClient.isStarted()) {
            mLOnceClient.start();
            mLOnceClient.requestLocation();
        }
    }

    public static LocationClient getmLOnceClient(Application application) {
        init(application);

        return mLOnceClient;
    }

    public void getLocationInfo(CompletionHandler completionHandler) {
        CallBackResult<LocationInfo> callBackResult = new CallBackResult<>();
        callBackResult.code = 0;
        callBackResult.data = locationInfo;
        completionHandler.complete(new Gson().toJson(callBackResult));
    }

    /**
     * 关闭持续定位 true ,关闭一般定位 false
     *
     * @param stopContinueLocaton true
     */
    public void stopLocation(boolean stopContinueLocaton) {
        if (stopContinueLocaton) {
            try {
                LocationClientOption option = new LocationClientOption();
                option.setCoorType("bd09ll"); // 设置坐标类型
                option.setOpenGps(false);
                if (mLOnceClient != null) {
                    mLOnceClient.setLocOption(option);
                }
                if (mLOnceClient != null) {
                    mLOnceClient.stop();
                    mLOnceClient.unRegisterLocationListener(mLOnceListener);
                }
                hasStartLocation = false;
            } catch (Throwable e) {
                Log.e("ContinuityMapBean", e.getMessage());
            } finally {
                mLOnceClient = null;
            }
        } else {
            if (hasStartLocation) {//保持持续定位


            } else {//关闭定位
                try {
                    LocationClientOption option = new LocationClientOption();
                    option.setCoorType("bd09ll"); // 设置坐标类型
                    option.setOpenGps(false);
                    if (mLOnceClient != null) {
                        mLOnceClient.setLocOption(option);
                    }
                    if (mLOnceClient != null) {
                        mLOnceClient.stop();
                        mLOnceClient.unRegisterLocationListener(mLOnceListener);
                    }
                } catch (Throwable e) {
                    Log.e("ContinuityMapBean", e.getMessage());
                } finally {
                    mLOnceClient = null;
                }
            }


        }


    }

    GeoCoder geoCoder;
    OnGetGeoCoderResultListener getGeoCoderResultListener = new OnGetGeoCoderResultListener() {

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
            if (result != null && !TextUtils.isEmpty(result.getAddress())) {
                String address = result.getAddress();
                if (locationAddressCompleteHandler != null) {
                    CallBackResult<String> callBackResult = new CallBackResult<>();
                    callBackResult.code = 0;
                    callBackResult.data = address;
                    locationAddressCompleteHandler.complete(new Gson().toJson(callBackResult));
                }
            } else {
                if (locationAddressCompleteHandler != null) {
                    CallBackResult<String> callBackResult = new CallBackResult<>();
                    callBackResult.code = 1;
                    locationAddressCompleteHandler.complete(new Gson().toJson(callBackResult));
                }
            }
        }

        // 从位置转为经纬度
        @Override
        public void onGetGeoCodeResult(GeoCodeResult result) {
        }
    };
    private CompletionHandler locationAddressCompleteHandler;

    public void getLocationAddress(double lat, double lng, CompletionHandler completionHandler) {
        this.locationAddressCompleteHandler = completionHandler;
        geoCoder = GeoCoder.newInstance();
        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(getGeoCoderResultListener);
        LatLng latLng = new LatLng(lat, lng);
        try {
            geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
        } catch (Exception e) {
            if (locationAddressCompleteHandler != null) {
                CallBackResult<String> callBackResult = new CallBackResult<>();
                callBackResult.code = 1;
                locationAddressCompleteHandler.complete(new Gson().toJson(callBackResult));
            }
        }
    }

    public static class LocationInfo {
        public double lat;
        public double lng;
        public String address;
    }
}
