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

package com.jrdcom.android.gallery3d.ui;

import com.jrdcom.android.gallery3d.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jrdcom.android.gallery3d.app.GalleryAppImpl;

public class PopupWindowFrameLayout extends RelativeLayout {
    Context mContext;
    private boolean waitDouble = true;
    private static final int DOUBLE_CLICK_TIME = 300;
    private static final int INVISIBLE_CANCEL_ICON = 1;
    private static final int RESPONSE_SINGLE_CLICK = 2;
    private static final int RESPONSE_DOUBLE_CLICK = 3;
    private static final int RESCALE_WINDOW_SIZE = 4;
    private static final int RESPONSE_ORIENTATION_CHANGED = 5;
    int mOrientation;
    private float point_x;
    private float point_y;
    private float last_x = -1;
    private float last_y = -1;
    private float x;
    private float y;
    private PopupWindowFrameLayout fl;
    private FrameLayout cancelIcon;
    private WindowManager wm;
    private WindowManager.LayoutParams wmParams;
    private boolean mCanScale = false;//add by qjz for PR480767 20130727

    // double touch rescale zoom in
    float scale = 1;
    final static int MAX_TOUCHPOINTS = 2;
    int touchPointerNumber = 0;
    float baseValue = 0;
    long lastUpdate = 0;
    // int display width and height
    int displayWidth;
    int displayHeight;
    double widthLengthRatio = 0;
    int videoWidth;
    int videoHeight;
    private Handler mWindowScaleHandler;
    private MotionEvent mEvent;
    private long touchDownTime;
    private long touchUpTime;
    private long clickEventDelay=150;
    private final static String TAG="PopupWindowFrameLayout";
    Handler handler = new MainHandler();

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case INVISIBLE_CANCEL_ICON:
                if (cancelIcon != null)
                    cancelIcon.setVisibility(View.GONE);
                break;
            case RESPONSE_SINGLE_CLICK: {
                /*cancelIcon.setVisibility(View.VISIBLE);
                handler.removeMessages(RESPONSE_DOUBLE_CLICK);
                handler.removeMessages(INVISIBLE_CANCEL_ICON);
                handler.sendEmptyMessageDelayed(INVISIBLE_CANCEL_ICON, 2000);*/
                // send broadcast to control play status, such as play, stop
                if (waitDouble) {
                    break;
                }
                Intent intent = new Intent();
                intent.setAction("com.jrdcom.action.CHANGE_PLAY_STATUS");
                mContext.startService(intent);
                waitDouble = true;
            }
                break;
            case RESPONSE_DOUBLE_CLICK: {
                Intent intent = new Intent();
                intent.setAction("com.jrdcom.action.BACK_TO_NORMAL_WINDOW");
                mContext.startService(intent);
            }
                break;
            case RESCALE_WINDOW_SIZE:
                wmParams = ((GalleryAppImpl) mContext.getApplicationContext()).getMywmParams();
                videoWidth = wmParams.width;
                videoHeight = wmParams.height;
                //Log.i("yanlong", "RESCALE_WINDOW_SIZE: width "+videoWidth+" videoHeight "+videoHeight);
                try {
                    if (fl.isEnabled()) {
                        wm.updateViewLayout(fl, wmParams);
                    }
                }catch (IllegalArgumentException ex) {
                    Log.d(TAG,ex.toString());
                }
                //Log.i(TAG, " after scale window size x "+wmParams.x+" y "+wmParams.y);
                break;
            case RESPONSE_ORIENTATION_CHANGED:
                wmParams = ((GalleryAppImpl) mContext.getApplicationContext()).getMywmParams();
                int currentWidth=wmParams.width;
                int currentHeight=wmParams.height;
                int scaleWidth=0,scaleHeight=0;
                //modified by qjz for PR481391 20130706 begin
                int width = displayWidth;
                if (displayWidth > displayHeight) {
                    width = displayHeight;
                }
                if(mOrientation==Configuration.ORIENTATION_PORTRAIT){
                    //landscape 2 portrait
                       if(currentWidth>width){
                           scaleWidth=width;
                           scaleHeight=(int)(scaleWidth/widthLengthRatio);
                       }
                }else{
                    //portrait 2 landscape
                    if(currentHeight>width){
                        scaleHeight=width;
                        scaleWidth=(int)(scaleHeight*widthLengthRatio);
                    }
                }
                //modified by qjz for PR481391 20130706 end
                if(scaleWidth!=0&&scaleHeight!=0){
                wmParams.width=scaleWidth;
                wmParams.height=scaleHeight;
                }
                videoWidth = wmParams.width;
                videoHeight = wmParams.height;
                wm.updateViewLayout(fl, wmParams);
                break;
            }
        }
    }

    public PopupWindowFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public PopupWindowFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public PopupWindowFrameLayout(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        wm = (WindowManager) mContext.getApplicationContext().getSystemService("window");
        wmParams = ((GalleryAppImpl) mContext.getApplicationContext()).getMywmParams();
        HandlerThread handlerThread = new HandlerThread("processWindowScale");
        handlerThread.start();
        mWindowScaleHandler = new Handler(handlerThread.getLooper());
    }

    private Thread mHandleScaleThread = new Thread() {
        public void run() {
            try{
            if(mEvent.getPointerCount()<2) return;
            float x = mEvent.getX(mEvent.getPointerId(0)) - mEvent.getX(mEvent.getPointerId(1));
            float y = mEvent.getY(mEvent.getPointerId(0)) - mEvent.getY(mEvent.getPointerId(1));
            float value = (float) Math.sqrt(x * x + y * y);//
            if (baseValue == 0.0) {
                baseValue = value;
            } else {
                if (value != baseValue) {
                    scale = value / baseValue;
                    float compensation = 0;
                    popupWindowScale(scale + compensation);
                    baseValue = value;
                }
            }
        }catch(Exception ex){
            Log.i(TAG, "mHandleScaleThread process rescale fail!");
        }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        if (pointerCount > MAX_TOUCHPOINTS) {
            pointerCount = MAX_TOUCHPOINTS;
        }
        if (pointerCount == 2 && (touchPointerNumber == 1 || touchPointerNumber == 0)) {
            touchPointerNumber = 2;
            baseValue = 0;
        }

        point_x = wmParams.x;
        point_y = wmParams.y;
        x = event.getRawX();
        y = event.getRawY();

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (pointerCount == 1) {
                if (last_x == -1 || last_y == -1) {// else meint there is
                                                   // already on touch point
                    touchPointerNumber = 1;
                    last_x = event.getRawX();
                    last_y = event.getRawY();
                    touchDownTime=System.currentTimeMillis();
                }
            }
            break;

        case MotionEvent.ACTION_MOVE:
            //Log.i("yanlong", "action move from popupwindow pointerCount: "+pointerCount+"  touchPointerNumber: "+touchPointerNumber);
            //PR485495 486691 Gallery force close when zoom in/out popup video. 
            //modified by qjz 20130715 begin
            if (event.getPointerCount() == 2) {
                try {
                    long curTime = System.currentTimeMillis();
                    if ((curTime - lastUpdate) > 50) {
                        lastUpdate = curTime;
                        touchPointerNumber = 2;
                        //mEvent = event;
                        //mWindowScaleHandler.post(mHandleScaleThread);
                        float x = event.getX(event.getPointerId(0)) - event.getX(event.getPointerId(1));
                        float y = event.getY(event.getPointerId(0)) - event.getY(event.getPointerId(1));
                        float value = (float) Math.sqrt(x * x + y * y);
                        if (baseValue == 0.0) {
                            baseValue = value;
                        } else {
                            if (value != baseValue&&Math.abs(baseValue-value)>0.5) {
                                scale = value / baseValue;
                                popupWindowScale(scale);
                                baseValue = value;
                            }
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    Log.w(TAG, ex.toString());
                }
            } else if (event.getPointerCount() == 1 && touchPointerNumber == 1) {
                //Log.i("yanlong", "action move from popupwindow updateViewPosition");
                updateViewPosition();
                last_x = event.getRawX();
                last_y = event.getRawY();
            }
            //PR485495 486691 Gallery force close when zoom in/out popup video. 
            //modified by qjz 20130715 end
            break;

        case MotionEvent.ACTION_UP:
            if (touchPointerNumber == 1) {
                updateViewPosition();
                touchPointerNumber = 0;
                last_x = -1;
                last_y = -1;
                touchUpTime=System.currentTimeMillis();
            } else if (touchPointerNumber == 2) {
                touchPointerNumber = 1;
                last_x = -1;
                last_y = -1;
            }

            break;
        }
        if (pointerCount == 1) {// single touch
            if ((touchUpTime-touchDownTime)<clickEventDelay){
                if(event.getAction()==MotionEvent.ACTION_UP){
                    //one more condition for event is action_up
                    if(touchUpTime>touchDownTime)
                        return super.onTouchEvent(event);
                }else{
                return super.onTouchEvent(event);
                }
            }
        }
        return true;
    }

    private void popupWindowScale(float scale2) {
        //TODO resolve screen orientation exchange
        if (!mCanScale) return;//add by qjz for PR480767 20130727
        int width,height;
        width = (int) (videoWidth * scale2);
        height = (int) (videoHeight * scale2);
        //modified by qjz for PR481391 20130706 begin
        /*if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            // lanscape
            height = (height >= displayWidth) ? displayWidth : height;
            width = (int) (height * widthLengthRatio);
            width = width >= displayHeight ? displayHeight : width;
        } else*/ {
            // portrait
            width = width >= displayWidth ? displayWidth : width;
            //PR488083:[popup video]Portrait/Landscape minimum length and height is not same with ergo
            //modified by qjz 20130715 begin
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE ) {
                if (width < displayHeight/2) {
                    width = displayHeight/2;
                }
            } else {
                if (width < displayWidth/2) {
                    width = displayWidth/2;
                }
            }
            //PR488083:[popup video]Portrait/Landscape minimum length and height is not same with ergo
            //modified by qjz 20130715 end
            height = (int) (width / widthLengthRatio);
            height = (height >= displayHeight) ? displayHeight : height;
        }
        //modified by qjz for PR481391 20130706 end
        wmParams.width = width;
        wmParams.height = height;
        ((GalleryAppImpl) mContext.getApplicationContext())
                .setMywmParams(wmParams);
        handler.removeMessages(RESCALE_WINDOW_SIZE);
        handler.sendEmptyMessage(RESCALE_WINDOW_SIZE);
    }

    private void updateViewPosition() {
        if (fl.isEnabled()) {
            wmParams.x = (int) (point_x + (x - last_x));
            wmParams.y = (int) (point_y + (y - last_y));
            wm.updateViewLayout(this, wmParams);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        fl = (PopupWindowFrameLayout) findViewById(R.id.popup_root);
        cancelIcon = (FrameLayout) findViewById(R.id.cancel_icon);
        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Log.i(TAG, " fl in PopupWindowFramelayout onclock event ");
                if (waitDouble == true) {
                    waitDouble = false;
                    /*Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                sleep(DOUBLE_CLICK_TIME);
                                if (waitDouble == false) {
                                    waitDouble = true;
                                    singleClick();
                                }
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();*/
                    //((GalleryAppImpl) mContext.getApplicationContext()).setMywmParams(wmParams);
                    cancelIcon.setVisibility(View.VISIBLE);
                    handler.removeMessages(INVISIBLE_CANCEL_ICON);
                    handler.removeMessages(RESPONSE_SINGLE_CLICK);
                    handler.sendEmptyMessageDelayed(INVISIBLE_CANCEL_ICON, 3000);
                    handler.sendEmptyMessageDelayed(RESPONSE_SINGLE_CLICK, DOUBLE_CLICK_TIME);
                } else {
                    waitDouble = true;
                    fl.setEnabled(false);
                    handler.removeMessages(RESPONSE_SINGLE_CLICK);
                    Intent intent = new Intent();
                    intent.setAction("com.jrdcom.action.BACK_TO_NORMAL_WINDOW");
                    mContext.startService(intent);
                    //doubleClick();
                }

            }
        });
    }

    public void singleClick() {
        //Log.i("yanlong", "singleclick");
        ((GalleryAppImpl) mContext.getApplicationContext()).setMywmParams(wmParams);
        handler.removeMessages(RESPONSE_SINGLE_CLICK);
        handler.sendEmptyMessage(RESPONSE_SINGLE_CLICK);
    }

    private void doubleClick() {
        //Log.i("yanlong", "doubleclick");
        ((GalleryAppImpl) mContext.getApplicationContext()).setMywmParams(wmParams);
        handler.removeMessages(RESPONSE_DOUBLE_CLICK);
        handler.sendEmptyMessage(RESPONSE_DOUBLE_CLICK);

    }

    public void rescalePopWindowSize(int displayWidth, int displayHeight, double widthLengthRatio) {
        //Log.i("yanlong", "rescalePopWindowSize displayWidth " + displayWidth + " displayHeight  "
        //        + displayHeight + " widthLengthRatio " + widthLengthRatio);
        mCanScale = true;//add by qjz for PR480767 20130727
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.widthLengthRatio = widthLengthRatio;
        handler.removeMessages(RESCALE_WINDOW_SIZE);
        handler.sendEmptyMessage(RESCALE_WINDOW_SIZE);
    }
    
    public void informOrientationChanged(int orientation){
        mOrientation=orientation;
        //modified by qjz for PR481391 20130706 begin
        if ((mOrientation==Configuration.ORIENTATION_PORTRAIT && displayWidth > displayHeight) ||
            (mOrientation==Configuration.ORIENTATION_LANDSCAPE && displayWidth < displayHeight)) {
            int temp = displayWidth;
            displayWidth = displayHeight;
            displayHeight = temp;
        }
        //modified by qjz for PR481391 20130706 end
        handler.removeMessages(RESPONSE_ORIENTATION_CHANGED);
        handler.sendEmptyMessage(RESPONSE_ORIENTATION_CHANGED);
    }

}