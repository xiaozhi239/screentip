package com.huangsz.android.screentip.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

/**
 * Control the connection between the configuration activity and the watch face service.
 */
class WatchFaceConfigConnector implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WFConfigConnector";

    private static final String DATA_LAYER_WATCH_FACE_CONFIG_PATH = "/watch_face_config";

    private static final String KEY_CHARACTER_COLOR = "KEY_CHARACTER_COLOR";

    private static final String KEY_TICK_COLOR = "KEY_TICK_COLOR";

    private static final String KEY_CHARACTER_TEXT = "KEY_CHARACTER_TEXT";

    private static final String KEY_BACKGROUND_IMG = "KEY_BACKGROUND_IMG";

    private GoogleApiClient mGoogleApiClient;

    private String mCharacterColor = null;

    private String mCharacterText = null;

    private String mTickColor = null;

    private Bitmap mBackgroundImage = null;

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
            putDataMapRequest.getDataMap().putString(KEY_CHARACTER_TEXT, mCharacterText);
        }
        if (mCharacterColor != null) {
            putDataMapRequest.getDataMap().putString(KEY_CHARACTER_COLOR, mCharacterColor);
        }
        if (mTickColor != null) {
            putDataMapRequest.getDataMap().putString(KEY_TICK_COLOR, mTickColor);
        }
        if (mBackgroundImage != null) {
            Asset asset = compressAndCreateAssetFromImageUri(mBackgroundImage);
            if (asset != null) {
                putDataMapRequest.getDataMap().putAsset(KEY_BACKGROUND_IMG, asset);
            }
        }
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
        resetConfig();
    }

    private Asset compressAndCreateAssetFromImageUri(Bitmap image) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private boolean configChanged() {
        return mCharacterText != null || mCharacterColor != null || mTickColor != null
                || mBackgroundImage != null;
    }

    private void resetConfig() {
        mCharacterText = null;
        mCharacterColor = null;
        mTickColor = null;
        mBackgroundImage = null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Google API connected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Applications should disable UI components that require the service,
        // and wait for a call to onConnected(Bundle) to re-enable them.
        Log.i(TAG, "Google API connection suspended with cause : " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Google API connection failed");
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

    public void setBackgroundImage(Bitmap backgroundImage) {
        mBackgroundImage = backgroundImage;
    }
}
