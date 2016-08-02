package com.huangsz.android.screentip.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.common.utils.ViewUtils;
import com.huangsz.android.screentip.connect.model.TextConfigModel;
import com.huangsz.android.screentip.connect.model.WeatherModel;
import com.huangsz.android.screentip.data.location.LocationTracker;
import com.huangsz.android.screentip.data.weather.WeatherData;
import com.huangsz.android.screentip.data.weather.WeatherDataManager;

import java.util.Comparator;
import java.util.concurrent.ExecutionException;

/**
 * Config dialog for showing weather on watch face.
 */
public class WeatherConfigDialog extends BaseConfigDialog {

    private static final String TAG = "WeatherConfigDialog";

    private CheckBox mWeatherCheckBox;

    private LinearLayout mConfigLayout;

    private Spinner mUnitSpinner;

    private SeekBar mCoordinateX;

    private SeekBar mCoordinateY;

    private Spinner mColorSpinner;

    private Spinner mFontSpinner;

    private WeatherDataManager mWeatherDataManager;

    private LocationTracker mLocationTracker;

    private WeatherModel mWeatherModel;

    @Nullable
    private ListenableFuture<WeatherData> mWeatherDataFuture;

    public static WeatherConfigDialog newInstance(
            String title, Handler handler, int handlerFlag) {
        WeatherConfigDialog dialog = new WeatherConfigDialog();
        setupDialog(dialog, title, handler, handlerFlag);
        return dialog;
    }

    public WeatherConfigDialog() {
        mWeatherDataManager = WeatherDataManager.getInstance();
        mLocationTracker = LocationTracker.getInstance(getActivity());
        mWeatherModel = new WeatherModel();
    }

    @Override
    public void customizeView(AlertDialog.Builder builder) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_weather_config, null);
        builder.setView(view);
        onInitialize(view);
        builder.setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmConfig();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just close.
            }
        });
//        maybeFetchWeatherData();
    }

    private void onInitialize(View view) {
        mConfigLayout = (LinearLayout) view.findViewById(R.id.dialog_weather_configuration);
        mWeatherCheckBox = (CheckBox) view.findViewById(R.id.dialog_weather_checkbox);
        mWeatherCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                ViewUtils.updateEnableControls(mConfigLayout, checked /* enabled or not */);
            }
        });
        mUnitSpinner = (Spinner) view.findViewById(R.id.dialog_weather_unit_spinner);
        mUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                maybeFetchWeatherData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mCoordinateX = (SeekBar) view.findViewById(R.id.dialog_weather_coordinate_x);
        mCoordinateY = (SeekBar) view.findViewById(R.id.dialog_weather_coordinate_y);
        mColorSpinner = (Spinner) view.findViewById(R.id.dialog_weather_color_spinner);
        mFontSpinner = (Spinner) view.findViewById(R.id.dialog_weather_font_spinner);
        maybeRestoreStatus();
    }

    private void confirmConfig() {
        if (mWeatherDataFuture == null) {
            Toast.makeText(getActivity(),
                    "Not able to fetch weather data, check the network setting.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        WeatherData weatherData;
        try {
            weatherData = mWeatherDataFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, e.toString());
            return;
        }
        mWeatherModel.setShowWeather(mWeatherCheckBox.isChecked());
        if (mWeatherCheckBox.isChecked()) {
            WeatherModel.Unit unit =
                    WeatherModel.Unit.values()[mUnitSpinner.getSelectedItemPosition()];
            mWeatherModel.setTemperatureUnit(unit);
            mWeatherModel.setCurrentTemperature(weatherData.getCurrentTemperature());
            TextConfigModel textModel = new TextConfigModel();
            mWeatherModel.setTextConfigModel(textModel);
            textModel.setColor(mColorSpinner.getSelectedItem().toString());
            textModel.setTextSize(getFontDpFromSpinnerSelection(
                    mFontSpinner.getSelectedItem().toString()));
            textModel.setContent(
                    String.format("%.1f %s",
                            mWeatherModel.getCurrentTemperature(),
                            mWeatherModel.getTemperatureUnit().getSymbol()));
            textModel.setCoordinateX(mCoordinateX.getProgress());
            textModel.setCoordinateY(mCoordinateY.getProgress());

        }
        sendMessage(mWeatherModel);
    }

    private void maybeRestoreStatus() {
        if (mWeatherModel.isEmpty()) {
            return;
        }
        ViewUtils.updateEnableControls(
                mConfigLayout, mWeatherModel.isShowWeather() /* enabled or not */);
        ViewUtils.setSpinnerSelection(mUnitSpinner, mWeatherModel.getTemperatureUnit());

        TextConfigModel textModel = mWeatherModel.getTextConfigModel();
        mCoordinateX.setProgress((int)textModel.getCoordinateX());
        mCoordinateY.setProgress((int)textModel.getCoordinateY());
        ViewUtils.setSpinnerSelection(mColorSpinner, textModel.getColor());
        ViewUtils.setSpinnerSelection(mFontSpinner, textModel.getTextSize(),
                new Comparator<Object>() {
                    @Override
                    public int compare(Object o, Object t1) {
                        return getFontDpFromSpinnerSelection((String) o) - (int) t1;
                    }
                });
    }

    private int getFontDpFromSpinnerSelection(String spinnerSelection) {
        String fontInDp = spinnerSelection.trim();  // Example: '12dp'
        return Integer.parseInt(fontInDp.substring(0, fontInDp.length() - 2));
    }

    private void maybeFetchWeatherData() {
        Location location = mLocationTracker.getLocation();
        if (location != null) {
            WeatherModel.Unit unit =
                    WeatherModel.Unit.values()[mUnitSpinner.getSelectedItemPosition()];
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            mWeatherDataFuture =
                    mWeatherDataManager.fetchWeather(latitude, longitude, unit.getValue());
        }
    }
}
