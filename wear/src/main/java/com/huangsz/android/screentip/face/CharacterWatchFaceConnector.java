package com.huangsz.android.screentip.face;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.Wearable;
import com.huangsz.android.screentip.common.data.LoadBitmapAsyncTask;
import com.huangsz.android.screentip.connect.ConnectManager;
import com.huangsz.android.screentip.connect.model.ConfigModel;
import com.huangsz.android.screentip.connect.model.TextConfigModel;
import com.huangsz.android.screentip.feature.FLAGS;

/**
 * Responsible for the watch face related connection and data syncing between wear and handheld.
 */
class CharacterWatchFaceConnector implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "CWatchFaceConnector";

    private GoogleApiClient mGoogleApiClient;

    private CharacterWatchFaceRenderer mWatchFaceRenderer;

    private LoadBitmapAsyncTask.PostExecuteCallback mLoadBitmapCompleteCallback =
            new LoadBitmapAsyncTask.PostExecuteCallback() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap) {
            mWatchFaceRenderer.setBackgroundImage(bitmap);
        }
    };

    public CharacterWatchFaceConnector(Context context,
                                       CharacterWatchFaceRenderer watchFaceRenderer) {
        mWatchFaceRenderer = watchFaceRenderer;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private final DataApi.DataListener mOnDataListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            for (DataEvent event : dataEvents) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    DataItem item = event.getDataItem();
                    processConfigurationFor(item);
                }
            }
            dataEvents.release();
            mWatchFaceRenderer.updateWatchFace();
        }
    };

    private final ResultCallback<DataItemBuffer> mOnConnectedResultCallback =
            new ResultCallback<DataItemBuffer>() {
                // This is only notified when the service is firstly connected.
                @Override
                public void onResult(DataItemBuffer dataItems) {
                    for (DataItem item : dataItems) {
                        processConfigurationFor(item);
                    }
                    dataItems.release();
                    mWatchFaceRenderer.updateWatchFace();
                }
            };

    private void processConfigurationFor(DataItem item) {
        ConfigModel configModel = ConnectManager.getInstance().maybeGetConfigModel(item);
        if (configModel == null) {
            return;
        }
        if (configModel.containsKey(ConfigModel.KEY_TICK_COLOR)) {
            String color = configModel.getDataMap().getString(ConfigModel.KEY_TICK_COLOR);
            mWatchFaceRenderer.setTickColor(Color.parseColor(color));
        }
        if (configModel.containsKey(ConfigModel.KEY_HAND_COLOR)) {
            String color = configModel.getDataMap().getString(ConfigModel.KEY_HAND_COLOR);
            mWatchFaceRenderer.setHandColor(Color.parseColor(color));
        }
        if (configModel.containsKey(TextConfigModel.KEY_TEXT_CONFIG_MODEL) && FLAGS.SCREEN_TEXT) {
            TextConfigModel textModel = configModel.maybeGetTextConfigModel();
            mWatchFaceRenderer.setText(textModel);
        }
        if (configModel.containsKey(ConfigModel.KEY_BACKGROUND_IMG)) {
            Asset asset = configModel.getDataMap().getAsset(ConfigModel.KEY_BACKGROUND_IMG);
            new LoadBitmapAsyncTask(
                    mGoogleApiClient, mLoadBitmapCompleteCallback).execute(asset);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected.");
        Wearable.DataApi.addListener(mGoogleApiClient, mOnDataListener);
        Wearable.DataApi.getDataItems(mGoogleApiClient).setResultCallback(
                mOnConnectedResultCallback);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient suspended with cause: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed.");
    }

    void maybeConnectGoogleApi() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    void maybeDisconnectGoogleApi() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.i(TAG, "GoogleApiClient disconnect");
            Wearable.DataApi.removeListener(mGoogleApiClient, mOnDataListener);
            mGoogleApiClient.disconnect();
        }
    }
}
