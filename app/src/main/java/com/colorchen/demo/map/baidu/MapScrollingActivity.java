package com.colorchen.demo.map.baidu;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.colorchen.demo.R;
import com.colorchen.demo.UIUtils;
import com.colorchen.demo.base.BaseActivity;
import com.colorchen.lib.qmap.KingMap;
import com.lqr.recyclerview.LQRRecyclerView;
import com.orhanobut.logger.Logger;

import butterknife.BindView;

/**
 * name：
 * @author ChenQ
 * @date 2018-3-29
 * @des 选择定位
 */
public class MapScrollingActivity extends BaseActivity {

    @BindView(R.id.rlMap)
    RelativeLayout mRlMap;
    @BindView(R.id.map)
    MapView mMap;

    @BindView(R.id.rvPOI)
    LQRRecyclerView mRvPOI;
    @BindView(R.id.pb)
    ProgressBar mPb;

    int maxHeight = UIUtils.dip2Px(300);
    int minHeight = UIUtils.dip2Px(150);
//    private LQRAdapterForRecyclerView<> mAdapter;

    BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private boolean isFirstLoc = true; // 是否首次定位


    @Override
    protected int layoutId() {
        return R.layout.activity_map_scrolling;
    }

    @Override
    protected void initView() {

        mBaiduMap = mMap.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        mBaiduMap.setOnMapLoadedCallback(() -> {
            doLbs();
            Logger.i("地图加载成功");
        });

        setRlMapHeight(maxHeight);
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
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(0);
        mLocClient.setLocOption(option);
        mLocClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (mLocClient == null || mMap == null) {
                    return;
                }
                //定位和地图之间的关联
                MyLocationData locData = new MyLocationData
                        .Builder()
                        .accuracy(location.getRadius())
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);
                if (isFirstLoc) {
                    isFirstLoc = false;
                }
            }
        });
        mLocClient.start();
    }


    @Override
    protected void initData() {

    }

}
