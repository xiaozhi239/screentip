package com.huangsz.android.screentip.connect;

import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.huangsz.android.screentip.connect.model.ConfigModel;
import com.huangsz.android.screentip.connect.model.SnapshotResponseModel;
import com.huangsz.android.screentip.connect.monitor.NodeMonitor;

/**
 * Manage class for handling model sending and receiving.
 */
public class ConnectManager {

    public static final String DATA_LAYER_WATCH_FACE_CONFIG_PATH = "/watch_face_config";

    public static final String MESSAGE_LAYER_WATCH_FACE_SNAPSHOT = "/wearable_msg_face_snapshot";

    private static ConnectManager sInstance = null;

    private NodeMonitor mNodeMonitor = NodeMonitor.getInstance();

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
                dataMap.putDataMap(ConfigModel.KEY_CONFIG_MODEL, configModel.getDataMap());
            }
        }.send();
    }

    /**
     * Send a request to watch to take a snapshot of watch face.
     */
    public void sendSnapshotRequest(GoogleApiClient googleApiClient) {
        sendMessage(googleApiClient, null);
    }

    /**
     * Send a snapshot response from wear to mobile.
     */
    public void sendSnapshotResponse(GoogleApiClient googleApiClient,
                                     final SnapshotResponseModel response) {
        new ConnectClient(googleApiClient) {
            @Override
            void putData(DataMap dataMap) {
                dataMap.putDataMap(SnapshotResponseModel.KEY_SNAPSHOT_RESPONSE_MODEL,
                        response.getDataMap());
            }
        }.send();
    }

    /**
     * Try to get ConfigModel from DataItem. Return null if not applicable.
     */
    @Nullable
    public ConfigModel maybeGetConfigModel(DataItem item) {
        DataMap dataMap = getDataMapFromItem(item);
        if (dataMap != null && dataMap.containsKey(ConfigModel.KEY_CONFIG_MODEL)) {
            return new ConfigModel(dataMap.getDataMap(ConfigModel.KEY_CONFIG_MODEL));
        }
        return null;
    }

    /**
     * Returns if a snapshot request has been made.
     */
    public boolean isSnapshotRequest(MessageEvent messageEvent) {
        return messageEvent.getPath().equals(MESSAGE_LAYER_WATCH_FACE_SNAPSHOT);
    }

    @Nullable
    public SnapshotResponseModel maybeGetSnapshotResponseModel(DataItem item) {
        DataMap dataMap = getDataMapFromItem(item);
        if (dataMap != null
                && dataMap.containsKey(SnapshotResponseModel.KEY_SNAPSHOT_RESPONSE_MODEL)) {
            return new SnapshotResponseModel(dataMap.getDataMap(
                    SnapshotResponseModel.KEY_SNAPSHOT_RESPONSE_MODEL));
        }
        return null;
    }

    /**
     * Check if the data item is valid and contains key or not.
     */
    public boolean containsKey(DataItem item, String key) {
        DataMap dataMap = getDataMapFromItem(item);
        return dataMap != null && dataMap.containsKey(key);
    }

    @Nullable
    private DataMap getDataMapFromItem(DataItem item) {
        if (DATA_LAYER_WATCH_FACE_CONFIG_PATH.equals(item.getUri().getPath())) {
            return DataMapItem.fromDataItem(item).getDataMap();
        }
        return null;
    }

    private void sendMessage(GoogleApiClient googleApiClient, @Nullable byte[] content) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            throw new RuntimeException("GoogleApiClient isn't connected");
        }
        if (mNodeMonitor.isEmpty()) {
            throw new IllegalStateException("No device connected");
        }
        String nodeId = mNodeMonitor.getConnectedNodes().get(0).getId();
        Wearable.MessageApi.sendMessage(googleApiClient, nodeId,
                MESSAGE_LAYER_WATCH_FACE_SNAPSHOT, null);
    }
}
