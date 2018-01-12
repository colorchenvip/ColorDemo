package com.colorchen.lib.qmap.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.colorchen.lib.qmap.R;
import com.colorchen.lib.qmap.callback.MapCallback;
import com.colorchen.lib.qmap.callback.PlayPathListener;
import com.colorchen.lib.qmap.data.KingMarker;
import com.colorchen.lib.qmap.map.path.PathPlayManager;
import com.colorchen.lib.qmap.utils.MapUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * name：GoogleMapService
 * @author: ChenQ
 * @date: 2018-1-3
 */
public class GoogleMapService extends AbsMapService implements GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private List<Marker> mGoogleMarkers = new ArrayList<>();

    private Marker mMoveMarker;

    private MapCallback mMapCallback;


    public GoogleMapService(Context context) {
        super(context);
    }

    OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onMapReady(GoogleMap googleMap) {
//            googleMap.setMyLocationEnabled(true);
            mMap = googleMap;
            mMap.setOnMarkerClickListener(GoogleMapService.this);
            if(null != mMapCallback){
                mMapCallback.onMapLoadedCallback();
            }
        }
    };


    @Override
    public IMapService showMap(int containerId, FragmentManager fragmentManager, MapCallback callback) {
        mMapCallback = callback;
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentByTag("google_map");

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(containerId, mapFragment, "google_map");
            fragmentTransaction.commit();
        }

        mapFragment.getMapAsync(onMapReadyCallback);
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
                Marker marker = mMap.addMarker(options);
                marker.setTag(i);
                mGoogleMarkers.add(marker);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        int tag = (int) marker.getTag();
        if (tag >= 0 && null != mAdapter) {
            mAdapter.onMarkerClick(tag);
        }
        return false;
    }

    public void clearOverlay() {
        if (null != mMap) {
            mMap.clear();
        }
//        if(null != pathMarkers && !pathMarkers.isEmpty()){
//            pathMarkers.clear();
//        }
//        if(null != mGoogleMarkers && !mGoogleMarkers.isEmpty()){
//            mGoogleMarkers.clear();
//        }
    }


    @Override
    public void showPath(List<KingMarker> list, boolean autoPlay, int timeInterval, PlayPathListener listener) {
        super.showPath(list, autoPlay, timeInterval, listener);
        mPlayPathListener = listener;
        if (null == mMap) {
            return;
        }
        clearOverlay();
        // A geodesic polyline that goes around the world.
        List<KingMarker> afterConvertMarkers = new ArrayList<>();
        PolylineOptions options = new PolylineOptions();
        options.width(5);
        options.color(Color.BLUE);
        for (int index = 0; index < list.size(); index++) {
            LatLng ggLatLng = MapUtils.gpsToGoogle(list.get(index).latitude, list.get(index).longitude);
            options.add(ggLatLng);
            KingMarker kingMarker = new KingMarker(ggLatLng);
            kingMarker.setRotate(list.get(index).rotate);
            afterConvertMarkers.add(kingMarker);
        }
        mMap.addPolyline(options);
        zoomToSpan(options.getPoints());

        //添加初始位置marker
        MarkerOptions markerOptions;

        markerOptions = new MarkerOptions()
                .flat(true)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start))
                .position(options.getPoints().get(0));
        mMap.addMarker(markerOptions);

        markerOptions = new MarkerOptions()
                .flat(true)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end))
                .position(options.getPoints().get(afterConvertMarkers.size() - 1));
        mMap.addMarker(markerOptions);

        BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(MapUtils.getViewCache(mContext,
                listener.getView(0, afterConvertMarkers.get(0).getRotate())));
        markerOptions = new MarkerOptions()
                .flat(true)
                .anchor(0.1f, 0.5f)
                .icon(bd)
                .position(options.getPoints().get(0));
        mMoveMarker = mMap.addMarker(markerOptions);

        mPathPlayManager = new PathPlayManager(autoPlay, timeInterval, afterConvertMarkers, uiCallBack, listener);
    }


    PathPlayManager.UICallBack uiCallBack = new PathPlayManager.UICallBack() {
        @Override
        public void setPosition(KingMarker marker, int index) {
            if (mMap == null) {
                return;
            }
            mMoveMarker.setPosition(marker.toGGLatLng());
            if (null != mPlayPathListener) {
                BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(MapUtils.getViewCache(mContext,
                        mPlayPathListener.getView(index, marker.getRotate())));
                mMoveMarker.setIcon(bd);
            }
        }
    };


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void saveInstanceState(Bundle outState) {

    }

    @Override
    public void setDefaultLocation(double lat, double lon) {

    }
}
