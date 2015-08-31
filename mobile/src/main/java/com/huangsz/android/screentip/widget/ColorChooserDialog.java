package com.huangsz.android.screentip.widget;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.config.WatchFaceConfigActivity;

public class ColorChooserDialog extends DialogFragment {

    private static final String KEY_DIALOG_TITLE = "KEY_DIALOG_TITLE";

    private static final String KEY_HANDLER_FLAG = "KEY_HANDLER_FLAG";

    private Handler mUpdateHandler;

    public static ColorChooserDialog newInstance(String title, Handler handler, int handlerFlag) {
        ColorChooserDialog dialog = new ColorChooserDialog();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DIALOG_TITLE, title);
        bundle.putInt(KEY_HANDLER_FLAG, handlerFlag);
        dialog.setArguments(bundle);
        dialog.setHandler(handler);
        return dialog;
    }

    public void setHandler(Handler handler) {
        mUpdateHandler = handler;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String title = getArguments().getString(KEY_DIALOG_TITLE);
        final int handlerFlag = getArguments().getInt(KEY_HANDLER_FLAG);
        builder.setTitle(title)
                .setItems(R.array.colors_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] colors = getResources().getStringArray(R.array.colors_array);
                        Message message = new Message();
                        message.what = handlerFlag;
                        message.obj = colors[which];
                        mUpdateHandler.sendMessage(message);
                    }
                });
        return builder.create();
    }

}
