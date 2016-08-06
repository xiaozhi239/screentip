package com.huangsz.android.screentip.connect.model;

import android.support.annotation.Nullable;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;

/**
 * Response of a snapshot request, which includes the snapshot of watch face.
 */
public class SnapshotResponseModel extends Model {

    private static final String KEY_SNAPSHOT = "KEY_SNAPSHOT";

    public SnapshotResponseModel() {
        super();
    }

    public SnapshotResponseModel(DataMap dataMap) {
        super(dataMap);
    }

    @Nullable
    public Asset maybeGetSnapshot() {
        return mDataMap.getAsset(KEY_SNAPSHOT);
    }

    public void setSnapshot(Asset snapshot) {
        mDataMap.putAsset(KEY_SNAPSHOT, snapshot);
    }
}
