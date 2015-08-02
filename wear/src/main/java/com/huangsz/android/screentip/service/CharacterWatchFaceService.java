package com.huangsz.android.screentip.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.huangsz.android.screentip.R;

import java.util.Calendar;
import java.util.TimeZone;

public class CharacterWatchFaceService extends CanvasWatchFaceService {

    private static final String TAG = "CharWatchFaceService";

    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SERIF, Typeface.NORMAL);

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        return new Engine();
    }

    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

        // Constant to help calculate clock hand rotations
        private static final float TWO_PI = (float) Math.PI * 2f;

        // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
        private static final String DATA_LAYER_WATCHFACE_CONFIG_PATH = "/watch_face_config";

        // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
        private static final String TAG_CHARACTER_COLOR = "TAG_CHARACTER_COLOR";

        // Keep the same with {@link WatchFaceConfigActivity} in handheld app.
        private static final String TAG_TICK_COLOR = "TAG_TICK_COLOR";

        private static final int MSG_UPDATE_TIME = 0;

        private static final int INTERACTIVE_UPDATE_RATE_MS = 1000;

        private boolean mTimeZoneReceiverRegistered = false;

        private boolean mLowBitAmbient = false;
        private boolean mBurnInProtection = false;

        private Calendar mCalendar;

        // graphic objects
        private Bitmap mBackgroundBitmap;
        private Bitmap mBackgroundScaledBitmap;
        private Paint mMinTickPaint;
        private Paint mSecTickPaint;
        private Paint mHourPaint;
        private Paint mMinutePaint;
        private Paint mSecondPaint;
        private Paint mCharacterPaint;  // Paint to show a character as a reminder tip.

        private GoogleApiClient mGoogleApiClient;

        // TODO(huangsz) Receive this from handheld device.
        private static final String TIP_TEXT = "坚";

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
                mCalendar.setTimeZone(TimeZone.getDefault());
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
                if (dataMap.containsKey(TAG_CHARACTER_COLOR)) {
                    String color = dataMap.getString(TAG_CHARACTER_COLOR);
                    mCharacterPaint.setColor(Color.parseColor(color));
                }
                if (dataMap.containsKey(TAG_TICK_COLOR)) {
                    String color = dataMap.getString(TAG_TICK_COLOR);
                    mMinTickPaint.setColor(Color.parseColor(color));
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

            // load the background image
            Resources resources = CharacterWatchFaceService.this.getResources();
            Drawable backgroundDrawable = resources.getDrawable(R.drawable.background, null);
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

            // create graphic styles
            mMinTickPaint = createLinePaint(resources.getColor(R.color.minute_tick_color),
                    2.f, Paint.Cap.ROUND);
            mSecTickPaint = createLinePaint(resources.getColor(R.color.second_tick_color),
                    1.f, Paint.Cap.BUTT);
            mHourPaint = createLinePaint(
                    resources.getColor(R.color.hour_bar_color), 5.f, Paint.Cap.ROUND);
            mMinutePaint = createLinePaint(
                    resources.getColor(R.color.minute_bar_color), 4.f, Paint.Cap.ROUND);
            mSecondPaint = createLinePaint(
                    resources.getColor(R.color.second_bar_color), 2.f, Paint.Cap.BUTT);

            mCharacterPaint = createTextPaint(resources, false);

            // allocate a Calendar to calculate local time using the UTC time and time zone
            mCalendar = Calendar.getInstance();

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
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION,
                    false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            // In ambient mode, most watch face implementations simply invalidate the canvas to
            // redraw the watch face in the Engine.onTimeTick() method.
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mHourPaint.setAntiAlias(antiAlias);
                mMinutePaint.setAntiAlias(antiAlias);
                // mSecondPaint is not presented in ambient mode anyway.
                mMinTickPaint.setAntiAlias(antiAlias);
                mCharacterPaint.setAntiAlias(antiAlias);
            }
            // TODO(huangsz) restore to black and white.
            invalidate();
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Update the time
            mCalendar.setTimeInMillis(System.currentTimeMillis());

            int width = bounds.width();
            int height = bounds.height();

            canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);

            // Find the center. Ignore the window insets so that, on round watches
            // with a "chin", the watch face is centered on the entire screen, not
            // just the usable portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            // Draw the ticks.
            drawTicks(canvas, centerX, centerY, 10, 12, mMinTickPaint);
            drawTicks(canvas, centerX, centerY, 3, 60, mSecTickPaint);

            // Compute rotations and lengths for the clock hands.
            float seconds = mCalendar.get(Calendar.SECOND) +
                    mCalendar.get(Calendar.MILLISECOND) / 1000f;
            float secRot = seconds / 60f * TWO_PI;
            float minutes = mCalendar.get(Calendar.MINUTE) + seconds / 60f;
            float minRot = minutes / 60f * TWO_PI;
            float hours = mCalendar.get(Calendar.HOUR) + minutes / 60f;
            float hrRot = hours / 12f * TWO_PI;

            float secLength = centerX - 20;
            float minLength = centerX - 40;
            float hrLength = centerX - 60;

            // Only draw the second hand in interactive mode.
            if (!isInAmbientMode()) {
                float secX = (float) Math.sin(secRot) * secLength;
                float secY = (float) -Math.cos(secRot) * secLength;
                canvas.drawLine(centerX, centerY, centerX + secX, centerY +
                        secY, mSecondPaint);
            }

            // Draw the minute and hour hands.
            float minX = (float) Math.sin(minRot) * minLength;
            float minY = (float) -Math.cos(minRot) * minLength;
            canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY,
                    mMinutePaint);
            float hrX = (float) Math.sin(hrRot) * hrLength;
            float hrY = (float) -Math.cos(hrRot) * hrLength;
            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY,
                    mHourPaint);

            // Draw the character.
            float charX = centerX;
            float charY = centerY * 1.6f;
            canvas.drawText(TIP_TEXT, charX, charY, mCharacterPaint);
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
            if (mBackgroundScaledBitmap == null
                    || mBackgroundScaledBitmap.getWidth() != width
                    || mBackgroundScaledBitmap.getHeight() != height) {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                        width, height, true /* filter */);
            }
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
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        private void drawTicks(Canvas canvas, float centerX, float centerY,
                               int length, int divisions, Paint paint) {
            float outerTickRadius = centerX;
            float innerTickRadius = centerX - length;
            for (int tickIndex = 0; tickIndex < divisions; tickIndex++) {
                float tickRot = tickIndex * TWO_PI / divisions;
                float innerX = (float) Math.sin(tickRot) * innerTickRadius;
                float innerY = (float) -Math.cos(tickRot) * innerTickRadius;
                float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                float outerY = (float) -Math.cos(tickRot) * outerTickRadius;
                canvas.drawLine(centerX + innerX, centerY + innerY,
                        centerX + outerX, centerY + outerY, paint);
            }
        }

        private Paint createLinePaint(int color, float strokeWidth, Paint.Cap strokeCap) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStrokeWidth(strokeWidth);
            paint.setStrokeCap(strokeCap);
            paint.setAntiAlias(true);
            return paint;
        }

        private Paint createTextPaint(Resources resources, boolean isBold) {
            Paint paint = new Paint();
            paint.setColor(resources.getColor(R.color.black));
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(isBold ? BOLD_TYPEFACE : NORMAL_TYPEFACE);
            paint.setTextSize(resources.getDimension(R.dimen.tip_text_size));
            paint.setColor(resources.getColor(R.color.tip_text_color));
            return paint;
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
    }
}
