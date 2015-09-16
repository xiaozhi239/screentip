package com.huangsz.android.screentip.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.huangsz.android.screentip.R;

/**
 * A dialog to confirm sharing watch face snapshot.
 */
public class ShareWatchFaceDialog extends BaseConfigDialog {

    private static final int BORDER_WIDTH = 90;

    private ImageView mPreview;

    private Bitmap mRawSnapshot;

    private Bitmap mShareSnapshot;

    public static ShareWatchFaceDialog newInstance(
            String title, Handler handler, int handlerFlag, Bitmap snapshot) {
        ShareWatchFaceDialog dialog = new ShareWatchFaceDialog();
        dialog.mRawSnapshot = snapshot;
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
                sendMessage(mShareSnapshot);
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
        mShareSnapshot = createShareBitmap(mRawSnapshot);
        mPreview.setImageBitmap(mShareSnapshot);
    }

    private Bitmap createShareBitmap(Bitmap src) {
        Bitmap watch = BitmapFactory.decodeResource(getResources(), R.drawable.android_watch);
        int width = watch.getWidth();
        int height = watch.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int faceWidth = width - BORDER_WIDTH * 2;
        int faceHeight = (int)((float)faceWidth / (float)src.getWidth() * src.getHeight());
        src = src.createScaledBitmap(src, faceWidth, faceHeight, true);

        Bitmap bitmap = Bitmap.createBitmap(width, height, src.getConfig());
        Canvas canvas = new Canvas(bitmap);

        canvas.drawBitmap(watch, 0, 0, null);

        int startX = centerX - faceWidth / 2;
        int startY = centerY - faceHeight / 2;
        int endX = startX + faceWidth;
        int endY = startY + faceHeight;
        RectF snapshotRect = new RectF(startX, startY, endX, endY);
        canvas.drawBitmap(src, null, snapshotRect, null);
        return bitmap;
    }
}
