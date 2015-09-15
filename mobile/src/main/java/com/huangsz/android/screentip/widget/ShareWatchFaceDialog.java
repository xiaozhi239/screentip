package com.huangsz.android.screentip.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.huangsz.android.screentip.R;

/**
 * A dialog to confirm sharing watch face snapshot.
 */
public class ShareWatchFaceDialog extends BaseConfigDialog {

    private ImageView mPreview;

    private Bitmap mSnapshot;

    public static ShareWatchFaceDialog newInstance(
            String title, Handler handler, int handlerFlag, Bitmap snapshot) {
        ShareWatchFaceDialog dialog = new ShareWatchFaceDialog();
        dialog.mSnapshot = snapshot;
        setupDialog(dialog, title, handler, handlerFlag);
        return dialog;
    }

    @Override
    public void customizeView(AlertDialog.Builder builder) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_share_face, null);
        builder.setView(view);
        builder.setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMessage(mSnapshot);
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just close.
            }
        });
        setElements(view);
    }

    private void setElements(View view) {
        mPreview = (ImageView) view.findViewById(R.id.dialog_share_face_preview);
        mPreview.setImageBitmap(mSnapshot);
    }
}
