package com.huangsz.android.screentip.data.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * A utility class to set or cancel periodic weather fetching.
 */
public class WeatherAlarmUtils {

    public static void setUpPeriodicWeatherUpdate(Context context) {
        Intent intent = new Intent(context, WeatherIntentReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, WeatherIntentReceiver.REQUEST_CODE, intent,
                /* In case of overlapping, replace with current one instead of piling up. */
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR,
                pendingIntent);

    }

    public static void cancelPeriodicWeatherUpdate(Context context) {
        Intent intent = new Intent(context, WeatherIntentReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, WeatherIntentReceiver.REQUEST_CODE, intent,
                /* In case of overlapping, replace with current one instead of piling up. */
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
