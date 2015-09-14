package com.huangsz.android.screentip.connect;

import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.huangsz.android.screentip.connect.model.ConfigModel;
import com.huangsz.android.screentip.connect.model.SnapshotRequestModel;

/**
 * Manage class for handling model sending and receiving.
 */
public class ConnectManager {

    public static final String DATA_LAYER_WATCH_FACE_CONFIG_PATH = "/watch_face_config";

    private static ConnectManager instance = null;

    public static ConnectManager getInstance() {
        if (instance == null) {
            instance = new ConnectManager();
        }
        return instance;
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
        new ConnectClient(googleApiClient) {
            @Override
            void putData(DataMap dataMap) {
                dataMap.putBoolean(SnapshotRequestModel.KEY_SNAPSHOT_REQUEST_MODEL, true);
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
    public boolean getSnapshotRequest(DataItem item) {
        DataMap dataMap = getDataMapFromItem(item);
        if (dataMap != null && dataMap.containsKey(
                SnapshotRequestModel.KEY_SNAPSHOT_REQUEST_MODEL)) {
            return dataMap.getBoolean(SnapshotRequestModel.KEY_SNAPSHOT_REQUEST_MODEL);
        }
        return false;
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
}
