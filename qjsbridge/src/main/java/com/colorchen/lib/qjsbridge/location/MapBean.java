package com.colorchen.lib.qjsbridge.location;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

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

public class MapBean {
    private Application context;
    private LocationClient mLOnceClient;
    GeoCoder geoCoder;
    OnGetGeoCoderResultListener getGeoCoderResultListener = new OnGetGeoCoderResultListener() {

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
            Log.d("BDLocationListener", "onGetReverseGeoCodeResult 1");
            if (locationTimes < MAX_LOCATION_TIMES) {
                return;
            }
            if (result != null && result.getLocation() != null && !TextUtils.isEmpty(result.getAddress()) && result.getAddressDetail() != null) {
                String address = result.getAddress();
                ReverseGeoCodeResult.AddressComponent addressComponent = result.getAddressDetail();
                String province = addressComponent.province;
                String city = addressComponent.city;
                String district = addressComponent.district;
                String streetName = addressComponent.street;
                String streetNumber = addressComponent.streetNumber;
                try {
                    Log.d("BDLocationListener", "onGetReverseGeoCodeResult " + address + " " + province + " " + city + " " + " " + district + " " + streetName + " " + streetNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (locationOnceCallBack != null) {
                    locationOnceCallBack.onResult(result.getLocation().longitude, result.getLocation().latitude, address, province, city, district, streetName, streetNumber);
                }
            } else {
                Log.d("BDLocationListener", "onGetReverseGeoCodeResult 没有获取到逆地址信息");
                if (locationOnceCallBack != null) {
                    locationOnceCallBack.onResult(0, 0, "", "", "", "", "", "");
                }
            }
            stopOnceLocation();
        }

        // 从位置转为经纬度
        @Override
        public void onGetGeoCodeResult(GeoCodeResult result) {
        }
    };

    private static MapBean instance_;

    public static MapBean getInstance(Application context) {
        if (instance_ == null) {
            instance_ = new MapBean();
            instance_.context = context;
        }
        return instance_;
    }

    private MapBean() {
    }

    private static int MAX_LOCATION_TIMES = 1;
    private int locationTimes = 1;

    public LocationOnceCallBack locationOnceCallBack;
    private BDLocationListener mLOnceListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            Log.d("onReceiveLocation", "onReceiveLocation");
            if (bdLocation != null) {
                String log = bdLocation.getLongitude() + "," + bdLocation.getLatitude();
                try {
                    log += "," + bdLocation.getAddrStr() + "," + bdLocation.getProvince()
                            + "," + bdLocation.getCity() + "," + bdLocation.getDistrict()
                            + "," + bdLocation.getStreetNumber();
                    Log.d("BDLocationListener", "location: " + log);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //没有街道信息
                if (TextUtils.isEmpty(bdLocation.getAddrStr()) || TextUtils.isEmpty(bdLocation.getProvince())) {
                    LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                    try {
                        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                    } catch (Exception e) {
                        if (locationTimes >= MAX_LOCATION_TIMES) {
                            if (locationOnceCallBack != null) {
                                locationOnceCallBack.onResult(0, 0, "", "", "", "", "", "");
                            }
                            stopOnceLocation();
                            return;
                        }
                    }
                } else {
                    //有街道信息
                    if (locationTimes >= MAX_LOCATION_TIMES) {
                        if (locationOnceCallBack != null) {
                            locationOnceCallBack.onResult(bdLocation.getLongitude(), bdLocation.getLatitude(), bdLocation.getAddrStr(), bdLocation.getProvince(),
                                    bdLocation.getCity() == null ? "" : bdLocation.getCity(),
                                    bdLocation.getDistrict() == null ? "" : bdLocation.getDistrict(),
                                    bdLocation.getStreet() == null ? "" : bdLocation.getStreet(),
                                    bdLocation.getStreetNumber() == null ? "" : bdLocation.getStreetNumber());
                        }
                        stopOnceLocation();
                        return;
                    }
                }
            } else {
                Log.d("BDLocationListener", "没有获取到定位信息");
                //没有获取到经纬度信息
                if (locationTimes >= MAX_LOCATION_TIMES) {
                    if (locationOnceCallBack != null) {
                        locationOnceCallBack.onResult(0, 0, "", "", "", "", "", "");
                    }
                    stopOnceLocation();
                    return;
                }
            }
            locationTimes++;
        }
    };

    public void locationOnce(final LocationOnceCallBack callBack, int maxNumber) {
        MAX_LOCATION_TIMES = maxNumber;
        locationTimes = 1;
        this.locationOnceCallBack = callBack;
        geoCoder = GeoCoder.newInstance();
        // 设置地理编码检索监听者
        geoCoder.setOnGetGeoCodeResultListener(getGeoCoderResultListener);
        mLOnceClient = ContinuityMapBean.getmLOnceClient(context);
        mLOnceClient.registerLocationListener(mLOnceListener);
        if (!mLOnceClient.isStarted()) {
            mLOnceClient.start();
            mLOnceClient.requestLocation();
        }
    }

    public void stopOnceLocation() {
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

            geoCoder.destroy();
        } catch (Exception e) {
            Log.e("MapBean", e.getMessage());
        } finally {
            ContinuityMapBean.getInstance(context).stopLocation(false);
            mLOnceClient = null;
        }
    }

    public interface LocationOnceCallBack {
        void onResult(double longitude, double latitude, String address, String province, String city, String district, String streetName, String streetNumber);
    }

}
