package com.huangsz.android.screentip.connect;

import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.huangsz.android.screentip.connect.message.MessageConstants;
import com.huangsz.android.screentip.connect.model.ConfigModel;
import com.huangsz.android.screentip.connect.model.SnapshotResponseModel;

/**
 * Manage class for handling model sending and receiving.
 */
public class ConnectManager {

    public static final String DATA_LAYER_WATCH_FACE_CONFIG_PATH = "/watch_face_config";

    /** Key for the DataMap which contains the watch face configuration information.  */
    private static final String KEY_CONFIG_MODEL = "KEY_CONFIG_MODEL";

    /** Key for the response from the watch regarding the snapshot request sent from mobile. */
    private static final String KEY_SNAPSHOT_RESPONSE_MODEL = "KEY_SNAPSHOT_RESPONSE_MODEL";

    private static ConnectManager sInstance = null;

    public static ConnectManager getInstance() {
        if (sInstance == null) {
            sInstance = new ConnectManager();
        }
        return sInstance;
    }

    /**
     * Send ConfigModel to wear.
     */
    public void sendConfigModel(GoogleApiClient googleApiClient, final ConfigModel configModel) {
        new ConnectClient(googleApiClient) {
            @Override
            void putData(DataMap dataMap) {
                dataMap.putDataMap(KEY_CONFIG_MODEL, configModel.getDataMap());
            }
        }.send();
    }

    /**
     * Send a request to watch to take a snapshot of watch face.
     */
    public void sendSnapshotRequest(GoogleApiClient googleApiClient, String nodeId) {
        sendMessage(googleApiClient, nodeId, null);
    }

    /**
     * Send a snapshot response from wear to mobile.
     */
    public void sendSnapshotResponse(GoogleApiClient googleApiClient,
                                     final SnapshotResponseModel response) {
        new ConnectClient(googleApiClient) {
            @Override
            void putData(DataMap dataMap) {
                dataMap.putDataMap(KEY_SNAPSHOT_RESPONSE_MODEL, response.getDataMap());
            }
        }.send();
    }

    /**
     * Try to get ConfigModel from DataItem. Return null if not applicable.
     */
    @Nullable
    public ConfigModel maybeGetConfigModelFromDataItem(DataItem item) {
        DataMap dataMap = getDataMapFromItem(item);
        if (dataMap != null && dataMap.containsKey(KEY_CONFIG_MODEL)) {
            return new ConfigModel(dataMap.getDataMap(KEY_CONFIG_MODEL));
        }
        return null;
    }

    /**
     * Returns if a snapshot request has been made.
     */
    public boolean isSnapshotRequest(MessageEvent messageEvent) {
        return messageEvent.getPath().equals(MessageConstants.MESSAGE_SNAPSHOT_REQUEST);
    }

    @Nullable
    public SnapshotResponseModel maybeGetSnapshotResponseModel(DataItem item) {
        DataMap dataMap = getDataMapFromItem(item);
        if (dataMap != null && dataMap.containsKey(KEY_SNAPSHOT_RESPONSE_MODEL)) {
            return new SnapshotResponseModel(dataMap.getDataMap(KEY_SNAPSHOT_RESPONSE_MODEL));
        }
        return null;
    }

    @Nullable
    private DataMap getDataMapFromItem(DataItem item) {
        if (DATA_LAYER_WATCH_FACE_CONFIG_PATH.equals(item.getUri().getPath())) {
            return DataMapItem.fromDataItem(item).getDataMap();
        }
        return null;
    }

    private void sendMessage(GoogleApiClient googleApiClient, String nodeId,
                             @Nullable byte[] content) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            throw new RuntimeException("GoogleApiClient isn't connected");
        }
        Wearable.MessageApi.sendMessage(googleApiClient, nodeId,
                MessageConstants.MESSAGE_SNAPSHOT_REQUEST, content);
    }
}
