package com.huangsz.android.screentip.data.weather;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.huangsz.android.screentip.config.WatchFaceConfigConnector;
import com.huangsz.android.screentip.connect.model.WeatherModel;
import com.huangsz.android.screentip.data.location.LocationTracker;
import com.huangsz.android.screentip.data.persist.ConfigModelPersistenceManager;
import com.huangsz.android.screentip.nodes.NodeMonitor;

/**
 * IntentService to get weather data in background and update the watch face.
 */
public class WeatherIntentService extends IntentService {

    private final static String TAG = "WeatherIntentService";

    private LocationTracker locationTracker;
    private WeatherDataManager weatherDataManager;
    private WatchFaceConfigConnector watchFaceConfigConnector;
    private ConfigModelPersistenceManager persistenceManager;

    public WeatherIntentService() {
        super("WeatherIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        watchFaceConfigConnector = new WatchFaceConfigConnector(
                getApplicationContext(), NodeMonitor.getInstance(), null);
        watchFaceConfigConnector.maybeConnect();
        locationTracker = LocationTracker.getInstance(getApplicationContext());
        weatherDataManager = WeatherDataManager.getInstance();
        persistenceManager = new ConfigModelPersistenceManager(getApplicationContext());
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
                    WeatherModel weatherModel = createWeatherModel(weatherData);
                    if (!weatherModel.isShowWeather()
                            || weatherModel.getTextConfigModel() == null) {
                        WeatherAlarmUtils.cancelPeriodicWeatherUpdate(getApplicationContext());
                        return;
                    }
                    watchFaceConfigConnector.setWeatherModel(weatherModel);
                    watchFaceConfigConnector.sendConfigChangeToWatch();
                    watchFaceConfigConnector.maybeDisconnect();
                    Log.i(TAG, String.format(
                            "Weather information sent to watch: %f%s",
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
        WeatherModel persistedWeatherModel = persistenceManager.retrieveWeatherModel();
        return weatherDataManager.createWeatherModel(
                persistedWeatherModel.isShowWeather(),
                weatherData,
                persistedWeatherModel.getTextConfigModel());
    }
}
