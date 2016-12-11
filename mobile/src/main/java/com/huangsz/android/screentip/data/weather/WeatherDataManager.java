package com.huangsz.android.screentip.data.weather;

import android.support.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.huangsz.android.screentip.connect.model.TextConfigModel;
import com.huangsz.android.screentip.connect.model.WeatherModel;

/**
 * Manages the weather data fetched from the server.
 */
public class WeatherDataManager {

    @Nullable
    private static WeatherDataManager instance = null;

    private WeatherModel.Unit weatherUnit = WeatherModel.Unit.Celsius;

    private WeatherDataManager() {}

    public synchronized static WeatherDataManager getInstance() {
        if (instance == null) {
            instance = new WeatherDataManager();
        }
        return instance;
    }

    public ListenableFuture<WeatherData> fetchWeather(double latitude, double longitude) {
        final SettableFuture<WeatherData> future = SettableFuture.create();
        FetchWeatherTask task = new FetchWeatherTask(new FetchWeatherTask.PostExecuteCallback() {
            @Override
            public void onWeatherDataFetched(WeatherData weatherData) {
                future.set(weatherData);
            }
        });
        task.execute(String.valueOf(latitude), String.valueOf(longitude), weatherUnit.getValue());
        return future;
    }

    public void setWeatherUnit(WeatherModel.Unit weatherUnit) {
        this.weatherUnit = weatherUnit;
    }

    public WeatherModel.Unit getWeatherUnit() {
        return weatherUnit;
    }

    public WeatherModel createWeatherModel(
            boolean showWeatherInWatch, @Nullable WeatherData weatherData,
            @Nullable TextConfigModel textConfigModel) {
        if (showWeatherInWatch && (weatherData == null || textConfigModel == null)) {
            throw new IllegalStateException("WeatherData or Text not set for showing weather!");
        }
        WeatherModel weatherModel = new WeatherModel();
        weatherModel.setShowWeather(showWeatherInWatch);
        if (showWeatherInWatch) {
            weatherModel.setTemperatureUnit(weatherUnit);
            weatherModel.setCurrentTemperature(weatherData.getCurrentTemperature());
            weatherModel.setTextConfigModel(textConfigModel);
        }
        return weatherModel;
    }
}
