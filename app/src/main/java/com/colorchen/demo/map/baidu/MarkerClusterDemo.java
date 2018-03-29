package com.colorchen.demo.map.baidu;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.colorchen.demo.R;
import com.colorchen.lib.qmap.map.cluster.clusterutil.clustering.ClusterItem;
import com.colorchen.lib.qmap.map.cluster.clusterutil.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

/**
 * name：
 *
 * @author ChenQ
 * @date 2018-3-27
 * @des 多点聚合，多个聚合点
 */
public class MarkerClusterDemo extends AppCompatActivity implements BaiduMap.OnMapLoadedCallback {

    MapView mMapView;
    BaiduMap mBaiduMap;
    MapStatus ms;
    private ClusterManager<MyItem> mClusterManager;

    private final int MAP_STATUS_CHANGE = 100;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MAP_STATUS_CHANGE:
                    MapStatus mapStatus = (MapStatus) msg.obj;
                    if (mapStatus != null) {
                        Log.i("MarkerClusterDemo", "mapStatus=" + mapStatus.toString());
                        // to do :  判断地图状态，进行相应处理
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_cluster_demo);

        mMapView = (MapView) findViewById(R.id.bmapView);
        ms = new MapStatus.Builder().target(new LatLng(35.914935, 117.403119)).zoom(8).build();
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMapLoadedCallback(this);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
        // 定义点聚合管理类ClusterManager
        mClusterManager = new ClusterManager<MyItem>(this, mBaiduMap);
        // 添加Marker点
        addMarkers();
        // 设置地图监听，当地图状态发生改变时，进行点聚合运算
        mBaiduMap.setOnMapStatusChangeListener(mClusterManager);
        // 设置maker点击时的响应
        mBaiduMap.setOnMarkerClickListener(mClusterManager);

        mClusterManager.setOnClusterClickListener(cluster -> {
            Toast.makeText(MarkerClusterDemo.this,
                    "有" + cluster.getSize() + "个点", Toast.LENGTH_SHORT).show();

            List<MyItem> items = (List<MyItem>) cluster.getItems();
            LatLngBounds.Builder builder2 = new LatLngBounds.Builder();
            int i = 0;
            for (MyItem myItem : items) {
                builder2 = builder2.include(myItem.getPosition());
                Log.i("map", "log: i=" + i++ + " pos=" + myItem.getPosition().toString());
            }

            LatLngBounds latlngBounds = builder2.build();
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(latlngBounds, mMapView.getWidth(), mMapView.getHeight());
            mBaiduMap.animateMapStatus(u);
            Log.i("map", "log: mBaiduMap.animateMapStatus(u)");

            return false;
        });

        mClusterManager.setOnClusterItemClickListener(item -> {
            String showText = "点击单个Item";
            if (item.getBundle() != null) {
                showText += " index=" + item.getBundle().getString("index");
            }
            Toast.makeText(MarkerClusterDemo.this,
                    showText, Toast.LENGTH_SHORT).show();

            return false;
        });
        //设置handler
        mClusterManager.setHandler(handler, MAP_STATUS_CHANGE);
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    /**
     * 向地图添加Marker点
     */
    public void addMarkers() {
        // 添加Marker点
        LatLng llA = new LatLng(35.963175, 117.400244);
        LatLng llB = new LatLng(35.952821, 117.399199);
        LatLng llC = new LatLng(35.939723, 117.425541);
        LatLng llD = new LatLng(35.906965, 117.401394);
        LatLng llE = new LatLng(35.956965, 117.331394);
        LatLng llF = new LatLng(35.886965, 117.441394);
        LatLng llG = new LatLng(35.996965, 117.411394);

        Bundle bundleA = new Bundle();
        bundleA.putString("index", "001");
        Bundle bundleB = new Bundle();
        bundleB.putString("index", "002");
        Bundle bundleC = new Bundle();
        bundleC.putString("index", "003");
        List<MyItem> items = new ArrayList<MyItem>();
        items.add(new MyItem(llA, bundleA));
        items.add(new MyItem(llB, bundleB));
        items.add(new MyItem(llC, bundleC));
        items.add(new MyItem(llD));
        items.add(new MyItem(llE));
        items.add(new MyItem(llF));
        items.add(new MyItem(llG));

        mClusterManager.addItems(items);

    }

    /**
     * 每个Marker点，包含Marker点坐标以及图标
     */
    public class MyItem implements ClusterItem {
        private final LatLng mPosition;
        private Bundle mBundle;

        public MyItem(LatLng latLng) {
            mPosition = latLng;
            mBundle = null;
        }

        public MyItem(LatLng latLng, Bundle bundle) {
            mPosition = latLng;
            mBundle = bundle;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public BitmapDescriptor getBitmapDescriptor() {
            int iconId = R.drawable.icon_gcoding;
            if (mBundle != null) {
                if ("001".contentEquals(mBundle.getString("index"))) {
                    iconId = R.drawable.icon_marka;
                } else if ("002".contentEquals(mBundle.getString("index"))) {
                    iconId = R.drawable.icon_markb;
                }
            }

            return BitmapDescriptorFactory
                    .fromResource(iconId);//R.drawable.icon_gcoding);
        }

        public Bundle getBundle() {
            return mBundle;
        }

    }

    @Override
    public void onMapLoaded() {
        // TODO Auto-generated method stub
        ms = new MapStatus.Builder().zoom(9).build();
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
    }

}
