package com.huangsz.android.screentip.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CharacterWatchFaceService extends CanvasWatchFaceService {

    private static final String TAG = "CharWatchFaceService";

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        return new Engine();
    }

    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

        // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
        private static final String DATA_LAYER_WATCHFACE_CONFIG_PATH = "/watch_face_config";

        // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
        private static final String KEY_CHARACTER_COLOR = "KEY_CHARACTER_COLOR";

        // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
        private static final String KEY_TICK_COLOR = "KEY_TICK_COLOR";

        // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
        private static final String KEY_CHARACTER_TEXT = "KEY_CHARACTER_TEXT";

        // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
        private static final String KEY_BACKGROUND_IMG = "KEY_BACKGROUND_IMG";

        private static final long TIMEOUT_MS = 5000;

        private static final int MSG_UPDATE_TIME = 0;

        private static final int INTERACTIVE_UPDATE_RATE_MS = 1000;

        private CharacterWatchFaceRenderer mWatchFaceRenderer;

        private boolean mTimeZoneReceiverRegistered = false;

        private GoogleApiClient mGoogleApiClient;

        /**
         * handler to update the time once a second in interactive mode
         */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler
                                    .sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        /**
         * receiver to update the time zone
         */
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mWatchFaceRenderer.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        final DataApi.DataListener mOnDataListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                for (DataEvent event : dataEvents) {
                    if (event.getType() == DataEvent.TYPE_CHANGED) {
                        DataItem item = event.getDataItem();
                        processConfigurationFor(item);
                    }
                }
                dataEvents.release();
                invalidate();
            }
        };

        private final ResultCallback<DataItemBuffer> mOnConnectedResultCallback =
                new ResultCallback<DataItemBuffer>() {
                    // This is only notified when the service is firstly connected.
                    @Override
                    public void onResult(DataItemBuffer dataItems) {
                        for (DataItem item : dataItems) {
                            processConfigurationFor(item);
                        }
                        dataItems.release();
                        invalidate();
                    }
                };

        private void processConfigurationFor(DataItem item) {
            if (DATA_LAYER_WATCHFACE_CONFIG_PATH.equals(item.getUri().getPath())) {
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                if (dataMap.containsKey(KEY_CHARACTER_COLOR)) {
                    String color = dataMap.getString(KEY_CHARACTER_COLOR);
                    mWatchFaceRenderer.setCharacterColor(Color.parseColor(color));
                }
                if (dataMap.containsKey(KEY_TICK_COLOR)) {
                    String color = dataMap.getString(KEY_TICK_COLOR);
                    mWatchFaceRenderer.setTickColor(Color.parseColor(color));
                }
                if (dataMap.containsKey(KEY_CHARACTER_TEXT)) {
                    String text = dataMap.getString(KEY_CHARACTER_TEXT);
                    mWatchFaceRenderer.setCharacterTip(text);
                }
                if (dataMap.containsKey(KEY_BACKGROUND_IMG)) {
                    Asset asset = dataMap.getAsset(KEY_BACKGROUND_IMG);
                    new LoadBitMapAsyncTask(mGoogleApiClient).execute(asset);
                }
            }
        }




        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            // configure the system UI
            // configures peeking cards to be a single line tall, the background of a peeking card
            // to show only briefly and only for interruptive notifications, and the system time
            // not to be shown (since this watch face draws its own time representation).
            setWatchFaceStyle(new WatchFaceStyle.Builder(CharacterWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle
                            .BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            mWatchFaceRenderer = new CharacterWatchFaceRenderer(CharacterWatchFaceService.this);

            mGoogleApiClient = new GoogleApiClient.Builder(CharacterWatchFaceService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            // For devices that use low-bit ambient mode, the screen supports fewer bits for each
            // color in ambient mode, so you should disable anti-aliasing and bitmap filtering
            // when the device switches to ambient mode.
            // For devices that require burn-in protection, avoid using large blocks of white
            // pixels in ambient mode and do not place content within 10 pixels of the edge of
            // the screen, since the system shifts the content periodically to avoid pixel burn-in.
            mWatchFaceRenderer.setProperties(
                    properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false),
                    properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false));
        }

        @Override
        public void onTimeTick() {
            // In ambient mode, the system calls this method every one minute.
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mWatchFaceRenderer.setAmbientMode(inAmbientMode);
            invalidate();
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mWatchFaceRenderer.draw(canvas, bounds);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                maybeRegisterTimeZoneReceiver();
                maybeConnectGoogleApi();
            } else {
                maybeUnRegisterTimeZoneReceiver();
                maybeDisconnectGoogleApi();
            }
            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // Override the onSurfaceChanged() method to scale your background to fit the device
            // any time the view changes.
            mWatchFaceRenderer.onSurfaceChanged(width, height);
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onDestroy() {
            maybeDisconnectGoogleApi();
            super.onDestroy();
        }

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(TAG, "GoogleApiClient connected.");
            Wearable.DataApi.addListener(mGoogleApiClient, mOnDataListener);
            Wearable.DataApi.getDataItems(mGoogleApiClient).setResultCallback(
                    mOnConnectedResultCallback);
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.i(TAG, "GoogleApiClient suspended with cause: " + cause);
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.i(TAG, "GoogleApiClient connection failed.");
        }

        private void maybeRegisterTimeZoneReceiver() {
            if (mTimeZoneReceiverRegistered) {
                return;
            }
            mTimeZoneReceiverRegistered = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            CharacterWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void maybeUnRegisterTimeZoneReceiver() {
            if (!mTimeZoneReceiverRegistered) {
                return;
            }
            mTimeZoneReceiverRegistered = false;
            CharacterWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void maybeConnectGoogleApi() {
            if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }

        private void maybeDisconnectGoogleApi() {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Log.i(TAG, "GoogleApiClient disconnect");
                Wearable.DataApi.removeListener(mGoogleApiClient, mOnDataListener);
                mGoogleApiClient.disconnect();
            }
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }


        // TODO(huangsz) Move this to utils, add a callback extending Function in the postexecute.
        private final class LoadBitMapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {

            private GoogleApiClient googleApiClient;

            public LoadBitMapAsyncTask(GoogleApiClient googleApiClient) {
                this.googleApiClient = googleApiClient;
            }

            @Override
            protected Bitmap doInBackground(Asset... params) {
                Asset asset = params[0];
                if (asset == null) {
                    throw new IllegalArgumentException("Asset must be non-null");
                }
                if (googleApiClient == null || !googleApiClient.isConnected()) {
                    return null;
                }

                ConnectionResult result =
                        googleApiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (!result.isSuccess()) {
                    return null;
                }
                // convert asset into a file descriptor and block until it's ready
                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        googleApiClient, asset).await().getInputStream();

                if (assetInputStream == null) {
                    Log.w(TAG, "Requested an unknown Asset.");
                    return null;
                }
                // decode the stream into a bitmap
                return BitmapFactory.decodeStream(assetInputStream);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                mWatchFaceRenderer.setBackgroundImage(bitmap);
            }
        }
    }
}
