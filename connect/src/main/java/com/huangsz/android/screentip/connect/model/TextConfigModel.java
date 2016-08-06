package com.huangsz.android.screentip.connect.model;

import com.google.android.gms.wearable.DataMap;

/**
 * A model containing configurations for text on watch face.
 */
public class TextConfigModel extends Model {

    public static final String KEY_TEXT_CONTENT = "KEY_TEXT_CONTENT";

    public static final String KEY_TEXT_COLOR = "KEY_TEXT_COLOR";

    public static final String KEY_TEXT_SIZE = "KEY_TEXT_SIZE";

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
     * Get text size, the unit is dp. So it needs to be transferred to pixels when used.
     */
    public int getTextSize() {
        return mDataMap.getInt(KEY_TEXT_SIZE);
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
     * Set text size, the unit is dp. So it needs to be transferred to pixels when used.
     */
    public void setTextSize(int size) {
        mDataMap.putInt(KEY_TEXT_SIZE, size);
    }

    public void setColor(String color) {
        mDataMap.putString(KEY_TEXT_COLOR, color);
    }
}
