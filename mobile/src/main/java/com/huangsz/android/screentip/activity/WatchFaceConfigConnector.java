package com.huangsz.android.screentip.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Control the connection between the configuration activity and the watch face service.
 */
class WatchFaceConfigConnector implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WFConfigConnector";

    private static final String DATA_LAYER_WATCH_FACE_CONFIG_PATH = "/watch_face_config";

    private static final String TAG_CHARACTER_COLOR = "TAG_CHARACTER_COLOR";

    private static final String TAG_TICK_COLOR = "TAG_TICK_COLOR";

    private static final String TAG_CHARACTER_TEXT = "TAG_CHARACTER_TEXT";

    private GoogleApiClient mGoogleApiClient;

    private String mCharacterColor = null;

    private String mCharacterText = null;

    private String mTickColor = null;

    WatchFaceConfigConnector(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }

    public void maybeConnect() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void maybeDisconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void sendConfigChangeToWatch() {
        if (!configChanged()) {
            return;
        }
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            throw new RuntimeException("GoogleApiClient isn't connected");
        }
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(
                DATA_LAYER_WATCH_FACE_CONFIG_PATH);
        if (mCharacterText != null) {
            putDataMapRequest.getDataMap().putString(TAG_CHARACTER_TEXT, mCharacterText);
        }
        if (mCharacterColor != null) {
            putDataMapRequest.getDataMap().putString(TAG_CHARACTER_COLOR, mCharacterColor);
        }
        if (mTickColor != null) {
            putDataMapRequest.getDataMap().putString(TAG_TICK_COLOR, mTickColor);
        }
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
        resetConfig();
    }

    private boolean configChanged() {
        return mCharacterText != null || mCharacterColor != null || mTickColor != null;
    }

    private void resetConfig() {
        mCharacterText = null;
        mCharacterColor = null;
        mTickColor = null;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Applications should disable UI components that require the service,
        // and wait for a call to onConnected(Bundle) to re-enable them.
        Log.e(TAG, "Connection suspended with cause : " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void setCharacterColor(String mCharacterColor) {
        this.mCharacterColor = mCharacterColor;
    }

    public void setCharacterText(String mCharacterText) {
        this.mCharacterText = mCharacterText;
    }

    public void setTickColor(String mTickColor) {
        this.mTickColor = mTickColor;
    }
}
