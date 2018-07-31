package net.heatherandkevin.moto360face;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;
import android.widget.Toast;

import net.heatherandkevin.moto360face.FaceAccents.BatteryFace;
import net.heatherandkevin.moto360face.FaceAccents.DayFace;
import net.heatherandkevin.moto360face.FaceAccents.DayOfMonth;
import net.heatherandkevin.moto360face.FaceAccents.SecondsFace;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't
 * shown. On devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient
 * mode. The watch face is drawn with less contrast in mute mode.
 * <p>
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
public class Moto360Face extends CanvasWatchFaceService {

    /*
     * Updates rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<Moto360Face.Engine> mWeakReference;

        public EngineHandler(Moto360Face.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            Moto360Face.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private static final float HOUR_STROKE_WIDTH = 5f;
        private static final float MINUTE_STROKE_WIDTH = 3f;
        private static final float SECOND_TICK_STROKE_WIDTH = 0.5f;
        private static final float TICK_STROKE_WIDTH = 2f;

        private static final float CENTER_GAP_AND_CIRCLE_RADIUS = 6.5f;

        private static final int SHADOW_RADIUS = 6;
        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver = false;

        /* Handler to update the battery level changes */
        private float batteryLevel;
        private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                batteryLevel = level / (float)scale;
            }
        };
        private boolean mRegisteredBatteryReceiver = false;
        private boolean mMuteMode;
        private float mCenterX;
        private float mCenterY;
        private float mSecondHandLength;
        private float sMinuteHandLength;
        private float sHourHandLength;
        /* Colors for all hands (hour, minute, seconds, ticks) based on photo loaded. */
        private int mBackgroundColor;
        private int mWatchHandColor;
        private int mTickStrokeColor;
        private int mWatchHandHighlightColor;
        private int mWatchHandShadowColor;

        // NEW HOTNESS
        private Paint mBackgroundPaint;
        private Paint mRomanNumeralPaint;
        private Paint mTickStrokePaint;
        private Paint mTickStrokeLowPaint;
        private Paint mTickStrokeHighPaint;

        private Paint mHourPaint;
        private Paint mMinutePaint;
        private Paint mSecondPaint;
        private Paint mTickAndCirclePaint;
        private Paint mTickFillPaint;
        private Bitmap mBackgroundBitmap;
        private Bitmap mGrayBackgroundBitmap;
        private Bitmap mHourHandBitmap;
        private Bitmap mMinuteHandBitmap;
        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        private SecondsFace secondsFace;
        private DayFace dayFace;
        private DayOfMonth dayOfMonth;
        private BatteryFace batteryFace;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(Moto360Face.this)
                    .setAcceptsTapEvents(true)
                    .build());

            mCalendar = Calendar.getInstance();

            initializeBackground();
            initializeWatchFace();
        }

        private void initializeBackground() {
            mBackgroundColor = Color.BLUE;
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mBackgroundColor);
            mBackgroundPaint.setAntiAlias(false);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
//            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        }

        private void initializeWatchFace() {
            mHourHandBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hourhand);
            mMinuteHandBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.minute_hand);
            /* Set defaults for colors */

            mWatchHandColor = Color.DKGRAY;
            mTickStrokeColor = Color.WHITE;
            mWatchHandHighlightColor = Color.WHITE;
            mWatchHandShadowColor = Color.WHITE;

            mRomanNumeralPaint= new Paint();
            mRomanNumeralPaint.setColor(Color.BLACK);
            mRomanNumeralPaint.setAlpha(155);
//            mRomanNumeralPaint.setStrokeWidth(MINUTE_STROKE_WIDTH);
            mRomanNumeralPaint.setAntiAlias(true);
//            mRomanNumeralPaint.setStrokeCap(Paint.Cap.ROUND);
            mRomanNumeralPaint.setTextSize(45f);
            Typeface mTypeface = Typeface.createFromAsset(getBaseContext().getAssets(), "watchfont.otf");
            mRomanNumeralPaint.setTypeface(mTypeface);
            mRomanNumeralPaint.setLetterSpacing(-0.05f);
