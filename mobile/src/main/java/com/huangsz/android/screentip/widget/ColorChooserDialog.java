package com.huangsz.android.screentip.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;

import com.huangsz.android.screentip.R;

public class ColorChooserDialog extends BaseConfigDialog {

    public static ColorChooserDialog newInstance(String title, Handler handler, int handlerFlag) {
        ColorChooserDialog dialog = new ColorChooserDialog();
        setupDialog(dialog, title, handler, handlerFlag);
        return dialog;
    }

    @Override
    public void customizeView(AlertDialog.Builder builder) {
        builder.setItems(R.array.colors_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String[] colors = getResources().getStringArray(R.array.colors_array);
                sendMessage(colors[which]);
            }
        });
    }
}
