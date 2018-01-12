package com.colorchen.lib.qmap.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.colorchen.lib.qmap.R;
import com.colorchen.lib.qmap.callback.MapCallback;
import com.colorchen.lib.qmap.callback.PlayPathListener;
import com.colorchen.lib.qmap.data.KingMarker;
import com.colorchen.lib.qmap.map.path.PathPlayManager;
import com.colorchen.lib.qmap.utils.MapUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * des：百度地图管理类
 *
 * @author ChenQ
 * @date 2017-11-22
 */
public class BaiDuMapService extends AbsMapService implements BaiduMap.OnMapLoadedCallback, BaiduMap.OnMarkerClickListener, BaiduMap.OnMapClickListener {

    private MapView mapview;
    private BaiduMap mBaiduMap;

    private MyLocationData locData;

    private List<Marker> mBaiduMarkers = new ArrayList<>();


    private Polyline mPolyline;
    private Marker mMoveMarker;

    private MapCallback mMapCallback;


    public BaiDuMapService(Context context) {
        super(context);
    }


    @Override
    public IMapService showMap(int containerId, FragmentManager fragmentManager, MapCallback callback) {
        mMapCallback = callback;
        MapView rootView = new MapView(mContext);
        mBaiduMap = rootView.getMap();
        // 设置点击marker事件监听器
        mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnMapClickListener(this);
        mBaiduMap.setOnMapLoadedCallback(this);
        //默认显示全中国
        newMapCenterPoint(new LatLng(32.8686556726, 106.0634490978), 4);

        this.mapview = rootView;
        mapview.showZoomControls(false);

        ViewGroup containerView = ((Activity) mContext).findViewById(containerId);
        containerView.removeAllViews();
        FrameLayout.LayoutParams layoutParams
                = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        containerView.addView(mapview, layoutParams);



        return this;
    }

    @Override
    public void onMapLoaded() {
        if (null != mMapCallback) {
            mMapCallback.onMapLoadedCallback();
        }
    }


    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mapview == null) {
                return;
            }
//            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(0)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(locData);

            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(16.0f);
//            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        }

    }


    /**
     * 设置新的中心点和级别
     *
     * @param newPoint  中心位置
     * @param zoomLevel 缩放级别
     */
    private void newMapCenterPoint(LatLng newPoint, int zoomLevel) {
        MapStatus mMapStatus = new MapStatus
                .Builder()
                .target(newPoint)
                .zoom(zoomLevel)
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
    }


    @Override
    public void showMarkers(AbsMarkerAdapter adapter) {
        if (null == mBaiduMap) {
            return;
        }
        ArrayList<LatLng> latLngs = new ArrayList<>();
        clearOverlay();
        if (!adapter.isEmpty()) {
            for (int i = 0; i < adapter.getCount(); i++) {
                KingMarker temp = (KingMarker) adapter.getItem(i);
                LatLng latLng = new LatLng(temp.getLatitude(), temp.getLongitude());
                Bundle bundle = new Bundle();
                bundle.putInt("pos", i);
                BitmapDescriptor bd = BitmapDescriptorFactory.fromView(adapter.getView(i));

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .extraInfo(bundle)
                        .animateType(MarkerOptions.MarkerAnimateType.grow)
                        .icon(bd)
                        .zIndex(9);
                Marker marker = (Marker) mBaiduMap.addOverlay(options);
                mBaiduMarkers.add(marker);
                latLngs.add(marker.getPosition());
            }
        }
        zoomToSpan(latLngs);
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Bundle bundle = marker.getExtraInfo();
        if (null != bundle) {
            int temp = bundle.getInt("pos");
            if (null != mAdapter) {
                mAdapter.onMarkerClick(temp);
            }
        }

        return true;
    }

    public void zoomToSpan(List<LatLng> points) {
        if (mBaiduMap == null) {
            return;
        }
        if (points.size() == 0) {
            //默认显示全中国
            newMapCenterPoint(new LatLng(32.8686556726, 106.0634490978), 4);
        } else if (points.size() == 1) {
            newMapCenterPoint(points.get(0), 15);
        } else {
            try {
                // 设置所有maker显示在当前可视区域地图中
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng temp : points) {
                    builder.include(temp);
                }
                int showWidth = MapUtils.getDisplayWidth(mContext) / 2;
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build(),
                        showWidth, showWidth));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void showPath(List<KingMarker> list, boolean autoPlay, int timeInterval, PlayPathListener listener) {
        super.showPath(list, autoPlay, timeInterval, listener);
        if (null == mBaiduMap) {
            return;
        }
        mPlayPathListener = listener;
        clearOverlay();

        List<LatLng> polylines = new ArrayList<>();
        List<KingMarker> afterConvertMarkers = new ArrayList<>();
        for (int index = 0; index < list.size(); index++) {
            //坐标转化为百度坐标
            LatLng bdLatLng = MapUtils.cov(list.get(index).latitude, list.get(index).longitude);
            polylines.add(bdLatLng);
            KingMarker kingMarker = new KingMarker(bdLatLng);
            kingMarker.setRotate(list.get(index).rotate);
            afterConvertMarkers.add(kingMarker);
        }
        PolylineOptions polylineOptions = new PolylineOptions().points(polylines).width(5).color(Color.BLUE);

        mPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);

        OverlayOptions markerOptions;
        markerOptions = new MarkerOptions()
                .flat(true)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start))
                .position(polylines.get(0));
        mBaiduMap.addOverlay(markerOptions);

        markerOptions = new MarkerOptions()
                .flat(true)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end))
                .position(polylines.get(afterConvertMarkers.size() - 1));
        mBaiduMap.addOverlay(markerOptions);

        BitmapDescriptor bd = BitmapDescriptorFactory.fromView(listener.getView(0, afterConvertMarkers.get(0).getRotate()));
        markerOptions = new MarkerOptions()
                .flat(true)
                .anchor(0.1f, 0.5f)
                .icon(bd)
                .position(polylines.get(0));
        mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);

        zoomToSpan(mPolyline.getPoints());

        mPathPlayManager = new PathPlayManager(autoPlay, timeInterval, afterConvertMarkers, uiCallBack, listener);
    }


    PathPlayManager.UICallBack uiCallBack = new PathPlayManager.UICallBack() {
        @Override
        public void setPosition(KingMarker marker, int index) {
            if (mapview == null) {
                return;
            }
            mMoveMarker.setPosition(marker.toBDLatLng());
            if (null != mPlayPathListener) {
                BitmapDescriptor bd = BitmapDescriptorFactory.fromView(mPlayPathListener.getView(index, marker.getRotate()));
                mMoveMarker.setIcon(bd);
            }
        }
    };


    public void clearOverlay() {
        if (null != mBaiduMap) {
            mBaiduMap.clear();
        }
//        if (null != mBaiduMarkers && !mBaiduMarkers.isEmpty()) {
//            mBaiduMarkers.clear();
//        }
//        if(null != pathMarkers && !pathMarkers.isEmpty()){
//            pathMarkers.clear();
//        }
    }


    @Override
    public void onPause() {
        if (null != mapview) {
            mapview.onPause();
        }
    }

    @Override
    public void onResume() {
        if (null != mapview) {
            mapview.onResume();
        }
    }

    @Override
    public void onDestroy() {
        if (null != mapview) {
            mapview.onDestroy();
        }
    }

    @Override
    public void saveInstanceState(Bundle outState) {

    }

    @Override
    public void setDefaultLocation(double lat, double lon) {

    }

}
