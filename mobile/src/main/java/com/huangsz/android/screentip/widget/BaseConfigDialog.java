package com.huangsz.android.screentip.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Make sure sub class calls setupDialog when instantiate.
 */
public abstract class BaseConfigDialog extends DialogFragment {

    private static final String KEY_DIALOG_TITLE = "KEY_DIALOG_TITLE";

    private static final String KEY_HANDLER_FLAG = "KEY_HANDLER_FLAG";

    private Handler mUpdateHandler;

    private int mHandlerFlag;

    public void setHandler(Handler handler) {
        mUpdateHandler = handler;
    }

    protected static void setupDialog(BaseConfigDialog dialog, String title, Handler handler,
                                      int handlerFlag) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DIALOG_TITLE, title);
        bundle.putInt(KEY_HANDLER_FLAG, handlerFlag);
        dialog.setArguments(bundle);
        dialog.setHandler(handler);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String title = getArguments().getString(KEY_DIALOG_TITLE);
        mHandlerFlag = getArguments().getInt(KEY_HANDLER_FLAG);
        builder.setTitle(title);
        customizeView(builder);
        return builder.create();
    }

    protected void sendMessage(Object messageContent) {
        Message message = new Message();
        message.what = mHandlerFlag;
        message.obj = messageContent;
        mUpdateHandler.sendMessage(message);
    }

    public abstract void customizeView(AlertDialog.Builder builder);
}
