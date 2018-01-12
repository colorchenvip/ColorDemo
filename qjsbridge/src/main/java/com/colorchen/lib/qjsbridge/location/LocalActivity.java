package com.colorchen.lib.qjsbridge.location;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.google.gson.Gson;
import com.colorchen.lib.qjsbridge.CallBackResult;
import com.colorchen.lib.qjsbridge.R;
import com.colorchen.lib.qjsbridge.bridge.CompletionHandler;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class LocalActivity extends AppCompatActivity {
    private final String TAG = "LocalActivity";
    public final static String RESULT_LATITUDE = "latitude";
    public final static String RESULT_LONGITUDE = "longitude";
    public final static String RESULT_ADDRESS = "address";

    private ImageView original, centerIcon;

    //初始化时的经纬度和地图滑动时屏幕中央的经纬度
    private LatLng originalLL, currentLL;

    static MapView mMapView = null;
    private GeoCoder mSearch = null;
    private LocationClient mLocClient;// 定位相关
    public MyLocationListenner myListener = new MyLocationListenner();

    private ProgressDialog progressDialog;
    private BaiduMap mBaiduMap;
    private MapStatusUpdate myselfU;

    private String mResultAddress;
    private double mLatitude;
    private double mLongitude;
    private String province;
    private String city;
    private String district;
    private String street;
    private String street_number;
    //当滑动地图时再进行附近搜索
    private boolean changeState = true;
    /**
     * 是否可以拖动修改位置
     */
    public static int changeLocation;

    TextView txt_address;
    private boolean isFirstLoad = true;
    /**
     * 让定位结果只显示一次
     */
    private boolean loacateSuccess = false;


    private FrameLayout titleFrameLayout;
    private ImageView backButton;
    private TextView sendTextView;
    public static CompletionHandler locationHandler;

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class BaiduSDKReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            String st1 = "Network error";
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {

                String st2 = "key validation error!Please on AndroidManifest.xml file check the key set";
                Toast.makeText(LocalActivity.this, st2, Toast.LENGTH_SHORT).show();
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Toast.makeText(LocalActivity.this, st1, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BaiduSDKReceiver mBaiduReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_locale);
        txt_address = (TextView) findViewById(R.id.txt_address);
        titleFrameLayout = (FrameLayout) findViewById(R.id.titleFrameLayout);
        backButton = (ImageView) findViewById(R.id.backButton);
        sendTextView = (TextView) findViewById(R.id.sendTextView);
        backButton.setClickable(true);
        sendTextView.setClickable(true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallBackResult<LocationResult> callBackResult = new CallBackResult<LocationResult>();
                callBackResult.code = 1;
                locationHandler.complete(new Gson().toJson(callBackResult));
                finish();
            }
        });
        sendTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mResultAddress)) {
                    //提示没有地理位置信息
                    Toast.makeText(LocalActivity.this, "没有获取到位置信息！", Toast.LENGTH_SHORT).show();
                } else {
                    CallBackResult<LocationResult> callBackResult = new CallBackResult<LocationResult>();
                    callBackResult.code = 0;
                    LocationResult locationResult = new LocationResult();
                    locationResult.latitude = mLatitude;
                    locationResult.longitude = mLongitude;
                    locationResult.address = mResultAddress;
                    locationResult.province = province;
                    locationResult.city = city;
                    locationResult.district = district;
                    locationResult.streetName = street;
                    locationResult.streetNumber = street_number;
                    callBackResult.data = locationResult;
                    locationHandler.complete(new Gson().toJson(callBackResult));
                    finish();
                }
            }
        });
        init();
    }

    private void init() {
        original = (ImageView) findViewById(R.id.bmap_local_myself);
        mMapView = (MapView) findViewById(R.id.bmap_View);
        mSearch = GeoCoder.newInstance();
        centerIcon = (ImageView) findViewById(R.id.bmap_center_icon);

        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra(RESULT_LATITUDE, 0);
        LocationMode mCurrentMode = LocationMode.NORMAL;
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
        mMapView.setLongClickable(true);
        // 隐藏百度logo ZoomControl
        int count = mMapView.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mMapView.getChildAt(i);
            if (child instanceof ImageView || child instanceof ZoomControls) {
                child.setVisibility(View.INVISIBLE);
            }
        }
        // 隐藏比例尺
        mMapView.showScaleControl(false);
        if (latitude == 0) {
            mMapView = new MapView(this, new BaiduMapOptions());
            mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                    mCurrentMode, true, null));
            mBaiduMap.setMyLocationEnabled(true);
            showMapWithLocationClient();
            setOnclick();
            if (changeLocation == 0) {
                original.setVisibility(View.GONE);
                centerIcon.setVisibility(View.GONE);
                txt_address.setVisibility(View.GONE);
            }
        } else {
            double longtitude = intent.getDoubleExtra(RESULT_LONGITUDE, 0);
            String address = intent.getStringExtra(RESULT_ADDRESS);
            LatLng p = new LatLng(latitude, longtitude);
            mMapView = new MapView(this,
                    new BaiduMapOptions().mapStatus(new MapStatus.Builder()
                            .target(p).build()));
            original.setVisibility(View.GONE);
            centerIcon.setVisibility(View.GONE);
            txt_address.setVisibility(View.GONE);
            showMap(latitude, longtitude, address);
        }

        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mBaiduReceiver = new BaiduSDKReceiver();
        registerReceiver(mBaiduReceiver, iFilter);
    }


    /**
     * 设置点击事件
     */
    private void setOnclick() {
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                changeState = true;
            }
        });
        original.setOnClickListener(new MyOnClickListener());
        mBaiduMap.setOnMapStatusChangeListener(new MyMapStatusChangeListener());
        mSearch.setOnGetGeoCodeResultListener(new MyGetGeoCoderResultListener());
    }

    protected CompositeDisposable compositeDisposable;

    protected void addDisposable(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    /**
     * 根据经纬度进行反地理编码
     */
    private class MyGetGeoCoderResultListener implements OnGetGeoCoderResultListener {

        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                return;
            }
        }

        @Override
        public void onGetReverseGeoCodeResult(final ReverseGeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                return;
            }
            txt_address.post(new Runnable() {
                @Override
                public void run() {
                    txt_address.setText(result.getAddress());
                }
            });

            mResultAddress = result.getAddress();
            if (result.getAddressDetail() != null) {
                province = result.getAddressDetail().province;
                city = result.getAddressDetail().city;
                district = result.getAddressDetail().district;
                street = result.getAddressDetail().street;
                street_number = result.getAddressDetail().streetNumber;
            }
        }
    }

    /**
     * 滑动地图监听地图中心位置变化
     */
    private class MyMapStatusChangeListener implements BaiduMap.OnMapStatusChangeListener {
        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

        }

        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus) {

        }

        @Override
        public void onMapStatusChange(MapStatus mapStatus) {

        }

        @Override
        public void onMapStatusChangeFinish(MapStatus mapStatus) {
            if (changeState) {
                if (isFirstLoad) {
                    originalLL = mapStatus.target;
                    isFirstLoad = false;
                }
                if (changeLocation == 1) {
                    currentLL = mapStatus.target;//地图中心坐标
                    mLongitude = currentLL.longitude;//滑动结束之后设置需要传递的经纬度
                    mLatitude = currentLL.latitude;
                    // 反Geo搜索
                    mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(currentLL));
                }

            }
        }
    }


    /**
     * 查看别人发过来，或者已经发送出去的位置信息
     *
     * @param latitude   维度
     * @param longtitude 经度
     * @param address    详细地址信息
     */
    private void showMap(double latitude, double longtitude, String address) {
        LatLng targetLatLng = new LatLng(latitude, longtitude);
        showOverLay(targetLatLng, address);
    }

    private void showOverLay(final LatLng convertLatLng, String address) {

        Button button = new Button(this);
        button.setText(address);
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        button.setBackgroundResource(R.drawable.ic_location_text_bg);
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.map_descript);
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(button);
        linearLayout.addView(imageView);
        Flowable<BitmapDescriptor> flowable = Flowable.create(new FlowableOnSubscribe<BitmapDescriptor>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<BitmapDescriptor> emitter)
                    throws Exception {
                BitmapDescriptor addressDescriptor = BitmapDescriptorFactory.fromView(linearLayout);
                emitter.onNext(addressDescriptor);
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        Disposable disposable = flowable.subscribeWith(new DisposableSubscriber<BitmapDescriptor>() {
            @Override
            public void onNext(BitmapDescriptor bitmapDescriptor) {
                OverlayOptions addressOverlayOptions = new MarkerOptions().position(convertLatLng)
                        .icon(bitmapDescriptor)
                        .zIndex(4).draggable(true);
                mBaiduMap.addOverlay(addressOverlayOptions);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(convertLatLng, 17.0f);
                mBaiduMap.animateMapStatus(u);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                OverlayOptions iconOverlayOptions = new MarkerOptions().position(convertLatLng)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.map_descript))
                        .zIndex(4).draggable(true);
                mBaiduMap.addOverlay(iconOverlayOptions);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(convertLatLng, 17.0f);
                mBaiduMap.animateMapStatus(u);
            }

            @Override
            public void onComplete() {

            }
        });
        addDisposable(disposable);
    }

    /**
     * 显示当前的位置信息
     */
    private void showMapWithLocationClient() {
        String str1 = "正在刷新";
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(str1);
        progressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                finish();
            }
        });

        progressDialog.show();

        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("gcj02");
        option.setIsNeedAddress(true);
        option.setScanSpan(10000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        if (mLocClient != null) {
            mLocClient.stop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mSearch != null) {
            mSearch.destroy();
        }
        if (mLocClient != null) {
            if (myListener != null) {
                mLocClient.unRegisterLocationListener(myListener);
            }
            mLocClient.stop();
        }
        mMapView.onDestroy();
        unregisterReceiver(mBaiduReceiver);
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }

    /**
     * 监听函数，有新位置的时候，格式化成字符串，输出到屏幕中
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {
            if (location == null) {
                return;
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            mBaiduMap.clear();
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CoordinateConverter converter = new CoordinateConverter();//坐标转换工具类
            converter.coord(ll);//设置源坐标数据
            converter.from(CoordinateConverter.CoordType.COMMON);//设置源坐标类型
            LatLng convertLatLng = converter.convert();
            if (changeLocation == 0) {
                mLongitude = convertLatLng.longitude;//第一定位成功之后设置经纬度，后续经纬度为滑动之后地图的中心位置
                mLatitude = convertLatLng.latitude;
                mResultAddress = location.getAddrStr();
                province = location.getProvince();
                city = location.getCity();
                district = location.getDistrict();
                street = location.getStreet();
                street_number = location.getStreetNumber();
                loacateSuccess = true;
                txt_address.post(new Runnable() {
                    @Override
                    public void run() {
                        txt_address.setText(location.getAddrStr());
                    }
                });
                showOverLay(convertLatLng, location.getAddrStr());
            } else {
                if (!loacateSuccess) {
                    mLongitude = convertLatLng.longitude;//第一定位成功之后设置经纬度，后续经纬度为滑动之后地图的中心位置
                    mLatitude = convertLatLng.latitude;
                    mResultAddress = location.getAddrStr();
                    province = location.getProvince();
                    city = location.getCity();
                    district = location.getDistrict();
                    street = location.getStreet();
                    street_number = location.getStreetNumber();
                    loacateSuccess = true;
                    txt_address.post(new Runnable() {
                        @Override
                        public void run() {
                            txt_address.setText(location.getAddrStr());
                        }
                    });
                    myselfU = MapStatusUpdateFactory.newLatLngZoom(convertLatLng, 17.0f);
                    mBaiduMap.animateMapStatus(myselfU);
                }
            }
        }
    }

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (currentLL != originalLL) {
                changeState = true;
                mBaiduMap.animateMapStatus(myselfU);
            }
        }
    }

}
