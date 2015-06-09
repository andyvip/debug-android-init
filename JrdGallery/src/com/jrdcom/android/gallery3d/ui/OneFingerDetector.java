package com.jrdcom.android.gallery3d.ui;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class OneFingerDetector {

//    private static final String TAG = "OneFingerDetector";
    private GestureDetector mGestureDetector = null;
    private OnZoomerListener mListener = null;
    private boolean mIsZooming = false;
    private float mOneTimeDistance = 400f;
    private float mLastY = 0f;
    private float mFactor = 1f;
//    private float mFocusedX = -1.0f;
//    private float mFocusedY = -1.0f;
//    private long mBeginTimeMillis = -1;
    private float mFocusedRawX = -1.0f;
    private float mFocusedRawY = -1.0f;
    private final int TOUCH_SLOP;
    private boolean mDoubleTapDetected = false;

    public OneFingerDetector(Context context, OnZoomerListener listener) {
        super();
        TOUCH_SLOP = ViewConfiguration.get(context).getScaledTouchSlop();
        mListener = listener;
        mOneTimeDistance = (float) context.getResources().getDisplayMetrics().heightPixels / 2.0f;
        mGestureDetector = new GestureDetector(context, mGestureListener);
        mGestureDetector.setOnDoubleTapListener(mGestureListener);
    }

    public interface OnZoomerListener {
        void onZoomBegin(MotionEvent event);

        void onZoomEnd();

        void onZoom(float zoom);
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        public boolean onDoubleTap(MotionEvent e) {
//            Log.d(TAG, "-------------------------onDoubleTap");
            mDoubleTapDetected = true;
            mIsZooming = false;
            mLastY = e.getRawY();
            mFactor = 1.0f;
//            mFocusedX = e.getX();
//            mFocusedY = e.getY();
            mFocusedRawX = e.getRawX();
            mFocusedRawY = e.getRawY();
            if (null != mListener) {
                mListener.onZoomBegin(e);
            }
            return true;
        };
    };

    public boolean isZooming() {
        return mIsZooming;
    }

    public float getFactor() {
        return mFactor;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean isZoomingUp = false;
        if(mGestureDetector.onTouchEvent(ev)){
            return mIsZooming;
        }
        if (mDoubleTapDetected) {
            switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
//                Log.d(TAG, "-------------------------MotionEvent.ACTION_UP");
                isZoomingUp = true;
//            case MotionEvent.ACTION_DOWN:
//                Log.d(TAG, "-------------------------MotionEvent.ACTION_DOWN");
                if (null != mListener) {
                    mListener.onZoomEnd();
                }
//                mFocusedX = -1.0f;
//                mFocusedY = -1.0f;
                mDoubleTapDetected = false;
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "-------------------------MotionEvent.ACTION_MOVE");
                // check whether enter zooming mode
                if (!mIsZooming) {
                    float distanceX = ev.getRawX() - mFocusedRawX;
                    float distanceY = ev.getRawY() - mFocusedRawY;
                    float distance = (float)Math.sqrt(distanceX*distanceX + distanceY*distanceY);
                    if (distance > TOUCH_SLOP) {
                        mIsZooming = true;
                    }
                }
                if (mIsZooming) {
                    float deltaY = ev.getRawY() - mLastY;
                    float zoomRate = 1 + (deltaY / mOneTimeDistance);
                    mFactor *= zoomRate;
                    if (null != mListener) {
                        mListener.onZoom(zoomRate);
                    }
                    mLastY = ev.getRawY();
                }
                break;
            default:
                break;

            }
        }

        return mIsZooming || isZoomingUp;
    }
}
