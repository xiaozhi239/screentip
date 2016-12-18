package com.huangsz.android.screentip.data.persist;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Manager of setting related persistence.
 */
public class SettingPersistenceManager {

    private static final String TUTORIAL_PRESENTED = "TUTORIAL_PRESENTED";

    SharedPreferences sharedPreferences;

    public SettingPersistenceManager(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isTutorialPresented() {
        return sharedPreferences.getBoolean(TUTORIAL_PRESENTED, false);
    }

    public void setTutorialPresented(boolean isPresented) {
        sharedPreferences.edit().putBoolean(TUTORIAL_PRESENTED, isPresented).apply();
    }

}
