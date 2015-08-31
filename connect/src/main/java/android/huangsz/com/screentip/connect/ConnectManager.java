package android.huangsz.com.screentip.connect;

import android.huangsz.com.screentip.connect.model.ConfigModel;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Manage class for handling configuration model sending and receiving.
 */
public class ConnectManager {

    private static final String DATA_LAYER_WATCH_FACE_CONFIG_PATH = "/watch_face_config";

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
    public void sendConfigModel(GoogleApiClient googleApiClient, ConfigModel configModel) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            throw new RuntimeException("GoogleApiClient isn't connected");
        }
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(
                DATA_LAYER_WATCH_FACE_CONFIG_PATH);
        putDataMapRequest.getDataMap().putDataMap(ConfigModel.KEY_CONFIG_MODEL,
                configModel.getDataMap());
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, putDataRequest);
    }

    /**
     * Try to get ConfigModel from DataItem. Return null if not applicable.
     */
    public ConfigModel maybeGetConfigModel(DataItem item) {
        if (DATA_LAYER_WATCH_FACE_CONFIG_PATH.equals(item.getUri().getPath())) {
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
            if (dataMap.containsKey(ConfigModel.KEY_CONFIG_MODEL)) {
                return new ConfigModel(dataMap.getDataMap(ConfigModel.KEY_CONFIG_MODEL));
            }
        }
        return null;
    }
}
