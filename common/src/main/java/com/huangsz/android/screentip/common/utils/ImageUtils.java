package com.huangsz.android.screentip.common.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.wearable.Asset;

import java.io.ByteArrayOutputStream;
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

    /**
     * Create asset from bitmap.
     */
    public static Asset compressAndCreateAssetFromBitmap(Bitmap image) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    /**
     * Get the image uri from the bitmap.
     */
    public static Uri getImageUriFromBitmap(Bitmap bitmap, ContentResolver contentResolver) {
        String path = MediaStore.Images.Media.insertImage(contentResolver,
                bitmap, "Image Description", null);
        return Uri.parse(path);
    }

}
