/*
 *MyAnalogClock.
 *Author: Xiaosheng Zheng
 *Date:2009.06.30
 *Copyright (C) 2009 ArcherMind Technology, Inc.
 */

package com.jrdcom.timetool.worldclock.view;

import java.util.Calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.TimeToolActivity;

public class MyAnalogClock extends View {

    private final class UpdateSecondRunnable implements Runnable {
        @Override
        public void run() {
            Message message = Message.obtain();
            message.what = 1;
            mUpdateHandler.sendMessage(message);
        }
    }

    private Drawable mMinuteHand = null;

    private Drawable mSecondHand = null;

    private Drawable mMinuteHandShadow = null;

    private Drawable mSecondHandShadow = null;

    private int mCurrentSecond;


    private Handler mUpdateHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            mCurrentSecond++;
            Log.i("Clock", "update second->" + mCurrentSecond);
          
            invalidate();
            updateSecond();
        };

    };

    private Drawable mRealSecond;

    private Drawable mDial;

    private int mDialWidth;

    private int mDialHeight;

    private float mHours;

    private float mMinutes;
    private float mSeconds;

    Paint paint;

    private UpdateSecondRunnable mUpdateSecondRunnable;

    public MyAnalogClock(Context context) {
        super(context);
    }

    public MyAnalogClock(Context context, AttributeSet attrs) {
        super(context, attrs);

        // TODO Auto-generated constructor stub

        TypedArray a = context.getResources().obtainAttributes(attrs, R.styleable.pathbar);
        mDial = a.getDrawable(R.styleable.pathbar_dial);
        mMinuteHand = a.getDrawable(R.styleable.pathbar_minutehand);
        mSecondHand = a.getDrawable(R.styleable.pathbar_secondhand);

        mMinuteHandShadow = a.getDrawable(R.styleable.pathbar_minutehandShadow);
        mSecondHandShadow = a.getDrawable(R.styleable.pathbar_secondhandShadow);
        mRealSecond = a.getDrawable(R.styleable.pathbar_real_second);
        a.recycle();

        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();

        paint = new Paint();//
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.GRAY);
        paint.setShadowLayer(5f, 0f, 0f, Color.TRANSPARENT);//
        
        mUpdateSecondRunnable = new UpdateSecondRunnable();

    }

    public void updateTime(float Minutes, float Second) {
        mHours = Minutes / 5;
        mMinutes = Second;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int availableWidth = getRight() - getLeft();
        int availableHeight = getBottom() - getTop();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();

            int offsetX = 0;
            int offsetY = 0;
            boolean scaled = false;

            if (availableWidth < w || availableHeight < h) {
                scaled = true;
                float scale = Math.min((float) availableWidth / (float) w, (float) availableHeight
                        / (float) h);
                canvas.save();
                canvas.scale(scale, scale, x, y);
            }

            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
            dial.draw(canvas);
            // Add the shadow for secondhand
            canvas.save();
            canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);
            final Drawable minutehand = mSecondHand;
            final Drawable minutehandShadow = mSecondHandShadow;

            w = minutehand.getIntrinsicWidth();
            h = minutehand.getIntrinsicHeight();

            // add shadow
            if (mMinutes < 22.5 || mMinutes > 52.5) {
                offsetX = 1;
            } else if (mMinutes > 22.5 && mMinutes < 52.5) {
                offsetX = -4;
            } else {
                offsetX = 0;
            }
            if (mMinutes > 7.5 && mMinutes < 22.5) {
                offsetY = ((int) (mMinutes - 7.5) * 5) / 15;
            } else if (mMinutes > 22.5 && mMinutes < 37.5) {
                offsetY = ((int) (37.5 - mMinutes) * 5) / 15;
            } else if (mMinutes > 37.5 && mMinutes < 52.5) {
                offsetY = -((int) (mMinutes - 37.5) * 5) / 15;
            } else if (mMinutes < 7.5 && mMinutes > 52.5) {
                offsetY = -((int) (67.5 - mMinutes) * 5) / 15;
            } else {
                offsetY = 0;
            }
            minutehandShadow.setBounds(x - (w / 2) + offsetX, y - (h / 2) + offsetY, x + (w / 2)
                    + offsetX, y + (h / 2) + offsetY);

            minutehandShadow.draw(canvas);

            minutehand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));

            minutehand.draw(canvas);
            //
            canvas.restore();
            canvas.save();
            canvas.rotate(mHours / 12.0f * 360.0f, x, y);

            final Drawable hourHand = mMinuteHand;
            final Drawable hourHandShadow = mMinuteHandShadow;

            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();

            // add shadow
            if (mHours < 4.5 || mHours > 10.5) {
                offsetX = 1;
            } else if (mHours > 4.5 && mHours < 10.5) {
                offsetX = -4;
            } else {
                offsetX = 0;
            }
            if (mHours > 1.5 && mHours < 4.5) {
                offsetY = -((int) (mHours - 1.5) * 10) / 3;
            } else if (mHours > 4.5 && mHours < 7.5) {
                offsetY = -((int) (10.5 - mHours) * 5) / 3;
            } else if (mHours > 7.5 && mHours < 10.5) {
                offsetY = ((int) (mHours - 10.5) * 5) / 3;
            } else if (mHours < 10.5 && mHours > 1.5) {
                offsetY = ((int) (13.5 - mHours) * 5) / 3;
            } else {
                offsetY = 0;
            }
            hourHandShadow.setBounds(x - (w / 2) + offsetX, y - (h / 2) + offsetY, x + (w / 2)
                    + offsetX, y + (h / 2) + offsetY);
            hourHandShadow.draw(canvas);
            canvas.save();

            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));

            hourHand.draw(canvas);

            canvas.restore();

        drawSecond(canvas, x, y);

    }

    private void drawSecond(Canvas canvas, int x, int y) {
        int w;
        int h;
        w = mRealSecond.getIntrinsicWidth();
        h = mRealSecond.getIntrinsicHeight();
        canvas.save();
        mRealSecond.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        canvas.rotate(mCurrentSecond / 60.0f * 360.0f, x, y);
        mRealSecond.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float) heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSize((int) (mDialWidth * scale), widthMeasureSpec),
                resolveSize((int) (mDialHeight * scale), heightMeasureSpec));
    }

    public void setTime(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        mMinutes = minute + second / 60.0f;
        mHours = hour + mMinutes / 60.0f;

        mCurrentSecond = second;
        mUpdateHandler.removeCallbacks(mUpdateSecondRunnable);
        invalidate();
        updateSecond();
    }

    private void updateSecond() {
        
        mUpdateHandler.postDelayed(mUpdateSecondRunnable, 1000);

    }

}
