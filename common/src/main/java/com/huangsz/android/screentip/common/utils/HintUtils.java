package com.huangsz.android.screentip.common.utils;

import android.content.Context;
import android.widget.Toast;

import com.huangsz.android.screentip.common.R;

public class HintUtils {

    public static void showNoPairedWatchToast(Context context) {
        Toast.makeText(context, context.getResources().getString(R.string.hint_no_paired_watch),
                Toast.LENGTH_LONG).show();
    }
}
