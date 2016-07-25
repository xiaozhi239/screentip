package com.huangsz.android.screentip.connect.model;

import com.google.android.gms.wearable.DataMap;

/**
 * Model for weather data (temperature, wind, humidity, etc.) and weather configurations.
 */
public class WeatherModel extends Model {

    /**
     * Key for this model
     */
    public static final String KEY_WEATHER_MODEL = "KEY_WEATHER_MODEL";

    public static final String KEY_CURRENT_TEMPERATURE = "KEY_CURRENT_TEMPERATURE";

    public static final String KEY_TEMPERATURE_SCALE = "KEY_TEMPERATURE_SCALE";

    public static final String KEY_TEXT_CONFIG_MODEL = "KEY_TEXT_CONFIG_MODEL";

    public enum Scale {
        Celsius, Fahrenheit
    }

    public WeatherModel() {
        super();
    }

    public WeatherModel(DataMap dataMap) {
        super(dataMap);
    }

    public void setCurrentTemperature(float degree) {
        mDataMap.putFloat(KEY_CURRENT_TEMPERATURE, degree);
    }

    public float getCurrentTemperature() {
        return mDataMap.getFloat(KEY_CURRENT_TEMPERATURE);
    }

    public void setTemperatureScale(Scale scale) {
        mDataMap.putInt(KEY_TEMPERATURE_SCALE, scale.ordinal());
    }

    public Scale getTemperatureScale() {
        return Scale.values()[mDataMap.getInt(KEY_TEMPERATURE_SCALE)];
    }

    /** Set the configuration of weather related texts. */
    public void setTextConfigModel(TextConfigModel textConfigModel) {
        mDataMap.putDataMap(KEY_TEXT_CONFIG_MODEL, textConfigModel.getDataMap());
    }

    public TextConfigModel getTextConfigModel() {
        return new TextConfigModel(mDataMap.getDataMap(KEY_TEXT_CONFIG_MODEL));
    }
}
