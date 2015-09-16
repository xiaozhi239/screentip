package com.huangsz.android.screentip.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Wearable;
import com.huangsz.android.screentip.common.utils.ImageUtils;
import com.huangsz.android.screentip.connect.ConnectManager;
import com.huangsz.android.screentip.connect.model.ConfigModel;
import com.huangsz.android.screentip.connect.model.SnapshotResponseModel;
import com.huangsz.android.screentip.connect.model.TextConfigModel;
import com.huangsz.android.screentip.nodes.NodeMonitor;
import com.huangsz.android.screentip.connect.tasks.LoadBitmapAsyncTask;

/**
 * Control the connection between the configuration activity and the watch face service.
 */
class WatchFaceConfigConnector implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WFConfigConnector";

    private ConfigModel mConfigModel;

    private Bitmap mBackgroundImage;

    private GoogleApiClient mGoogleApiClient;

    private NodeMonitor mNodeMonitor;

    private Handler mUiHandler;

    LoadBitmapAsyncTask.PostExecuteCallback mLoadSnapshotCallback
            = new LoadBitmapAsyncTask.PostExecuteCallback() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap) {
            Message message = new Message();
            message.what = WatchFaceConfigActivity.ConfigHandler.MESSAGE_SNAPSHOT_LOADED;
            message.obj = bitmap;
            mUiHandler.sendMessage(message);
        }
    };

    WatchFaceConfigConnector(Context context, NodeMonitor nodeMonitor, Handler uiHandler) {
        mNodeMonitor = nodeMonitor;
        mUiHandler = uiHandler;
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
            Asset asset = ImageUtils.compressAndCreateAssetFromBitmap(mBackgroundImage);
            getConfigModel().getDataMap().putAsset(ConfigModel.KEY_BACKGROUND_IMG, asset);
        }
        if (!getConfigModel().isEmpty()) {
            ConnectManager.getInstance().sendConfigModel(mGoogleApiClient, getConfigModel());
            resetConfig();
        }
    }

    /**
     * Send a snapshot request to watch. Note that you should check if there is any available node
     * before calling this method.
     */
    public void sendSnapshotRequestToWatch() {
        if (mNodeMonitor.isEmpty() || mNodeMonitor.getAvailableNode() == null) {
            throw new RuntimeException("There is no available watch connected");
        }
        String nodeId = mNodeMonitor.getAvailableNode().getId();
        ConnectManager.getInstance().sendSnapshotRequest(mGoogleApiClient, nodeId);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Google API connected");
        Wearable.DataApi.addListener(mGoogleApiClient, mOnDataListener);
        NodeMonitor.getInstance().updateConnectedNodes(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Applications should disable UI components that require the service,
        // and wait for a call to onConnected(Bundle) to re-enable them.
        Log.i(TAG, "Google API connection suspended with cause : " + cause);
        Wearable.DataApi.removeListener(mGoogleApiClient, mOnDataListener);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Google API connection failed");
    }

    public void setTextConfigModel(TextConfigModel textConfigModel) {
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

    public boolean isConnectedToWear() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected()
                && mNodeMonitor.hasAvailableNode();
    }

    public ConfigModel getConfigModel() {
        if (mConfigModel == null) {
            mConfigModel = new ConfigModel();
        }
        return mConfigModel;
    }

    private void resetConfig() {
        mConfigModel = null;
    }

    private final DataApi.DataListener mOnDataListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            for (DataEvent event : dataEvents) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    DataItem item = event.getDataItem();
                    processDataItem(item);
                }
            }
            dataEvents.release();
         }
    };

    private void processDataItem(DataItem item) {
        ConnectManager connectManager = ConnectManager.getInstance();
        SnapshotResponseModel snapshotResponse = connectManager.maybeGetSnapshotResponseModel(item);
        if (snapshotResponse != null) {
            new LoadBitmapAsyncTask(mGoogleApiClient, mLoadSnapshotCallback)
                    .execute(snapshotResponse.getSnapshot());
        }
    }
}
