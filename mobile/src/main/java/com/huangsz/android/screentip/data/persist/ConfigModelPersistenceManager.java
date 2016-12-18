package com.huangsz.android.screentip.data.persist;

import android.content.Context;
import android.content.SharedPreferences;

import com.huangsz.android.screentip.connect.model.ConfigModel;
import com.huangsz.android.screentip.connect.model.WeatherModel;

/**
 * Manages the persistence of ConfigModel.
 */
public class ConfigModelPersistenceManager {

    private static final String PREF_KEY_CONNECTOR =
            "com.huangsz.android.screentip.data.persist.PREF_FILE_CONFIG_PERSIST";

    private static final String KEY_PREFIX = "screentip";

    private SharedPreferences sharedPreferences;

    public ConfigModelPersistenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_KEY_CONNECTOR, Context.MODE_PRIVATE);
    }

    public void persistConfigModel(ConfigModel configModel) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        configModel.persistData(editor, KEY_PREFIX);
        editor.commit();
    }

    public ConfigModel retrieveConfigModel() {
        ConfigModel configModel = new ConfigModel();
        configModel.retrieveDataFromPersistence(sharedPreferences, KEY_PREFIX);
        return configModel;
    }

    public WeatherModel retrieveWeatherModel() {
        ConfigModel configModelPlaceHolder = new ConfigModel();
        WeatherModel weatherModel = new WeatherModel();
        configModelPlaceHolder.setWeatherModel(weatherModel);
        weatherModel.retrieveDataFromPersistence(
                sharedPreferences, configModelPlaceHolder.getFullPersistenceKeyPrefix(KEY_PREFIX));
        return weatherModel;
    }
}
