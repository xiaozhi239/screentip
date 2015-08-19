package com.huangsz.android.screentip.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class for sharing preferences.
 */
public class PreferenceHelper {

    private static final String TUTORIAL_PRESENTED = "TUTORIAL_PRESENTED";

    public static boolean isTutorialPresented(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(TUTORIAL_PRESENTED, false);
    }

    public static void setTutorialPresented(Context context, boolean isPresented) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(TUTORIAL_PRESENTED, isPresented).apply();
    }

}
