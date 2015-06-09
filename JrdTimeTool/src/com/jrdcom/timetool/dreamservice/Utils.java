/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrdcom.timetool.dreamservice;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.android.deskclock.R;


public class Utils {
    private final static String TAG = Utils.class.getName();

    /**
     * Intent to be used for checking if a clock's date has changed. Must be every fifteen
     * minutes because not all time zones are hour-locked.
     **/
    public static final String ACTION_ON_QUARTER_HOUR = "com.android.deskclock.ON_QUARTER_HOUR";

    /** Types that may be used for clock displays. **/
    public static final String CLOCK_TYPE_DIGITAL = "digital";
    public static final String CLOCK_TYPE_ANALOG = "analog";

    /**
     * time format constants
     */
    public final static String HOURS_24 = "kk";
    public final static String HOURS = "h";
    public final static String MINUTES = ":mm";



    /**
     * For screensavers to set whether the digital or analog clock should be displayed.
     * Returns the view to be displayed.
     */
    public static View setClockStyle(Context context, View digitalClock, View analogClock,
            String clockStyleKey) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultClockStyle = context.getResources().getString(R.string.default_clock_style);
        String style = sharedPref.getString(clockStyleKey, defaultClockStyle);
        View returnView;
        if (style.equals(CLOCK_TYPE_ANALOG)) {
            digitalClock.setVisibility(View.GONE);
            analogClock.setVisibility(View.VISIBLE);
            returnView = analogClock;
        } else {
            digitalClock.setVisibility(View.VISIBLE);
            analogClock.setVisibility(View.GONE);
            returnView = digitalClock;
        }

        return returnView;
    }
    /**
     * For screensavers to dim the lights if necessary.
     */
    public static void dimClockView(boolean dim, View clockView) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setColorFilter(new PorterDuffColorFilter(
                        (dim ? 0x60FFFFFF : 0xC0FFFFFF),
                PorterDuff.Mode.MULTIPLY));
        clockView.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }
    
    /** Runnable for use with screensaver and dream, to move the clock every minute.
     *  registerViews() must be called prior to posting.
     */
    public static class ScreensaverMoveSaverRunnable implements Runnable {
        static final long MOVE_DELAY = 60000; // DeskClock.SCREEN_SAVER_MOVE_DELAY;
        static final long SLIDE_TIME = 10000;
        static final long FADE_TIME = 3000;

        static final boolean SLIDE = false;

        private View mContentView, mSaverView;
        private final Handler mHandler;

        private static TimeInterpolator mSlowStartWithBrakes;


        public ScreensaverMoveSaverRunnable(Handler handler) {
            mHandler = handler;
            mSlowStartWithBrakes = new TimeInterpolator() {
                @Override
                public float getInterpolation(float x) {
                    return (float)(Math.cos((Math.pow(x,3) + 1) * Math.PI) / 2.0f) + 0.5f;
                }
            };
        }

        public void registerViews(View contentView, View saverView) {
            mContentView = contentView;
            mSaverView = saverView;
        }

        @Override
        public void run() {
            long delay = MOVE_DELAY;
            if (mContentView == null || mSaverView == null) {
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, delay);
                return;
            }

            final float xrange = mContentView.getWidth() - mSaverView.getWidth();
            final float yrange = mContentView.getHeight() - mSaverView.getHeight();
            Log.v("xrange: "+xrange+" yrange: "+yrange);

            if (xrange == 0 && yrange == 0) {
                delay = 500; // back in a split second
            } else {
                final int nextx = (int) (Math.random() * xrange);
                final int nexty = (int) (Math.random() * yrange);

                if (mSaverView.getAlpha() == 0f) {
                    // jump right there
                    mSaverView.setX(nextx);
                    mSaverView.setY(nexty);
                    ObjectAnimator.ofFloat(mSaverView, "alpha", 0f, 1f)
                        .setDuration(FADE_TIME)
                        .start();
                } else {
                    AnimatorSet s = new AnimatorSet();
                    Animator xMove   = ObjectAnimator.ofFloat(mSaverView,
                                         "x", mSaverView.getX(), nextx);
                    Animator yMove   = ObjectAnimator.ofFloat(mSaverView,
                                         "y", mSaverView.getY(), nexty);

                    Animator xShrink = ObjectAnimator.ofFloat(mSaverView, "scaleX", 1f, 0.85f);
                    Animator xGrow   = ObjectAnimator.ofFloat(mSaverView, "scaleX", 0.85f, 1f);

                    Animator yShrink = ObjectAnimator.ofFloat(mSaverView, "scaleY", 1f, 0.85f);
                    Animator yGrow   = ObjectAnimator.ofFloat(mSaverView, "scaleY", 0.85f, 1f);
                    AnimatorSet shrink = new AnimatorSet(); shrink.play(xShrink).with(yShrink);
                    AnimatorSet grow = new AnimatorSet(); grow.play(xGrow).with(yGrow);

                    Animator fadeout = ObjectAnimator.ofFloat(mSaverView, "alpha", 1f, 0f);
                    Animator fadein = ObjectAnimator.ofFloat(mSaverView, "alpha", 0f, 1f);


                    if (SLIDE) {
                        s.play(xMove).with(yMove);
                        s.setDuration(SLIDE_TIME);

                        s.play(shrink.setDuration(SLIDE_TIME/2));
                        s.play(grow.setDuration(SLIDE_TIME/2)).after(shrink);
                        s.setInterpolator(mSlowStartWithBrakes);
                    } else {
                        AccelerateInterpolator accel = new AccelerateInterpolator();
                        DecelerateInterpolator decel = new DecelerateInterpolator();

                        shrink.setDuration(FADE_TIME).setInterpolator(accel);
                        fadeout.setDuration(FADE_TIME).setInterpolator(accel);
                        grow.setDuration(FADE_TIME).setInterpolator(decel);
                        fadein.setDuration(FADE_TIME).setInterpolator(decel);
                        s.play(shrink);
                        s.play(fadeout);
                        s.play(xMove.setDuration(0)).after(FADE_TIME);
                        s.play(yMove.setDuration(0)).after(FADE_TIME);
                        s.play(fadein).after(FADE_TIME);
                        s.play(grow).after(FADE_TIME);
                    }
                    s.start();
                }

                long now = System.currentTimeMillis();
                long adjust = (now % 60000);
                delay = delay
                        + (MOVE_DELAY - adjust) // minute aligned
                        - (SLIDE ? 0 : FADE_TIME) // start moving before the fade
                        ;
            }

            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, delay);
        }
    }
}
