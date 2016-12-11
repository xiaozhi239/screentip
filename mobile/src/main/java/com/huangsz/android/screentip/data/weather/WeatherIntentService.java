package com.huangsz.android.screentip.data.weather;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.huangsz.android.screentip.config.WatchFaceConfigConnector;
import com.huangsz.android.screentip.connect.model.TextConfigModel;
import com.huangsz.android.screentip.connect.model.WeatherModel;
import com.huangsz.android.screentip.data.location.LocationTracker;
import com.huangsz.android.screentip.nodes.NodeMonitor;

/**
 * IntentService to get weather data in background and update the watch face.
 */
public class WeatherIntentService extends IntentService {

    private final static String TAG = "WeatherIntentService";

    private LocationTracker locationTracker;
    private WeatherDataManager weatherDataManager;
    private WatchFaceConfigConnector watchFaceConfigConnector;

    public WeatherIntentService() {
        super("WeatherIntentService");
        locationTracker = LocationTracker.getInstance(getApplicationContext());
        weatherDataManager = WeatherDataManager.getInstance();
        watchFaceConfigConnector = new WatchFaceConfigConnector(
                getApplicationContext(), NodeMonitor.getInstance(), null);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "start updating weather to watch in background");
        Location location = locationTracker.getLocation();
        if (location != null) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            ListenableFuture<WeatherData> weatherDataFuture =
                    weatherDataManager.fetchWeather(latitude, longitude);
            // IntentService runs in a worker thread by default.
            Futures.addCallback(weatherDataFuture, new FutureCallback<WeatherData>() {
                @Override
                public void onSuccess(WeatherData weatherData) {
                    watchFaceConfigConnector.setWeatherModel(createWeatherModel(weatherData));
                    watchFaceConfigConnector.sendConfigChangeToWatch();
                    Log.i(TAG, String.format(
                            "Weather information sent to watch: %d%s",
                            weatherData.getCurrentTemperature(),
                            weatherDataManager.getWeatherUnit().getSymbol()));
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });
        }
    }

    private WeatherModel createWeatherModel(WeatherData weatherData) {
        // TODO: fetch TextModel from shared preference. Actually, the state of watch face should
        // be in shared preference.
        return weatherDataManager.createWeatherModel(true, weatherData, new TextConfigModel());
    }
}
