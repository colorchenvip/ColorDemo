package com.colorchen.lib.qmap.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolylineOptions;
import com.colorchen.lib.qmap.R;
import com.colorchen.lib.qmap.callback.MapCallback;
import com.colorchen.lib.qmap.callback.PlayPathListener;
import com.colorchen.lib.qmap.data.KingMarker;
import com.colorchen.lib.qmap.map.path.PathPlayManager;
import com.colorchen.lib.qmap.utils.MapUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * name：高德地图
 *
 * @author: ChenQ
 * @date: 2018-1-3
 */
public class GaoDeMapService extends AbsMapService implements AMap.OnMarkerClickListener {

    private MapView mMap;
    private AMap aMap;

    private List<Marker> mGaoDeMarkers = new ArrayList<>();
    private List<LatLng> mGaoDePiont = new ArrayList<>();

    private Marker mMoveMarker;

    private MapCallback mMapCallback;

    private Bundle saveInstanceState;


    public GaoDeMapService(Context context, Bundle saveInstanceState) {
        super(context);
        this.saveInstanceState = saveInstanceState;
    }

    @Override
    public void setDefaultLocation(double lat, double lon) {
        if (mMap == null) {
            mMap = new MapView(mContext);
            mMap.onCreate(saveInstanceState);
        }
        aMap = mMap.getMap();
        LatLng ggLatLng = MapUtils.gpsToGaoDe(lat, lon);
        List<LatLng> list = new ArrayList<LatLng>();
        list.add(ggLatLng);
        zoomToSpan(list);
    }

    @Override
    public IMapService showMap(int containerId, FragmentManager fragmentManager, MapCallback callback) {
        mMapCallback = callback;
        if (mMap == null) {
            mMap = new MapView(mContext);
            mMap.onCreate(saveInstanceState);
        }
        aMap = mMap.getMap();

        ViewGroup containerView = ((Activity) mContext).findViewById(containerId);
        containerView.removeAllViews();
        FrameLayout.LayoutParams layoutParams
                = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        containerView.addView(mMap, layoutParams);

        return this;
    }

    @Override
    public void showMarkers(AbsMarkerAdapter adapter) {
        if (null == mMap) {
            return;
        }
        clearOverlay();
        ArrayList<LatLng> latLngs = new ArrayList<>();

        if (!adapter.isEmpty()) {
            for (int i = 0; i < adapter.getCount(); i++) {
                KingMarker temp = (KingMarker) adapter.getItem(i);

                LatLng latLng = new LatLng(temp.getLatitude(), temp.getLongitude());
                BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(MapUtils.getViewCache(mContext,
                        adapter.getView(i)));
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .icon(bd)
                        .zIndex(9);
                Marker marker = aMap.addMarker(options);
                marker.setPeriod(i);
                mGaoDeMarkers.add(marker);
                latLngs.add(marker.getPosition());
            }
        }
        zoomToSpan(latLngs);
    }


    private void zoomToSpan(List<LatLng> latLngs) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng temp : latLngs) {
            builder.include(temp);
        }
        LatLngBounds bounds = builder.build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        int tag = marker.getPeriod();
        if (tag >= 0 && null != mAdapter) {
            mAdapter.onMarkerClick(tag);
        }
        return false;
    }

    public void clearOverlay() {
        if (null != mMap) {
            aMap.clear();
        }
    }


    @Override
    public void showPath(List<KingMarker> list, boolean autoPlay, int timeInterval, PlayPathListener listener) {
        super.showPath(list, autoPlay, timeInterval, listener);
        mPlayPathListener = listener;
        if (null == aMap) {
            return;
        }
        clearOverlay();
        // A geodesic polyline that goes around the world.
        List<KingMarker> afterConvertMarkers = new ArrayList<>();
        PolylineOptions options = new PolylineOptions();
        options.width(10);
        options.color(Color.parseColor("#22b14c"));

        mGaoDePiont.clear();
        for (int index = 0; index < list.size(); index++) {
//            LatLng ggLatLng = MapUtils.gpsToGaoDe(list.get(index).latitude, list.get(index).longitude);
            LatLng ggLatLng = new LatLng(list.get(index).latitude, list.get(index).longitude);
            mGaoDePiont.add(ggLatLng);
            KingMarker kingMarker = new KingMarker(ggLatLng);
            kingMarker.setRotate(list.get(index).rotate);
            afterConvertMarkers.add(kingMarker);
        }
        options.addAll(mGaoDePiont);
        aMap.addPolyline(options);
        zoomToSpan(options.getPoints());

        //添加初始位置marker
        MarkerOptions markerOptions;

        markerOptions = new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start))
                .position(options.getPoints().get(0));
        aMap.addMarker(markerOptions);

        markerOptions = new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end))
                .position(options.getPoints().get(afterConvertMarkers.size() - 1));
        aMap.addMarker(markerOptions);

        if (listener != null) {
          /*  BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(MapUtils.getViewCache(mContext,
                    listener.getView(0, afterConvertMarkers.get(0).getRotate())));
            markerOptions = new MarkerOptions()
                    .anchor(0.1f, 0.5f)
                    .icon(bd)
                    .position(options.getPoints().get(0));*/
        }
        mMoveMarker = aMap.addMarker(markerOptions);

        mPathPlayManager = new PathPlayManager(autoPlay, timeInterval, afterConvertMarkers, uiCallBack, listener);
    }


    PathPlayManager.UICallBack uiCallBack = new PathPlayManager.UICallBack() {
        @Override
        public void setPosition(KingMarker marker, int index) {
            if (mMap == null) {
                return;
            }
            mMoveMarker.setPosition(marker.toGDLatLng());
            if (null != mPlayPathListener) {
                BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(MapUtils.getViewCache(mContext,
                        mPlayPathListener.getView(index, marker.getRotate())));
                mMoveMarker.setIcon(bd);
            }
        }
    };


    @Override
    public void onPause() {
        mMap.onPause();
    }

    @Override
    public void onResume() {
        mMap.onResume();
    }

    @Override
    public void onDestroy() {
        mMap.onDestroy();
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        mMap.onSaveInstanceState(outState);
    }

}