//            mRomanNumeralPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, Color.BLACK);

            mHourPaint = new Paint();
            mHourPaint.setColor(mWatchHandColor);
            mHourPaint.setStrokeWidth(HOUR_STROKE_WIDTH);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);
            mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mMinutePaint = new Paint();
            mMinutePaint.setColor(mWatchHandColor);
            mMinutePaint.setStrokeWidth(MINUTE_STROKE_WIDTH);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);
            mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mSecondPaint = new Paint();
            mSecondPaint.setColor(mWatchHandHighlightColor);
            mSecondPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setStrokeCap(Paint.Cap.ROUND);

            mTickAndCirclePaint = new Paint();
            mTickAndCirclePaint.setColor(mWatchHandColor);
            mTickAndCirclePaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mTickAndCirclePaint.setAntiAlias(true);
            mTickAndCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

            mTickStrokePaint = new Paint();
            mTickStrokePaint.setColor(Color.DKGRAY);
            mTickStrokePaint.setAntiAlias(true);

            mTickFillPaint = new Paint(mTickStrokePaint);
            mTickFillPaint.setColor(Color.BLACK);
            mTickFillPaint.setAntiAlias(true);
            mTickFillPaint.setAlpha(125);
            mTickFillPaint.setStyle(Paint.Style.FILL);
            mTickStrokePaint.setColor(mTickStrokeColor);

            mTickStrokePaint.setStyle(Paint.Style.STROKE);
            mTickStrokePaint.setStrokeWidth(TICK_STROKE_WIDTH);
            mTickStrokePaint.setAntiAlias(true);

            mTickStrokeLowPaint = new Paint(mTickStrokePaint);
            mTickStrokeLowPaint.setColor(Color.DKGRAY);
            mTickStrokeLowPaint.setAlpha(200);
            mTickStrokeHighPaint = new Paint(mTickStrokePaint);
            mTickStrokeHighPaint.setColor(Color.LTGRAY);
            mTickStrokeHighPaint.setAlpha(200);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;

            updateWatchHandStyle();

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void updateWatchHandStyle() {
            mHourPaint.setColor(mWatchHandColor);
            mMinutePaint.setColor(mWatchHandColor);

            mHourPaint.setAntiAlias(true);
            mMinutePaint.setAntiAlias(true);
            mSecondPaint.setAntiAlias(true);

            mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
            mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
            mSecondPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mCenterX = width / 2f;
            mCenterY = height / 2f;

            secondsFace = new SecondsFace(mCenterX, mCenterY + mCenterY / 2f - 10f, 55);
            dayFace = new DayFace(mCenterX, mCenterY + mCenterY / 2f - 10f, 55,
                    45, 20);
            dayOfMonth = new DayOfMonth(mCenterX, mCenterY + mCenterY / 2f - 10f, 55,
                    45, 20);
            batteryFace = new BatteryFace(mCenterX ,mCenterY - mCenterY / 2.25f,35f);

            /*
             * Calculate lengths of different hands based on watch screen size.
             */
            mSecondHandLength = (float) (mCenterX * 0.875);
            sMinuteHandLength = (float) (mCenterX * 0.75);
            sHourHandLength = (float) (mCenterX * 0.5);


            /* Scale loaded background image (more efficient) if surface dimensions change. */
            float scale = ((float) width) / (float) mBackgroundBitmap.getWidth();

            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                    (int) (mBackgroundBitmap.getWidth() * scale),
                    (int) (mBackgroundBitmap.getHeight() * scale), true);

        }

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            drawBackground(canvas);
            drawWatchFace(canvas);
        }

        private void drawBackground(Canvas canvas) {

//            canvas.drawColor(mBackgroundColor);
            canvas.drawBitmap(mBackgroundBitmap, 0, 0, mBackgroundPaint);
//            canvas.drawCircle(mCenterX, mCenterY, 160, mBackgroundPaint);
        }

        private void drawWatchFace(Canvas canvas) {

            /*
             * Draw ticks. Usually you will want to bake this directly into the photo, but in
             * cases where you want to allow users to select their own photos, this dynamically
             * creates them on top of the photo.
             */
            float tickLength = 35f;
            float innerTickRadius;
            float outerTickRadius;
            Paint topTickPaint;
            Paint bottomTickPaint;
            for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
                float tickRot = (float) (tickIndex * 30);
                innerTickRadius = mCenterX - 10;

//                if (tickIndex % 5 == 0) {
//                    mTickStrokePaint.setStrokeWidth(TICK_STROKE_WIDTH * 2);
//                    displayText = true;
//                } else {
//                    mTickStrokePaint.setStrokeWidth(TICK_STROKE_WIDTH);
//                    displayText = false;
//                }

                if (tickIndex == 0 ) {
                    if (mAmbient) {
                        innerTickRadius -= 20;
                    } else {
                        continue;
                    }
                } else if (tickIndex == 1 || tickIndex == 11){
                    innerTickRadius = (innerTickRadius - 25) / (float)Math.cos(Math.toRadians(tickIndex * 30));
                }

                outerTickRadius = innerTickRadius - tickLength;

                if (tickIndex == 3 || tickIndex == 9) {
                    outerTickRadius += 5;
                }

                if (tickIndex >= 10 || tickIndex <= 2) {
                    topTickPaint = mTickStrokeHighPaint;
                    bottomTickPaint = mTickStrokeLowPaint;
                } else {
                    topTickPaint = mTickStrokeLowPaint;
                    bottomTickPaint = mTickStrokeHighPaint;
                }
                canvas.save();

                canvas.rotate(tickRot, mCenterX, mCenterY);



                canvas.drawLine(mCenterX - 4f, mCenterY + innerTickRadius - 1,
                        mCenterX + 4f, mCenterY + innerTickRadius,
                        topTickPaint);
                canvas.drawLine(mCenterX + 4f, mCenterY + innerTickRadius,
                        mCenterX + 4f,mCenterY + outerTickRadius + 1,
                        topTickPaint);

                canvas.drawLine(mCenterX + 4f, mCenterY + outerTickRadius + 1,
                        mCenterX - 4f,mCenterY + outerTickRadius,
                        bottomTickPaint);
                canvas.drawLine(mCenterX - 4f,mCenterY + outerTickRadius,
                        mCenterX - 4f,mCenterY + innerTickRadius - 1,
                        bottomTickPaint);

                canvas.drawRect(mCenterX - 5f, mCenterY + outerTickRadius,
                        mCenterX + 5f, mCenterY + innerTickRadius, mTickFillPaint);


                canvas.restore();
            }

