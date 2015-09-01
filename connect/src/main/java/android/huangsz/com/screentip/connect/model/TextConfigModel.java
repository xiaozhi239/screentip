package android.huangsz.com.screentip.connect.model;

import com.google.android.gms.wearable.DataMap;

/**
 * A model containing configurations for text on watch face.
 */
public class TextConfigModel extends Model {

    /**
     * Key for this model.
     */
    public static final String KEY_TEXT_CONFIG_MODEL = "KEY_TEXT_CONFIG_MODEL";

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

    public String getContent() {
        return mDataMap.getString(KEY_TEXT_CONTENT);
    }

    public float getCoordinateX() {
        return mDataMap.getFloat(KEY_COORDINATE_X);
    }

    public float getCoordinateY() {
        return mDataMap.getFloat(KEY_COORDINATE_Y);
    }

    /**
     * Get font, the unit is dp. So it needs to be transferred to pixels when used.
     */
    public int getFont() {
        return mDataMap.getInt(KEY_TEXT_FONT);
    }

    public String getColor() {
        return mDataMap.getString(KEY_TEXT_COLOR);
    }

    public void setContent(String content) {
        mDataMap.putString(KEY_TEXT_CONTENT, content);
    }

    public void setCoordinateX(float x) {
        mDataMap.putFloat(KEY_COORDINATE_X, x);
    }

    public void setCoordinateY(float y) {
        mDataMap.putFloat(KEY_COORDINATE_Y, y);
    }

    /**
     * Set font, the unit is dp. So it needs to be transferred to pixels when used.
     */
    public void setFont(int font) {
        mDataMap.putInt(KEY_TEXT_FONT, font);
    }

    public void setColor(String color) {
        mDataMap.putString(KEY_TEXT_COLOR, color);
    }
}
