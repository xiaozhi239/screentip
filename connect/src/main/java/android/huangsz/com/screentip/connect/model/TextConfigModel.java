package android.huangsz.com.screentip.connect.model;

import com.google.android.gms.wearable.DataMap;

/**
 * A model containing configurations for text on watch face.
 */
public class TextConfigModel extends Model {

    public static final String KEY_TEXT_CONFIG = "KEY_TEXT_CONFIG";

    public static final String KEY_TEXT_CONTENT = "KEY_TEXT_CONTENT";

    public static final String KEY_TEXT_COLOR = "KEY_TEXT_COLOR";

    public static final String KEY_TEXT_FONT = "KEY_TEXT_FONT";

    public static final String KEY_COORDINATE_X = "KEY_COORDINATE_X";

    public static final String KEY_COORDINATE_Y = "KEY_COORDINATE_Y";

    public TextConfigModel() {
        super();
    }

    public TextConfigModel(DataMap dataMap) {
        super(dataMap);
    }
}
