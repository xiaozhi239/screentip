package com.huangsz.android.screentip.data.weather;

/**
 * Weather data contains information such as current temperature, high/low temperature, etc.
 */
public class WeatherData {

    private float currentTemperature;

    public void setCurrentTemperature(float temperature) {
        currentTemperature = temperature;
    }

    public float getCurrentTemperature() {
        return currentTemperature;
    }
}
