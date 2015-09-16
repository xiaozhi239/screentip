package com.huangsz.android.screentip.common.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.huangsz.android.screentip.common.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class HintUtils {

    public static void showNoPairedWatchToast(Context context) {
        Toast.makeText(context, context.getResources().getString(R.string.hint_no_paired_watch),
                Toast.LENGTH_LONG).show();
    }

    public static ProgressDialog showProgressDialog(Context context, String title, String content,
                                                    boolean cancelable) {
        final ProgressDialog dialog = ProgressDialog.show(
                context, title, content, true, cancelable);
        return dialog;
    }

    public static void dismissDialog(Dialog dialog) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