//            Path test = new Path();
//            test.arcTo(mCenterX - mCenterX / 2f + 20 -45, mCenterY - 35,
//                    mCenterX - mCenterX / 2f + 20 +45, mCenterY + 55,
//                    98,265, false);
//            test.arcTo(mCenterX - 60f, mCenterY + mCenterY / 2f - 10 - 60,
//                    mCenterX + 60f, mCenterY + mCenterY / 2f - 10 + 60,
//                    257,-55, false);
//            test.close();
//
//            canvas.drawPath(test, mTickStrokeHighPaint);
//            canvas.drawPath(test, mTickFillPaint);

//            float xCenter = mCenterX + mCenterX / 3 + 5;
//            float yCenter = mCenterY;
//            float radius = 45f;
//
//            float cutoutR = 55;
//            float cutoutX = mCenterX;
//            float cutoutY = mCenterY + mCenterY / 2f - 10;
//            float buffer = 10;




//            Paint temp = new Paint(mTickFillPaint);
//            temp.setColor(Color.RED);
//            temp.setAlpha(125);

//            canvas.drawRect(xCenter - radius, yCenter - radius,
//                    xCenter + radius, yCenter + radius,
//                    temp);
//
//            canvas.drawRect(cutoutX - cutoutR - buffer, cutoutY - cutoutR - buffer,
//                    cutoutX + cutoutR + buffer, cutoutY + cutoutR + buffer,
//                    temp);

//            float adjacent = cutoutR + buffer;
//            float oppisite = xCenter - radius - cutoutX;
//            float angle = (float) Math.toDegrees(Math.atan((double) oppisite / adjacent)) + 270;

//            canvas.drawText(String.valueOf(angle), mCenterX, mCenterY, mHourPaint);


//            canvas.drawCircle(xCenter, yCenter, radius, mTickStrokeLowPaint);

