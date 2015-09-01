package android.huangsz.com.screentip.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * return null if imageUri not exists.
     */
    public static Bitmap createScaledBitmap(Uri imageUri, int width, int height, Context context) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),
                    imageUri);
            return bitmap.createScaledBitmap(bitmap, width, height, true);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
}
