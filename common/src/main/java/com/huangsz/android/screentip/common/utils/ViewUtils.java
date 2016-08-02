package com.huangsz.android.screentip.common.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import java.util.Comparator;

public class ViewUtils {

    /**
     * Enable or disable a {@link ViewGroup} recursively.
     */
    public static void updateEnableControls(ViewGroup viewGroup, boolean enable){
        for (int i = 0; i < viewGroup.getChildCount(); i++){
            View child = viewGroup.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup){
                updateEnableControls((ViewGroup)child, enable);
            }
        }
    }

    /**
     * Set spinner selection to a value using the default "equals" of object to find correct
     * selection.
     */
    public static void setSpinnerSelection(Spinner spinner, Object value) {
        setSpinnerSelection(spinner, value, new Comparator<Object>() {
            @Override
            public int compare(Object o, Object a) {
                return o.equals(a) ? 0 : -1;
            }
        });
    }

    /**
     * Set spinner selection to a value using specified comparator to find correct selection.
     */
    public static void setSpinnerSelection(
            Spinner spinner, Object value, Comparator<Object> comparator) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (comparator.compare(spinner.getItemAtPosition(i), value) == 0) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
