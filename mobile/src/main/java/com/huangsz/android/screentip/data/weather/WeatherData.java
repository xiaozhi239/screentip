package com.huangsz.android.screentip.data.weather;

/**
 * Weather data contains information such as current temperature, high/low temperature, etc.
 */
public class WeatherData {

    private double currentTemperature;

    public void setCurrentTemperature(double temperature) {
        currentTemperature = temperature;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }
}
