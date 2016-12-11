package com.huangsz.android.screentip.data.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * A broadcast receiver to receive periodic weather update intent.
 */
public class WeatherIntentReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, WeatherIntentService.class));
    }
}
