package com.huangsz.android.screentip.common.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Get bitmap from asset.
 */
public class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {

    private static final String TAG = "LoadBitmapAsyncTask";

    private static final long TIMEOUT_BLOCKING_ASSET_MS = 3000;

    private GoogleApiClient mGoogleApiClient;

    private PostExecuteCallback mPostExecuteCallback;

    public LoadBitmapAsyncTask(GoogleApiClient googleApiClient, PostExecuteCallback callback) {
        mGoogleApiClient = googleApiClient;
        mPostExecuteCallback = callback;
    }

    @Override
    protected Bitmap doInBackground(Asset... params) {
        Asset asset = params[0];
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return null;
        }

        ConnectionResult result =
                mGoogleApiClient.blockingConnect(
                        TIMEOUT_BLOCKING_ASSET_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        mPostExecuteCallback.onBitmapLoaded(bitmap);
    }

    public interface PostExecuteCallback {
        void onBitmapLoaded(Bitmap bitmap);
    }
}
