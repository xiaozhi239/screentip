package com.huangsz.android.screentip.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;

import com.huangsz.android.screentip.R;

/**
 * Config text on watch face. Such as user customized text, date or weather.
 */
public class TextConfigDialog extends BaseConfigDialog {

    public static TextConfigDialog newInstance(String title, Handler handler, int handlerFlag) {
        TextConfigDialog dialog = new TextConfigDialog();
        setupDialog(dialog, title, handler, handlerFlag);
        return dialog;
    }

    @Override
    public void customizeView(AlertDialog.Builder builder) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_text_config, null);
        builder.setView(view);
        builder.setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

}
