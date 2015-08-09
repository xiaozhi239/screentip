package com.huangsz.android.screentip.service;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.huangsz.android.screentip.R;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Responsible for the drawing of character watch face.
 */
class CharacterWatchFaceRenderer {

    // Text paint typefaces.
    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SERIF, Typeface.NORMAL);

    // Constant to help calculate clock hand rotations
    private static final float TWO_PI = (float) Math.PI * 2f;

    private Context mContext;

    private UpdateWatchFaceCallback mUpdateWatchFaceCallback;

    private Calendar mCalendar;

    private boolean mIsInAmbientMode;

    // properties
    private boolean mLowBitAmbient = false;
    private boolean mBurnInProtection = false;

    // graphic objects
    private Bitmap mBackgroundBitmap;
    private Bitmap mBackgroundScaledBitmap;
    private Paint mMinTickPaint;
    private Paint mSecTickPaint;
    private Paint mHourPaint;
    private Paint mMinutePaint;
    private Paint mSecondPaint;
    private Paint mCharacterPaint;  // Paint to show a character as a reminder tip.

    private String mTipText = "乐";

    CharacterWatchFaceRenderer(Context context, UpdateWatchFaceCallback updateWatchFaceCallback) {
        mContext = context;
        mUpdateWatchFaceCallback = updateWatchFaceCallback;

        // load the background image
        Resources resources = mContext.getResources();
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
    }

    public void setTimeZone(TimeZone timeZone) {
        mCalendar.setTimeZone(timeZone);
    }

    public void setCharacterColor(int color) {
        mCharacterPaint.setColor(color);
    }

    public void setTickColor(int color) {
        mMinTickPaint.setColor(color);
        mSecTickPaint.setColor(color);
    }

    public void setCharacterTip(String text) {
        mTipText = text;
    }

    public void setBackgroundImage(Bitmap image) {
        int width = mBackgroundScaledBitmap.getWidth();
        int height = mBackgroundScaledBitmap.getHeight();
        mBackgroundBitmap = image;
        mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                width, height, true /* filter */);
    }

    public void updateWatchFace() {
        mUpdateWatchFaceCallback.updateWatchFace();
    }

    public void setProperties(boolean lowBitAmbient, boolean burnInProtection) {
        mLowBitAmbient = lowBitAmbient;
        mBurnInProtection = burnInProtection;
    }

    public void setAmbientMode(boolean isInAmbientMode) {
        mIsInAmbientMode = isInAmbientMode;
        if (mLowBitAmbient) {
            boolean antiAlias = !isInAmbientMode;
            mHourPaint.setAntiAlias(antiAlias);
            mMinutePaint.setAntiAlias(antiAlias);
            // mSecondPaint is not presented in ambient mode anyway.
            mMinTickPaint.setAntiAlias(antiAlias);
            mCharacterPaint.setAntiAlias(antiAlias);
        }
        // TODO(huangsz) restore to black and white.
    }

    public void onSurfaceChanged(int width, int height) {
        if (mBackgroundScaledBitmap == null
                || mBackgroundScaledBitmap.getWidth() != width
                || mBackgroundScaledBitmap.getHeight() != height) {
            mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                    width, height, true /* filter */);
        }
    }

    public void draw(Canvas canvas, Rect bounds) {
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
        if (!mIsInAmbientMode) {
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
        canvas.drawText(mTipText, charX, charY, mCharacterPaint);
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

    interface UpdateWatchFaceCallback {
        public void updateWatchFace();
    }
}
