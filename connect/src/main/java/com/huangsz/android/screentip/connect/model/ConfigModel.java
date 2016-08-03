package com.huangsz.android.screentip.connect.model;

import android.support.annotation.Nullable;

import com.google.android.gms.wearable.DataMap;

/**
 * The instance contains the watch face configuration.
 */
public class ConfigModel extends Model {

    /**
     * Key for this model.
     */
    public static final String KEY_CONFIG_MODEL = "KEY_CONFIG_MODEL";

    public static final String KEY_TICK_COLOR = "KEY_TICK_COLOR";

    public static final String KEY_HAND_COLOR = "KEY_HAND_COLOR";

    public static final String KEY_BACKGROUND_IMG = "KEY_BACKGROUND_IMG";

    public ConfigModel() {
        super();
    }

    public ConfigModel(DataMap dataMap) {
        super(dataMap);
    }

    public void setTextConfigModel(TextConfigModel textConfigModel) {
        mDataMap.putDataMap(TextConfigModel.KEY_TEXT_CONFIG_MODEL, textConfigModel.getDataMap());
    }

    /**
     * Get the TextConfigModel, return null if not present.
     */
    @Nullable
    public TextConfigModel maybeTextConfigModel() {
        if (containsKey(TextConfigModel.KEY_TEXT_CONFIG_MODEL)) {
            return new TextConfigModel(mDataMap.getDataMap(TextConfigModel.KEY_TEXT_CONFIG_MODEL));
        }
        return null;
    }

    public void setWeatherModel(WeatherModel weatherModel) {
        mDataMap.putDataMap(WeatherModel.KEY_WEATHER_MODEL, weatherModel.getDataMap());
    }

    @Nullable
    public WeatherModel maybeGetWeatherModel() {
        if (containsKey(WeatherModel.KEY_WEATHER_MODEL)) {
            return new WeatherModel(mDataMap.getDataMap(WeatherModel.KEY_WEATHER_MODEL));
        }
        return null;
    }
}
