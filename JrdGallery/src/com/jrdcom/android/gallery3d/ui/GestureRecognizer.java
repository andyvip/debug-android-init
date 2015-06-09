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

import android.content.Context;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

// This class aggregates three gesture detectors: GestureDetector,
// ScaleGestureDetector, and DownUpDetector.
public class GestureRecognizer {
    @SuppressWarnings("unused")
    private static final String TAG = "GestureRecognizer";

    public interface Listener {
        boolean onSingleTapUp(float x, float y);
        boolean onDoubleTap(float x, float y);
        //add by biao.luo@tct-nj.com begin
        boolean onScroll(float dx, float dy, float totalX, float totalY, int zoom);
        //According to the number of points to determine the implementation of that event
        //add by biao.luo@tct-nj.com end
        boolean onFling(float velocityX, float velocityY);
        boolean onScaleBegin(float focusX, float focusY);
        boolean onScale(float focusX, float focusY, float scale);
        void onScaleEnd();
        void onDown(float x, float y);
        void onUp();
        void onLongPressed(float x, float y);/* PR464782 ming.zhang modify */
    }

    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private final DownUpDetector mDownUpDetector;
    private Listener mListener; //for change
    // FR583902-renjie.xu-begin
    //PR912344 Add One finger zoom function by fengke at 2015.01.26 start
    public static boolean isDoubleTap = false;
    private final OneFingerDetector mOneFingerDetector;
    //PR912344 Add One finger zoom function by fengke at 2015.01.26 end
    // FR583902-renjie.xu-end

    public GestureRecognizer(Context context, Listener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new MyGestureListener(),
                null, true /* ignoreMultitouch */);
        mScaleDetector = new ScaleGestureDetector(
                context, new MyScaleListener());
        mDownUpDetector = new DownUpDetector(new MyDownUpListener());
        // FR583902-renjie.xu-begin
        //PR912344 Add One finger zoom function by fengke at 2015.01.26 start
        mOneFingerDetector = new OneFingerDetector(context, new MyOneFingerListener());
        //PR912344 Add One finger zoom function by fengke at 2015.01.26 end
        // FR583902-renjie.xu-end
    }

    public void onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        mDownUpDetector.onTouchEvent(event);
        // FR583902-renjie.xu-begin
        //PR912344 Add One finger zoom function by fengke at 2015.01.26 start
        mOneFingerDetector.onTouchEvent(event);
        //PR912344 Add One finger zoom function by fengke at 2015.01.26 end
        // FR583902-renjie.xu-end
    }

    public boolean isDown() {
        return mDownUpDetector.isDown();
    }

    public void cancelScale() {
        long now = SystemClock.uptimeMillis();
        MotionEvent cancelEvent = MotionEvent.obtain(
                now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mScaleDetector.onTouchEvent(cancelEvent);
        cancelEvent.recycle();
    }

    /// M: camera will handle some gesture for new features @{
    public Listener setGestureListener(Listener listener) {
        Listener old = mListener;
        mListener = listener;
        return old;
    }
    /// @}

    private class MyGestureListener
                extends GestureDetector.SimpleOnGestureListener {
        // M: modified for MTK UX issues:
        // use onSingleTapConfirmed to avoid action bar from poping up
        // during double tap gesture.
        //@Override
        //public boolean onSingleTapUp(MotionEvent e) {
        //    return mListener.onSingleTapUp(e.getX(), e.getY());
        //}
    	public final int dinstance=100;
    	public final int dinstancex=-100;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // FR583902-renjie.xu-begin
            //PR912344 Add One finger zoom function by fengke at 2015.01.26 start
            // return mListener.onDoubleTap(e.getX(), e.getY());
            isDoubleTap = true;
            return false;
            //PR912344 Add One finger zoom function by fengke at 2015.01.26 end
            // FR583902-renjie.xu-end
        }

        @Override
        public boolean onScroll(
                MotionEvent e1, MotionEvent e2, float dx, float dy) {
            // add by biao.luo@tct-nj.com begin
            // modify by hui.xu@2013/2/28 begin
            // Gesture delicacy

            //PR817590, 817587,817574 modify for gallery display abnormal by fengke at 2014.10.23 start
            //add-lukeke-PR557769-20131220(e1.getY() - e2.getY() > dinstance|| e1.getY() - e2.getY() < dinstancex)
			//if (e1.getX() - e2.getX() > dinstance
				//	|| e1.getX() - e2.getX() < dinstancex
					//|| e1.getY() - e2.getY() > dinstance
					//|| e1.getY() - e2.getY() < dinstancex) {
            return mListener.onScroll(dx, dy, e2.getX() - e1.getX(),
                    e2.getY() - e1.getY(), e2.getPointerCount());
			//} else {
				//return false;
			//}
            // modify by hui.xu@2013/2/28 end
            //PR817590, 817587,817574 modify for gallery display abnormal by fengke at 2014.10.23 end

            // According to the number of points to determine the implementation
            // of that event
            // add by biao.luo@tct-nj.com end
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            return mListener.onFling(velocityX, velocityY);
        }
        
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return mListener.onSingleTapUp(e.getX(), e.getY());
        }
        /* PR464782 ming.zhang modify begin
         * LongPressed to take a photo
         * */
        @Override
        public void onLongPress(MotionEvent e) {
            mListener.onLongPressed(e.getX(), e.getY());
        }
        /* PR464782 ming.zhang modify end */
    }

    private class MyScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return mListener.onScaleBegin(
                    detector.getFocusX(), detector.getFocusY());
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return mListener.onScale(detector.getFocusX(),
                    detector.getFocusY(), detector.getScaleFactor());
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mListener.onScaleEnd();
        }
    }

    private class MyDownUpListener implements DownUpDetector.DownUpListener {
        @Override
        public void onDown(MotionEvent e) {
            mListener.onDown(e.getX(), e.getY());
        }

        @Override
        public void onUp(MotionEvent e) {
            // FR583902-renjie.xu-begin
            //PR912344 Add One finger zoom function by fengke at 2015.01.26 start
            // mListener.onUp();
            if (isDoubleTap && !mOneFingerDetector.isZooming()) {
                mListener.onDoubleTap(e.getX(), e.getY());
                //PR607194,v-nj-feiqiang.cheng,add begin fengke porting to soul4.5 2014.02.28
                mListener.onUp();
                //PR607194,v-nj-feiqiang.cheng,add end fengke porting to soul4.5 2014.02.28
            } else {
                mListener.onUp();
            }
            isDoubleTap = false;
            //PR912344 Add One finger zoom function by fengke at 2015.01.26 end
            // FR583902-renjie.xu-end
        }
    }
    
    // FR583902-renjie.xu-begin
    //PR912344 Add One finger zoom function by fengke at 2015.01.26 start
    private class MyOneFingerListener implements
            OneFingerDetector.OnZoomerListener {

        float mFocusX = 0.0f;
        float mFocusY = 0.0f;

        @Override
        public void onZoomBegin(MotionEvent e) {
            mFocusX = e.getX();
            mFocusY = e.getY();
            mListener.onScaleBegin(mFocusX, mFocusY);
        }

        @Override
        public void onZoomEnd() {
            mListener.onScaleEnd();
        }

        @Override
        public void onZoom(float zoom) {
            mListener.onScale(mFocusX, mFocusY, zoom);
        }

    }
    //PR912344 Add One finger zoom function by fengke at 2015.01.26 end
    // FR583902-renjie.xu-end
}
