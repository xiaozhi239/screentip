package com.huangsz.android.screentip.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.huangsz.com.screentip.connect.ConnectManager;
import android.huangsz.com.screentip.connect.model.ConfigModel;
import android.huangsz.com.screentip.connect.model.TextConfigModel;
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

    private ConfigModel mConfigModel;

    private GoogleApiClient mGoogleApiClient;

    private Bitmap mBackgroundImage;

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
        if (mBackgroundImage != null) {
            Asset asset = compressAndCreateAssetFromImageUri(mBackgroundImage);
            if (asset != null) {
                getConfigModel().getDataMap().putAsset(ConfigModel.KEY_BACKGROUND_IMG, asset);
            }
        }
        if (!getConfigModel().isEmpty()) {
            ConnectManager.getInstance().sendConfigModel(mGoogleApiClient, getConfigModel());
            resetConfig();
        }
    }

    private Asset compressAndCreateAssetFromImageUri(Bitmap image) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private void resetConfig() {
        mConfigModel = null;
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

    public void setText(TextConfigModel textConfigModel) {
        getConfigModel().setTextConfigModel(textConfigModel);
    }

    public void setTickColor(String tickColor) {
        getConfigModel().getDataMap().putString(ConfigModel.KEY_TICK_COLOR, tickColor);
    }

    public void setHandColor(String handColor) {
        getConfigModel().getDataMap().putString(ConfigModel.KEY_HAND_COLOR, handColor);
    }

    public void setBackgroundImage(Bitmap backgroundImage) {
        // Bitmap should be changed to Asset before putting to DataMap, which costs time.
        // So we only do it when it is about to send the DataMap.
        mBackgroundImage = backgroundImage;
    }

    private ConfigModel getConfigModel() {
        if (mConfigModel == null) {
            mConfigModel = new ConfigModel();
        }
        return mConfigModel;
    }
}
