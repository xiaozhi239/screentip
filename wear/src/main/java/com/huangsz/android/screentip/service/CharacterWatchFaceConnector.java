package com.huangsz.android.screentip.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
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
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for the watch face related connection and data syncing between wear and handheld.
 */
class CharacterWatchFaceConnector implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "CWatchFaceConnector";

    // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
    private static final String DATA_LAYER_WATCHFACE_CONFIG_PATH = "/watch_face_config";

    // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
    private static final String KEY_CHARACTER_COLOR = "KEY_CHARACTER_COLOR";

    // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
    private static final String KEY_TICK_COLOR = "KEY_TICK_COLOR";

    // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
    private static final String KEY_CHARACTER_TEXT = "KEY_CHARACTER_TEXT";

    // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
    private static final String KEY_BACKGROUND_IMG = "KEY_BACKGROUND_IMG";

    private static final long TIMEOUT_BLOCKING_ASSET_MS = 3000;

    private GoogleApiClient mGoogleApiClient;

    private CharacterWatchFaceRenderer mWatchFaceRenderer;

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
        if (DATA_LAYER_WATCHFACE_CONFIG_PATH.equals(item.getUri().getPath())) {
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
            if (dataMap.containsKey(KEY_CHARACTER_COLOR)) {
                String color = dataMap.getString(KEY_CHARACTER_COLOR);
                mWatchFaceRenderer.setCharacterColor(Color.parseColor(color));
            }
            if (dataMap.containsKey(KEY_TICK_COLOR)) {
                String color = dataMap.getString(KEY_TICK_COLOR);
                mWatchFaceRenderer.setTickColor(Color.parseColor(color));
            }
            if (dataMap.containsKey(KEY_CHARACTER_TEXT)) {
                String text = dataMap.getString(KEY_CHARACTER_TEXT);
                mWatchFaceRenderer.setCharacterTip(text);
            }
            if (dataMap.containsKey(KEY_BACKGROUND_IMG)) {
                Asset asset = dataMap.getAsset(KEY_BACKGROUND_IMG);
                new LoadBitMapAsyncTask(mGoogleApiClient).execute(asset);
            }
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

    // TODO(huangsz) Move this to utils, add a callback extending Function in the postexecute.
    private final class LoadBitMapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {

        private GoogleApiClient googleApiClient;

        public LoadBitMapAsyncTask(GoogleApiClient googleApiClient) {
            this.googleApiClient = googleApiClient;
        }

        @Override
        protected Bitmap doInBackground(Asset... params) {
            Asset asset = params[0];
            if (asset == null) {
                throw new IllegalArgumentException("Asset must be non-null");
            }
            if (googleApiClient == null || !googleApiClient.isConnected()) {
                return null;
            }

            ConnectionResult result =
                    googleApiClient.blockingConnect(
                            TIMEOUT_BLOCKING_ASSET_MS, TimeUnit.MILLISECONDS);
            if (!result.isSuccess()) {
                return null;
            }
            // convert asset into a file descriptor and block until it's ready
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    googleApiClient, asset).await().getInputStream();

            if (assetInputStream == null) {
                Log.w(TAG, "Requested an unknown Asset.");
                return null;
            }
            // decode the stream into a bitmap
            return BitmapFactory.decodeStream(assetInputStream);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mWatchFaceRenderer.setBackgroundImage(bitmap);
        }
    }
}
