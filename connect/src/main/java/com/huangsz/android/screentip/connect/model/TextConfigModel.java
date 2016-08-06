package com.huangsz.android.screentip.connect.model;

import android.support.annotation.Nullable;

import com.google.android.gms.wearable.DataMap;

/**
 * A model containing configurations for text on watch face.
 */
public class TextConfigModel extends Model {

    private static final String KEY_TEXT_CONTENT = "KEY_TEXT_CONTENT";

    private static final String KEY_TEXT_COLOR = "KEY_TEXT_COLOR";

    private static final String KEY_TEXT_SIZE = "KEY_TEXT_SIZE";

    private static final String KEY_COORDINATE_X = "KEY_COORDINATE_X";

    private static final String KEY_COORDINATE_Y = "KEY_COORDINATE_Y";

    public TextConfigModel() {
        super();
    }

    public TextConfigModel(DataMap dataMap) {
        super(dataMap);
    }

    @Nullable
    public String maybeGetContent() {
        return mDataMap.getString(KEY_TEXT_CONTENT);
    }

    @Nullable
    public float maybeGetCoordinateX() {
        return mDataMap.getFloat(KEY_COORDINATE_X);
    }

    @Nullable
    public float maybeGetCoordinateY() {
        return mDataMap.getFloat(KEY_COORDINATE_Y);
    }

    /**
     * Get text size, the unit is dp. So it needs to be transferred to pixels when used.
     */
    @Nullable
    public int maybeGetTextSize() {
        return mDataMap.getInt(KEY_TEXT_SIZE);
    }

    @Nullable
    public String maybeGetColor() {
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
