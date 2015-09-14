package com.huangsz.android.screentip.connect;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * In charge of the specific sending data operations.
 */
abstract class ConnectClient {

    private GoogleApiClient mGoogleApiClient;

    ConnectClient(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

    void send() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            throw new RuntimeException("GoogleApiClient isn't connected");
        }
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(
                ConnectManager.DATA_LAYER_WATCH_FACE_CONFIG_PATH);
        putData(putDataMapRequest.getDataMap());
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
    }

    abstract void putData(DataMap dataMap);
}