//
//            Path test2 = new Path();
//            test2.arcTo(cutoutX - cutoutR - buffer, cutoutY - cutoutR - buffer,
//                    cutoutX + cutoutR + buffer, cutoutY + cutoutR + buffer,
//                    angle,56.5f, false);
//            test2.arcTo(xCenter - radius, yCenter - radius,
//                    xCenter + radius, yCenter + radius,
//                    87.5f,-271.5f, false);
//            test2.close();
//
//            canvas.drawPath(test2, mTickStrokeHighPaint);
//            canvas.drawPath(test2, mTickFillPaint);
//
//            canvas.drawLine(mCenterX,mCenterY,
//                    mCenterX+12.5f, mCenterY,
//                    mHourPaint);
//


//            canvas.drawRect(mCenterX - 100f, mCenterY - 55f,
//                    mCenterX + 100, mCenterY + 55f,
//                    mTickFillPaint);

//            canvas.drawCircle(mCenterX + mCenterX / 3 ,mCenterY - mCenterY / 4,45f, mTickFillPaint);
//            canvas.drawCircle(mCenterX - mCenterX / 3 ,mCenterY - mCenterY / 4,45f, mTickFillPaint);

            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            final float seconds = mCalendar.get(Calendar.SECOND);
            final float secondsRotation = seconds * 6f;

            if (!mAmbient) {
                secondsFace.draw(canvas, seconds);
                dayFace.setCalendar(mCalendar);
                dayFace.draw(canvas);
                dayOfMonth.setCalendar(mCalendar);
                dayOfMonth.draw(canvas);
                batteryFace.draw(canvas, batteryLevel);
//                canvas.drawText(mCalendar.getTime().toString(), 30, mCenterY, mRomanNumeralPaint);
            }

            final float minutesRotation = mCalendar.get(Calendar.MINUTE) * 6f + secondsRotation / 60f;

//            final float hourHandOffset = mCalendar.get(Calendar.MINUTE) / 2f;
            final float hoursRotation = (mCalendar.get(Calendar.HOUR) * 30) + minutesRotation / 12f;

            /*
             * Save the canvas state before we can begin to rotate it.
             */
            canvas.save();

            final float hourHandLeftOffset = mCenterX - mHourHandBitmap.getWidth() / 2f + 0.5f;
            final float hourHandTopOffset = mCenterY - mHourHandBitmap.getHeight() * 0.5f;
            final float minuteHandLeftOffset = mCenterX - mMinuteHandBitmap.getWidth() / 2f + 0.5f;
            final float minuteHandTopOffset = mCenterY - mMinuteHandBitmap.getHeight() * 0.5f;
            canvas.rotate(hoursRotation, mCenterX, mCenterY);
//            canvas.drawBitmap(mHourHandBitmap, hourHandLeftOffset,
//                    hourHandTopOffset, mHourPaint);

//            canvas.drawRect(mCenterX - 3.5f, mCenterY - 75,
//                    mCenterX + 3.5f, mCenterY, mHourPaint);

            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
//            canvas.drawLine(
//                    mCenterX,
//                    mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
//                    mCenterX,
//                    mCenterY - sMinuteHandLength,
//                    mMinutePaint);
//            canvas.drawBitmap(mMinuteHandBitmap, minuteHandLeftOffset,
//                    minuteHandTopOffset, mMinutePaint);

            /*
             * Ensure the "seconds" hand is drawn only when we are in interactive mode.
             * Otherwise, we only update the watch face once a minute.
             */
            canvas.restore();


            canvas.drawCircle(
                    mCenterX,
                    mCenterY,
                    CENTER_GAP_AND_CIRCLE_RADIUS,
                    mTickAndCirclePaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void registerReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                mRegisteredTimeZoneReceiver = true;
                IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                Moto360Face.this.registerReceiver(mTimeZoneReceiver, filter);
            }

            if (!mRegisteredBatteryReceiver) {
                mRegisteredBatteryReceiver = true;
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Moto360Face.this.registerReceiver(mBatteryReceiver,filter);
            }
        }

        private void unregisterReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                mRegisteredTimeZoneReceiver = false;
                Moto360Face.this.unregisterReceiver(mTimeZoneReceiver);
            }

            if (mRegisteredBatteryReceiver) {
                mRegisteredBatteryReceiver = false;
                Moto360Face.this.unregisterReceiver(mBatteryReceiver);
            }
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
