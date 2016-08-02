package com.huangsz.android.screentip.connect.model;

import com.google.android.gms.wearable.DataMap;

/**
 * Base model class, which contains information for rendering watch face, it is used for
 * the communication between the mobile and the wear.
 */
public abstract class Model {

    protected DataMap mDataMap;

    public Model(DataMap dataMap) {
        mDataMap = dataMap;
    }

    public Model() {
        this(new DataMap());
    }

    public DataMap getDataMap() {
        return mDataMap;
    }

    public boolean isEmpty() {
        return mDataMap.isEmpty();
    }

    public boolean containsKey(String key) {
        return mDataMap.containsKey(key);
    }

    @Override
    public boolean equals(Object another) {
        if (another instanceof Model && mDataMap != null) {
            return mDataMap.equals(((Model) another).mDataMap);
        }
        return super.equals(another);
    }
}
