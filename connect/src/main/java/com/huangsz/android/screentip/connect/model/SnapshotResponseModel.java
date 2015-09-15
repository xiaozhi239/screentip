package com.huangsz.android.screentip.connect.model;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;

/**
 * Response of a snapshot request, which includes the snapshot of watch face.
 */
public class SnapshotResponseModel extends Model {

    /**
     * Key for this model.
     */
    public static final String KEY_SNAPSHOT_RESPONSE_MODEL = "KEY_SNAPSHOT_RESPONSE_MODEL";

    public static final String KEY_SNAPSHOT = "KEY_SNAPSHOT";

    public SnapshotResponseModel() {
        super();
    }

    public SnapshotResponseModel(DataMap dataMap) {
        super(dataMap);
    }

    public Asset getSnapshot() {
        return mDataMap.getAsset(KEY_SNAPSHOT);
    }

    public void setSnapshot(Asset snapshot) {
        mDataMap.putAsset(KEY_SNAPSHOT, snapshot);
    }
}
