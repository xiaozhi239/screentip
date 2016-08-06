package com.huangsz.android.screentip.connect.model;

import android.support.annotation.Nullable;

import com.google.android.gms.wearable.Asset;
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

    public void setTickColor(String color) {
        mDataMap.putString(KEY_TICK_COLOR, color);
    }

    @Nullable
    public String maybeGetTickColor() {
        return mDataMap.getString(KEY_TICK_COLOR);
    }

    public void setHandColor(String color) {
        mDataMap.putString(KEY_HAND_COLOR, color);
    }

    @Nullable
    public String maybeGetHandColor() {
        return mDataMap.getString(KEY_HAND_COLOR);
    }

    public void setBackgroundImage(Asset image) {
        mDataMap.putAsset(KEY_BACKGROUND_IMG, image);
    }

    @Nullable
    public Asset maybeGetBackgroundImage() {
        return mDataMap.getAsset(KEY_BACKGROUND_IMG);
    }

    public void setTextConfigModel(TextConfigModel textConfigModel) {
        mDataMap.putDataMap(TextConfigModel.KEY_TEXT_CONFIG_MODEL, textConfigModel.getDataMap());
    }

    /**
     * Get the TextConfigModel, return null if not present.
     */
    @Nullable
    public TextConfigModel maybeGetTextConfigModel() {
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

    public void onModelUpdate(ConfigModel configModel) {
        if (configModel.maybeGetTickColor() != null)  {
            setTickColor(configModel.maybeGetTickColor());
        }
        if (configModel.maybeGetHandColor() != null) {
            setHandColor(configModel.maybeGetHandColor());
        }
        if (configModel.maybeGetBackgroundImage() != null) {
            setBackgroundImage(configModel.maybeGetBackgroundImage());
        }
        if (configModel.maybeGetTextConfigModel() != null) {
            setTextConfigModel(configModel.maybeGetTextConfigModel());
        }
        if (configModel.maybeGetWeatherModel() != null) {
            setWeatherModel(configModel.maybeGetWeatherModel());
        }
    }
}
