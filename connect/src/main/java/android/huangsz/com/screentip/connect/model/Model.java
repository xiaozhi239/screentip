package android.huangsz.com.screentip.connect.model;

import com.google.android.gms.wearable.DataMap;

/**
 * Base model class.
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
}
