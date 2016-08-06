package com.huangsz.android.screentip.connect.model;

import com.google.android.gms.wearable.DataMap;

/**
 * Model for weather data (temperature, wind, humidity, etc.) and weather configurations.
 */
public class WeatherModel extends Model {

    private static final String KEY_SHOW_WEATHER = "KEY_SHOULD_SHOW_WEATHER";

    private static final String KEY_CURRENT_TEMPERATURE = "KEY_CURRENT_TEMPERATURE";

    private static final String KEY_TEMPERATURE_UNIT = "KEY_TEMPERATURE_UNIT";

    private static final String KEY_TEXT_CONFIG_MODEL = "KEY_TEXT_CONFIG_MODEL";

    public enum Unit {
        Celsius("metric", "°C"), Fahrenheit("imperial", "°F");

        private String value;

        private String symbol;

        Unit(String value, String symbol) {
            this.value = value;
            this.symbol = symbol;
        }

        public String getValue() {
            return value;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    public WeatherModel() {
        super();
    }

    public WeatherModel(DataMap dataMap) {
        super(dataMap);
    }

    public void setShowWeather(boolean showWeather) {
        mDataMap.putBoolean(KEY_SHOW_WEATHER, showWeather);
    }

    public boolean isShowWeather() {
        return mDataMap.getBoolean(KEY_SHOW_WEATHER, false);
    }

    public void setCurrentTemperature(double degree) {
        mDataMap.putDouble(KEY_CURRENT_TEMPERATURE, degree);
    }

    public double getCurrentTemperature() {
        return mDataMap.getDouble(KEY_CURRENT_TEMPERATURE);
    }

    public void setTemperatureUnit(Unit unit) {
        mDataMap.putInt(KEY_TEMPERATURE_UNIT, unit.ordinal());
    }

    public Unit getTemperatureUnit() {
        return Unit.values()[mDataMap.getInt(KEY_TEMPERATURE_UNIT)];
    }

    /** Set the configuration of weather related texts. */
    public void setTextConfigModel(TextConfigModel textConfigModel) {
        mDataMap.putDataMap(KEY_TEXT_CONFIG_MODEL, textConfigModel.getDataMap());
    }

    public TextConfigModel getTextConfigModel() {
        return new TextConfigModel(mDataMap.getDataMap(KEY_TEXT_CONFIG_MODEL));
    }
}
