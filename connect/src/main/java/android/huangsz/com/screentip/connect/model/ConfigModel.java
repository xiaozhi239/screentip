package android.huangsz.com.screentip.connect.model;

import com.google.android.gms.wearable.DataMap;

/**
 * The instance contains the watch face configuration.
 */
public class ConfigModel {

    public static final String KEY_CONFIG_MODEL = "KEY_CONFIG_MODEL";

    public static final String KEY_TICK_COLOR = "KEY_TICK_COLOR";

    public static final String KEY_HAND_COLOR = "KEY_HAND_COLOR";

    public static final String KEY_TEXT = "KEY_TEXT";

    public static final String KEY_BACKGROUND_IMG = "KEY_BACKGROUND_IMG";

    private DataMap mDataMap;

    public ConfigModel(DataMap dataMap) {
        mDataMap = dataMap;
    }

    public ConfigModel() {
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
