package com.huangsz.android.screentip.data.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

/**
 * Manages and provides location information.
 *
 * <pNetworkProvider is enough to provide the accuracy we need for weather.
 */
public class LocationTracker {

    private static final long UPDATE_MIN_INTERVAL_MS = 30 * 60 * 1000;

    private static final long UPDATE_MIN_DISTANCE_METER = 100;

    @Nullable  private static LocationTracker sInstance;

    private final LocationManager mLocationManager;

    @Nullable private Location mLocation;

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    };

    public synchronized static LocationTracker getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LocationTracker(context);
        }
        return sInstance;
    }

    private LocationTracker(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Preconditions.checkState(
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        mLocationManager.requestSingleUpdate(
                LocationManager.NETWORK_PROVIDER, mLocationListener, null);
    }

    public void start() {
        mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                UPDATE_MIN_INTERVAL_MS,
                UPDATE_MIN_DISTANCE_METER,
                mLocationListener);
    }

    public void stop() {
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Nullable
    public Location getLocation() {
        return mLocation;
    }
}
