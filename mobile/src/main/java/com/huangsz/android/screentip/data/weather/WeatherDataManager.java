package com.huangsz.android.screentip.data.weather;

import android.support.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Manages the weather data fetched from the server.
 */
public class WeatherDataManager {

    @Nullable
    private static WeatherDataManager instance = null;

    @Nullable
    private WeatherData weatherData = null;

    private WeatherDataManager() {}

    public synchronized static WeatherDataManager getInstance() {
        if (instance == null) {
            instance = new WeatherDataManager();
        }
        return instance;
    }

    public ListenableFuture<WeatherData> fetchWeather(
            double latitude, double longitude, String metric) {
        final SettableFuture<WeatherData> future = SettableFuture.create();
        FetchWeatherTask task = new FetchWeatherTask(new FetchWeatherTask.PostExecuteCallback() {
            @Override
            public void onWeatherDataFetched(WeatherData weatherData) {
                WeatherDataManager.this.weatherData = weatherData;
                future.set(weatherData);
            }
        });
        task.execute(String.valueOf(latitude), String.valueOf(longitude), metric);
        return future;
    }
}
