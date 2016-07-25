package com.huangsz.android.screentip.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;

import com.huangsz.android.screentip.R;

/**
 * Config dialog for showing weather on watch face.
 */
public class WeatherConfigDialog extends BaseConfigDialog {

    public static WeatherConfigDialog newInstance(String title, Handler handler, int handlerFlag) {
        WeatherConfigDialog dialog = new WeatherConfigDialog();
        setupDialog(dialog, title, handler, handlerFlag);
        return dialog;
    }

    @Override
    public void customizeView(AlertDialog.Builder builder) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_weather_config, null);
        builder.setView(view);
        builder.setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmConfig();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just close. TODO(huangsz) preserve current state.
            }
        });
    }

    private void confirmConfig() {
        // gets the weather from WeatherFetcher
        // WeatherFetcher stores the weather and only update once per hour.
        // If the checkbox is clicked off, then we cancel the schedule weather update.
    }
}
