package com.huangsz.android.screentip.face;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;

import java.lang.ref.WeakReference;
import java.util.TimeZone;

public class CharacterWatchFaceService extends CanvasWatchFaceService {

    private static final String TAG = "CharWatchFaceService";

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        return new WatchFaceEngine();
    }

    /**
     * Update time handler.
     */
    private static class UpdateTimeHandler extends Handler {

        private static final int MSG_UPDATE_TIME = 0;

        private static final int INTERACTIVE_UPDATE_RATE_MS = 1000;

        private final WeakReference<WatchFaceEngine> engineReference;

        private UpdateTimeHandler(WatchFaceEngine engine) {
            this.engineReference = new WeakReference<>(engine);
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_UPDATE_TIME:
                    WatchFaceEngine engine = this.engineReference.get();
                    if (engine == null) {
                        this.removeMessages(MSG_UPDATE_TIME);
                        return;
                    }
                    engine.invalidate();
                    if (engine.shouldTimerBeRunning()) {
                        long timeMs = System.currentTimeMillis();
                        long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                        this.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                    }
                    break;
            }
        }
    }

    /**
     * Implement service callback methods.
     */
    private class WatchFaceEngine extends CanvasWatchFaceService.Engine {

        private CharacterWatchFaceRenderer mWatchFaceRenderer;

        private CharacterWatchFaceConnector mWatchFaceConnector;

        private boolean mTimeZoneReceiverRegistered = false;

        /**
         * handler to update the time once a second in interactive mode
         */
        final Handler mUpdateTimeHandler = new UpdateTimeHandler(this);

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

            mWatchFaceRenderer = new CharacterWatchFaceRenderer(CharacterWatchFaceService.this,
                    new CharacterWatchFaceRenderer.UpdateWatchFaceCallback() {
                        @Override
                        public void updateWatchFace() {
                            invalidate();
                        }
                    });
            mWatchFaceConnector = new CharacterWatchFaceConnector(
                    CharacterWatchFaceService.this, mWatchFaceRenderer);
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
                mWatchFaceConnector.maybeConnectGoogleApi();
            } else {
                maybeUnRegisterTimeZoneReceiver();
                mWatchFaceConnector.maybeDisconnectGoogleApi();
            }
            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // Override the onSurfaceChanged() method to scale your background to fit the device
            // any time the view changes.
            Log.d(TAG, "onSurfaceChanged");
            mWatchFaceRenderer.onSurfaceChanged(width, height);
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onDestroy() {
            mWatchFaceConnector.maybeDisconnectGoogleApi();
            super.onDestroy();
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

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(UpdateTimeHandler.MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(UpdateTimeHandler.MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }


}
