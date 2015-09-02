package com.huangsz.android.screentip.common.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * A util class handling unit transfers.
 */
public class UnitTransferUtils {

    /**
     * Convert dp size to pixel size.
     */
    public static float getPixelFromDp(int sizeInDp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp,
                context.getResources().getDisplayMetrics());
    }
}
