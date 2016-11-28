package com.huangsz.android.screentip.data.weather;

import android.app.IntentService;
import android.content.Intent;

/**
 * IntentService to get weather data in background and update the watch face.
 */
public class WeatherIntentService extends IntentService {
    // Check https://guides.codepath.com/android/Starting-Background-Services#using-with-alarmmanager-for-periodic-tasks.
    // Also check the Efficient Android Threading for IntentService.

    public WeatherIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
