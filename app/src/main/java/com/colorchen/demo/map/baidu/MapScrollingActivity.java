package com.colorchen.demo.map.baidu;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.colorchen.demo.R;
import com.colorchen.demo.UIUtils;
import com.colorchen.demo.base.BaseActivity;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.recyclerview.LQRRecyclerView;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * name：
 *
 * @author ChenQ
 * @date 2018-3-29
 * @des 选择定位
 */
public class MapScrollingActivity extends BaseActivity implements
         OnGetSuggestionResultListener, OnGetGeoCoderResultListener {

    @BindView(R.id.rlMap)
    RelativeLayout mRlMap;
    @BindView(R.id.map_container_layout)
    FrameLayout mapContainerLayout;

    @BindView(R.id.rvPOI)
    LQRRecyclerView mRvPOI;
    @BindView(R.id.pb)
    ProgressBar mPb;

    int maxHeight = UIUtils.dip2Px(300);
    int minHeight = UIUtils.dip2Px(150);
    private int mSelectedPosi = 0;
    private List<SuggestionResult.SuggestionInfo> suggestList = new ArrayList<>();
    private LQRAdapterForRecyclerView<SuggestionResult.SuggestionInfo> mAdapter;

    private Marker myLocation;
    private BaiduMap mBaiduMap;
    private MapView mMapView;
    private LocationClient mLocClient;
    // 是否首次定位
    private boolean isFirstLoc = true;
    private SuggestionSearch mSuggestionSearch = null;
    private GeoCoder mSearch;


    @Override
    protected int layoutId() {
        return R.layout.activity_map_scrolling;
    }

    @Override
    protected void initView() {
        mMapView = new MapView(this);
        mapContainerLayout.removeAllViews();
        FrameLayout.LayoutParams layoutParams
                = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mapContainerLayout.addView(mMapView, layoutParams);
        mBaiduMap = mMapView.getMap();
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setOnMapLoadedCallback(() -> {
            doLbs();
            Logger.i("地图加载成功");
        });
        setRlMapHeight(maxHeight);
        mAdapter = new LQRAdapterForRecyclerView<SuggestionResult.SuggestionInfo>(getApplicationContext(), suggestList, R.layout.item_location_poi) {

            @Override
            public void convert(LQRViewHolderForRecyclerView helper, SuggestionResult.SuggestionInfo item, int position) {
                helper.setText(R.id.tvTitle, item.key).setText(R.id.tvDesc, item.district)
                        .setViewVisibility(R.id.ivSelected, mSelectedPosi == position ? View.VISIBLE : View.GONE);
            }
        };
        mRvPOI.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
            mSelectedPosi = position;
            mAdapter.notifyDataSetChanged();
            Logger.i("选中的位置：" + position);
            addLocationMark(suggestList.get(position).pt,suggestList.get(position).city);
        });
        mRvPOI.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && Math.abs(dy) > 10 && ((GridLayoutManager) mRvPOI.getLayoutManager()).findFirstCompletelyVisibleItemPosition() <= 1 && mRlMap.getHeight() == maxHeight) {
                    Logger.d("上拉缩小");
                    setRlMapHeight(minHeight);
                    UIUtils.postTaskDelay(() -> mRvPOI.moveToPosition(0), 0);
                } else if (dy < 0 && Math.abs(dy) > 10 && ((GridLayoutManager) mRvPOI.getLayoutManager()).findFirstCompletelyVisibleItemPosition() == 1 && mRlMap.getHeight() == minHeight) {
                    Logger.d("下拉放大");
                    setRlMapHeight(maxHeight);
                    UIUtils.postTaskDelay(() -> mRvPOI.moveToPosition(0), 0);
                }
            }
        });

        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
    }

    private void setRlMapHeight(int height) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mRlMap.getLayoutParams();
        params.height = height;
        mRlMap.setLayoutParams(params);
    }

    /**
     * 定位操作
     * 需要先请求定位权限
     */
    private void doLbs() {
        mLocClient = new LocationClient(this);
        if (!mLocClient.isStarted()) {
            mLocClient.start();
        }
        LocationClientOption option = new LocationClientOption();
        // 打开gps
        option.setOpenGps(true);
        // 设置坐标类型
        option.setCoorType("bd09ll");
        //返回当前位置
        option.setIsNeedAddress(true);
        option.setScanSpan(0);
        mLocClient.setLocOption(option);
        mLocClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                    StringBuffer sb = new StringBuffer(256);
                    sb.append("time : ");
                    /**
                     * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                     * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                     */
                    sb.append(location.getTime());
                    sb.append("\nlocType : ");// 定位类型
                    sb.append(location.getLocType());
                    sb.append("\nlocType description : ");// *****对应的定位类型说明*****
                    sb.append(location.getLocTypeDescription());
                    sb.append("\nlatitude : ");// 纬度
                    sb.append(location.getLatitude());
                    sb.append("\nlontitude : ");// 经度
                    sb.append(location.getLongitude());
                    sb.append("\nradius : ");// 半径
                    sb.append(location.getRadius());
                    sb.append("\nCountryCode : ");// 国家码
                    sb.append(location.getCountryCode());
                    sb.append("\nCountry : ");// 国家名称
                    sb.append(location.getCountry());
                    sb.append("\ncitycode : ");// 城市编码
                    sb.append(location.getCityCode());
                    sb.append("\ncity : ");// 城市
                    sb.append(location.getCity());
                    sb.append("\nDistrict : ");// 区
                    sb.append(location.getDistrict());
                    sb.append("\nStreet : ");// 街道
                    sb.append(location.getStreet());
                    sb.append("\naddr : ");// 地址信息
                    sb.append(location.getAddrStr());
                    sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
                    sb.append(location.getUserIndoorState());
                    sb.append("\nDirection(not all devices have value): ");
                    sb.append(location.getDirection());// 方向
                    sb.append("\nlocationdescribe: ");
                    sb.append(location.getLocationDescribe());// 位置语义化信息
                    sb.append("\nPoi: ");// POI信息
                    if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                        for (int i = 0; i < location.getPoiList().size(); i++) {
                            Poi poi = (Poi) location.getPoiList().get(i);
                            sb.append(poi.getName() + ";");
                        }
                    }
                    if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                        sb.append("\nspeed : ");
                        sb.append(location.getSpeed());// 速度 单位：km/h
                        sb.append("\nsatellite : ");
                        sb.append(location.getSatelliteNumber());// 卫星数目
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 海拔高度 单位：米
                        sb.append("\ngps status : ");
                        sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                        sb.append("\ndescribe : ");
                        sb.append("gps定位成功");
                    } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                        // 运营商信息
                        if (location.hasAltitude()) {// *****如果有海拔高度*****
                            sb.append("\nheight : ");
                            sb.append(location.getAltitude());// 单位：米
                        }
                        sb.append("\noperationers : ");// 运营商信息
                        sb.append(location.getOperators());
                        sb.append("\ndescribe : ");
                        sb.append("网络定位成功");
                    } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                        sb.append("\ndescribe : ");
                        sb.append("离线定位成功，离线定位结果也是有效的");
                    } else if (location.getLocType() == BDLocation.TypeServerError) {
                        sb.append("\ndescribe : ");
                        sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                    } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                        sb.append("\ndescribe : ");
                        sb.append("网络不同导致定位失败，请检查网络是否通畅");
                    } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                        sb.append("\ndescribe : ");
                        sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                    }
                    Logger.e(sb.toString());
                }

                if (mLocClient == null || mMapView == null) {
                    return;
                }

                Logger.i("baidu_location" + location.getCity() + "---" + location.getAddrStr() + "经纬度：" + location.getLatitude() + location.getLongitude());
                //定位和地图之间的关联
                MyLocationData locData = new MyLocationData
                        .Builder()
                        .accuracy(location.getRadius())
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);
                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(18.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }


                LatLng tempPosition = new LatLng(location.getLatitude(), location.getLongitude());
                if (myLocation == null) {
                    MarkerOptions ooD = new MarkerOptions().position(tempPosition)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.arm)).animateType(MarkerOptions.MarkerAnimateType.drop).anchor(0.5f, 0.8f);
                    myLocation = (Marker) (mBaiduMap.addOverlay(ooD));
                }
                myLocation.setPosition(tempPosition);
            }
        });
        /*
        * setOnMapStatusChangeListener方法主要是记录当前地图中心点的坐标
        *  mapStatus.target获取当前地图中心点的坐标
        * */
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            LatLng tempPosition;
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                tempPosition = mapStatus.target;
                if (myLocation != null) {
                    myLocation.setPosition(tempPosition);
                }
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                mBaiduMap.clear();
                tempPosition = mapStatus.target;
                if (myLocation != null){
                    MarkerOptions ooD = new MarkerOptions().position(tempPosition)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.arm)).animateType(MarkerOptions.MarkerAnimateType.drop).anchor(0.5f, 0.8f);
                    myLocation = (Marker) (mBaiduMap.addOverlay(ooD));
                }
                mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(tempPosition));
            }
        });
        mLocClient.start();
    }


    @Override
    protected void initData() {
        searchButtonProcess();
    }

    /**
     * 响应城市内搜索按钮点击事件
     */

    public void searchButtonProcess() {
        mPb.setVisibility(View.VISIBLE);
        mRvPOI.setVisibility(View.GONE);
        mSearch.geocode(new GeoCodeOption().city("郑州").address("宇通"));
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();

            return;
        }
    }


    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }
        mPb.setVisibility(View.GONE);
        mRvPOI.setVisibility(View.VISIBLE);

        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info != null) {
                suggestList.add(info);
            }
        }
        mAdapter.setData(suggestList);
        mAdapter.notifyDataSetChanged();
        if (suggestList.size() > 0) {
            addLocationMark(suggestList.get(0).pt,suggestList.get(0).city);
        }

    }

    /**
     * 搜索目标地点
     *
     * @param latLng
     * @param cityStr
     */
    private void addLocationMark(LatLng latLng,String cityStr) {
        mBaiduMap.clear();
        Bundle bundle = new Bundle();
        bundle.putString("city", cityStr);
        MarkerOptions position = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_mark0.png")).extraInfo(bundle)
                .position(latLng);
        mBaiduMap.addOverlay(position);

        if (myLocation != null) {
            myLocation.setPosition(latLng);
        }
    }

    @Override
    protected void onDestroy() {
        mSuggestionSearch.destroy();
        super.onDestroy();
    }
}
