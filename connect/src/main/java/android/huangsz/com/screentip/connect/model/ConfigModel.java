package android.huangsz.com.screentip.connect.model;

import com.google.android.gms.wearable.DataMap;

/**
 * The instance contains the watch face configuration.
 */
public class ConfigModel extends Model {

    public static final String KEY_CONFIG_MODEL = "KEY_CONFIG_MODEL";

    public static final String KEY_TICK_COLOR = "KEY_TICK_COLOR";

    public static final String KEY_HAND_COLOR = "KEY_HAND_COLOR";

    public static final String KEY_TEXT = "KEY_TEXT";

    public static final String KEY_BACKGROUND_IMG = "KEY_BACKGROUND_IMG";

    public ConfigModel() {
        super();
    }

    public ConfigModel(DataMap dataMap) {
        super(dataMap);
    }

    public void setTextConfigModel(TextConfigModel textConfigModel) {
        mDataMap.putDataMap(KEY_TEXT, textConfigModel.getDataMap());
    }

    public TextConfigModel maybeGetTextConfigModel() {
        if (containsKey(KEY_TEXT)) {
            return new TextConfigModel(mDataMap.getDataMap(KEY_TEXT));
        }
        return null;
    }
}
