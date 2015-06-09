/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.jrdcom.android.gallery3d.app;

import android.app.Activity;//PR501083-kuiwang-001
import android.content.Context;
import android.content.Intent;//PR501083-kuiwang-001
import android.content.res.Resources.NotFoundException;//FR450344-yanlong.li@tcl.com
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.app.CommonControllerOverlay.State;
import com.jrdcom.mediatek.gallery3d.ext.IContrllerOverlayExt;
import com.jrdcom.mediatek.gallery3d.video.IControllerRewindAndForward;
import com.jrdcom.mediatek.gallery3d.video.IControllerRewindAndForward.IRewindAndForwardListener;
import com.jrdcom.mediatek.gallery3d.util.MtkLog;
import com.jrdcom.mediatek.gallery3d.video.ExtensionHelper;
import com.jrdcom.mediatek.gallery3d.video.ScreenModeManager;
import com.jrdcom.mediatek.gallery3d.video.ScreenModeManager.ScreenModeListener;
import android.view.GestureDetector;//modified by junliang.liu.hz for CR604778

/**
 * The playback controller for the Movie Player.
 */
public class MovieControllerOverlay extends CommonControllerOverlay implements
        AnimationListener,GestureDetector.OnGestureListener{
    /*
    * PR 620961 gangchen@tcl.com increase the follow thing.
    */
    final float scale = getResources().getDisplayMetrics().density;
	//PR804309 added by junliang.liu.hz at 2014.10.13 begin
	private static final int SEEKTO_GROUND_WIDTH_PX = 118;
	private static final int SEEKTO_GROUND_HEIGHT_PX = 70;
	private static final int SEEKTO_GROUND_PADDING_BOTTOM = 65;
	//PR804309 added by junliang.liu.hz at 2014.10.13 end
    //add by junliang.liu.hz for CR604778 begin
    private static final int GESTURE_FB_DLG_WIDTH = 120;
    private static final int GESTURE_FB_DLG_HEIGHT = 36;
    //add by junliang.liu.hz for CR604778 end
    private boolean hidden;

    private final Handler handler;
    private final Runnable startHidingRunnable;
    private final Animation hideAnimation;

    private boolean enableRewindAndForward = false;
    private Context mContext;
    private ImageView mLoopView;
    private ImageView mPlayControlView;
    //private ImageView mShowSeekTo;//add by qjz for NewVedioPlayerUI 20130405
    private BorderImageView mShowSeekTo;//add by qjz for NewVedioPlayerUI 20130410
    private TextView mFBNotifyView;//add by junliang.liu.hz for CR604778
    //private ImageView mSeekToView[]=new ImageView[9];
    private static final int MARGIN = 10; // dip
    public int mPreCurrentPosition = -1;
    private boolean mIsShow = false;
    private boolean mIsLand = false;
    private int mWidth;
    private int mHeight;
    boolean popupVideo = false;//FR450344-yanlong.li@tcl.com
    boolean mIsRTSP = false;//add by qjz for PR463745 20130605
    boolean mIsAttachment;// add by yaping.liu for pr506689
    private GestureDetector mAllGesture;//add by junliang.liu.hz for CR604778

    public MovieControllerOverlay(Context context, boolean isAttachment) {
        super(context);
        mIsAttachment = isAttachment;// add by yaping.liu for pr506689
        mContext = context;
        handler = new Handler();
        startHidingRunnable = new Runnable() {
            @Override
            public void run() {
                startHiding();
            }
        };

        hideAnimation = AnimationUtils
                .loadAnimation(context, R.anim.player_out);
        hideAnimation.setAnimationListener(this);


        if (ExtensionHelper.getMovieStrategy(context).shouldEnableRewindAndForward()) {
            enableRewindAndForward = true;
            MtkLog.v(TAG, "enableRewindAndForward is " + enableRewindAndForward);
            mControllerRewindAndForwardExt.init(context);
        }
        //FR450344-yanlong.li@tcl.com-begin
        try{
            //PR519068,In lanscape mode, it shows black window when play drm video via pop up video,bin.li@tct-nj.com
            if(MovieActivity.isDrm || mIsAttachment) popupVideo=false;//modify by yaping.liu for pr506689
            else popupVideo =  mContext.getResources().getBoolean(R.bool.def_gallery_pop_up_video);
            //end PR519068
            }catch(NotFoundException nfe){
            	MtkLog.v(TAG, " not found customize key def_gallery_pop_up_video ");
            }
      //FR450344-yanlong.li@tcl.com-end
        mScreenModeExt.init(context, mTimeBar);
        //del by qjz for NewVedioPlayerUI 201305011 begin
        /*LayoutParams wrapContent =
                        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mLoopView = new ImageView(context);
        mLoopView.setScaleType(ScaleType.CENTER);
        mLoopView.setFocusable(true);
        mLoopView.setClickable(true);
        mLoopView.setOnClickListener(this);
        addView(mLoopView, wrapContent);

        mPlayControlView = new ImageView(context);
        mPlayControlView.setScaleType(ScaleType.CENTER);
        mPlayControlView.setFocusable(true);
        mPlayControlView.setClickable(true);
        mPlayControlView.setOnClickListener(this);
        addView(mPlayControlView, wrapContent);
        
        mShowSeekTo = new BorderImageView(context);
        mShowSeekTo.setScaleType(ScaleType.CENTER);
        mShowSeekTo.setFocusable(true);
        addView(mShowSeekTo, wrapContent);
        //add by qjz for NewVedioPlayerUI 201305011 end*/
        setLoopViewBackground(mCanReplay);
        setPlayControlViewBackground(mState==State.PLAYING?true:false);
        mAllGesture = new GestureDetector(this);//add by junliang.liu.hz for CR604778
        hide();
    }
    //add by qjz for PR463745 20130606
    public void setVideoType(boolean isRTSP) {
        mIsRTSP = isRTSP;
        setLoopViewBackground(mCanReplay);
    }
    //add by qjz for NewUI
    public void setLoopViewBackground(boolean isLoop) {
        //FR450344-yanlong.li@tcl.com-begin
        if (popupVideo) {
            mLoopView.setImageResource(R.drawable.popup_window_button_selector);
        } else {
          //FR450344-yanlong.li@tcl.com-end
            //add by qjz for PR463745 20130606 begin
            if (mIsRTSP) {
                mLoopView.setEnabled(false);
                if (isLoop) {
                    mLoopView.setImageResource(R.drawable.loop_disable);
                } else {
                    mLoopView.setImageResource(R.drawable.no_loop_disable);
                }
            } else {
                mLoopView.setEnabled(true);
                if (isLoop) {
                    mLoopView.setImageResource(R.drawable.vedio_loop);// modified by qjz for
                                                                      // NewVedioPlayerUI 20130405
                } else {
                    mLoopView.setImageResource(R.drawable.vedio_noloop);// modified by qjz for
                                                                        // NewVedioPlayerUI 20130405
                }
            }
            //add by qjz for PR463745 20130606 end
        }
    }

    public void setPlayControlViewBackground(boolean isPlay) {
        if (isPlay) {
            mPlayControlView.setImageResource(R.drawable.vedio_pause);//modified by qjz for NewVedioPlayerUI 20130405 
        } else {
            mPlayControlView.setImageResource(R.drawable.vedio_play);//modified by qjz for NewVedioPlayerUI 20130405 
        }
    }
    public void showPlaying() {
        if (!mOverlayExt.handleShowPlaying()) {
            mState = State.PLAYING;
            //modified by qjz for New UI 2013-03-14
            //setPlayControlViewBackground(true);
            showMainView(null);//modified by qjz for PR461095 20130603
        }
        if (LOG) {
            MtkLog.v(TAG, "showPlaying() state=" + mState);
        }
    }

    public void showPaused() {
        if (!mOverlayExt.handleShowPaused()) {
            mState = State.PAUSED;
            //modified by qjz for New UI 2013-03-14
            //setPlayControlViewBackground(false);
            showMainView(null);//modified by qjz for PR461095 20130603
        }
        if (LOG) {
            MtkLog.v(TAG, "showPaused() state=" + mState);
        }
    }

    public void showEnded() {
        mOverlayExt.onShowEnded();
        mState = State.ENDED;
        //modified by qjz for New UI 2013-03-14
        //setPlayControlViewBackground(false);
        showMainView(null);//modified by qjz for PR461095 20130603
        if (LOG) {
            MtkLog.v(TAG, "showEnded() state=" + mState);
        }
    }

    public void showLoading() {
        mOverlayExt.onShowLoading();
        mState = State.LOADING;
        showMainView(mLoadingView);
        if (LOG) {
            MtkLog.v(TAG, "showLoading() state=" + mState);
        }
    }

    public void showErrorMessage(String message) {
        mOverlayExt.onShowErrorMessage(message);
        mState = State.ERROR;
        int padding = (int) (getMeasuredWidth() * ERROR_MESSAGE_RELATIVE_PADDING);
        mErrorView.setPadding(padding, mErrorView.getPaddingTop(), padding,
                mErrorView.getPaddingBottom());
        mErrorView.setText(message);
        showMainView(mErrorView);
    }

    @Override
    protected void createTimeBar(Context context) {
        mTimeBar = new TimeBar(context, this);
    }

    @Override
    public void hide() {
        boolean wasHidden = hidden;
        hidden = true;
        //del by qjz for New UI 2013-03-14
        //mPlayPauseReplayView.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.INVISIBLE);
        // /M: pure video only show background. @{
        if (!mOverlayExt.handleHide()) {
            // background.setVisibility(View.INVISIBLE);
            // timeBar.setVisibility(View.INVISIBLE);
            setVisibility(View.INVISIBLE);
            // mScreenModeExt.onHide();
        }
        mBackground.setVisibility(View.INVISIBLE);
        mTimeBar.setVisibility(View.INVISIBLE);
        mPlayControlView.setVisibility(View.INVISIBLE);//add by qjz for NewUI
        mLoopView.setVisibility(View.INVISIBLE);//add by qjz for NewUI
        mScreenModeExt.onHide();
        if (enableRewindAndForward) {
            mControllerRewindAndForwardExt.onHide();
        }
        // /@}
        setFocusable(true);
        requestFocus();
        if (mListener != null && wasHidden != hidden) {
            mListener.onHidden();
        }
        if (LOG) {
            MtkLog.v(TAG, "hide() wasHidden=" + wasHidden + ", hidden="
                    + hidden);
        }
    }

    private void showMainView(View view) {
        //modified by qjz for PR461095 20130603
        if (view != null) {
            mMainView = view;
            mErrorView.setVisibility(mMainView == mErrorView ? View.VISIBLE
                    : View.INVISIBLE);
            mLoadingView.setVisibility(mMainView == mLoadingView ? View.VISIBLE
                    : View.INVISIBLE);
            //del by qjz for New UI 2013-03-14
            /*mPlayPauseReplayView
                    .setVisibility(mMainView == mPlayPauseReplayView ? View.VISIBLE
                            : View.INVISIBLE);*/
        } else {
            mErrorView.setVisibility(View.INVISIBLE);
            mLoadingView.setVisibility(View.INVISIBLE);
        }
        mOverlayExt.onShowMainView(view);
        show();
    }

    @Override
    public void show() {
        boolean wasHidden = hidden;
        hidden = false;
        updateViews();
        setVisibility(View.VISIBLE);
        setFocusable(false);
        if (mListener != null && wasHidden != hidden) {
            mListener.onShown();
        }
        maybeStartHiding();
        if (LOG) {
            MtkLog.v(TAG, "show() wasHidden=" + wasHidden + ", hidden="
                    + hidden + ", listener=" + mListener);
        }
        //add by qjz for PR461095 20130603
        setPlayControlViewBackground(mState == State.PLAYING||mState == State.BUFFERING);
    }

    private void maybeStartHiding() {
        cancelHiding();
        if (mState == State.PLAYING) {
            handler.postDelayed(startHidingRunnable, 5000);//PR850351 by junliang.liu at 20141126
        }
        if (LOG) {
            MtkLog.v(TAG, "maybeStartHiding() state=" + mState);
        }
    }

    private void startHiding() {
        if (mOverlayExt.canHidePanel()) {
            startHideAnimation(mBackground);
            startHideAnimation(mTimeBar);
            startHideAnimation(mPlayControlView);//add by qjz for NewUI 2013-03-25
            startHideAnimation(mLoopView);//add by qjz for NewUI 2013-03-25
            mScreenModeExt.onStartHiding();
            if (enableRewindAndForward) {
                mControllerRewindAndForwardExt.onStartHiding();
            }
        }
        //del by qjz for New UI 2013-03-14
        //startHideAnimation(mPlayPauseReplayView);
    }

    private void startHideAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.startAnimation(hideAnimation);
        }
    }

    private void cancelHiding() {
        handler.removeCallbacks(startHidingRunnable);
        if (mOverlayExt.canHidePanel()) {
            mBackground.setAnimation(null);
            mTimeBar.setAnimation(null);
            mLoopView.setAnimation(null);//add by qjz for NewUI 2013-03-25
            mPlayControlView.setAnimation(null);//add by qjz for NewUI 2013-03-25
            mScreenModeExt.onCancelHiding();
            if (enableRewindAndForward) {
                mControllerRewindAndForwardExt.onCancelHiding();
            }
        }
        //del by qjz for New UI 2013-03-14
        //mPlayPauseReplayView.setAnimation(null);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        hide();
    }

    public void onClick(View view) {
        if (LOG) {
            MtkLog.v(TAG, "onClick(" + view + ") listener=" + mListener
                    + ", state=" + mState + ", canReplay=" + mCanReplay);
        }
        if (mListener != null) {
            //del by qjz for New UI 2013-03-14
            /*if (view == mPlayPauseReplayView) {
                if (mState == State.ENDED) {
                    if (mCanReplay) {
                        mListener.onReplay();
                    }
                } else if (mState == State.PAUSED || mState == State.PLAYING) {
                    mListener.onPlayPause();
                    //set view disabled (play/pause asynchronous processing)
                    setViewEnabled(false);
                }
            } else */
            //del by qjz for NewVedioPlayerUI 201305011 begin
            /*if (view == mPlayControlView) {
                if (mState == State.ENDED) {
                    if (mCanReplay) {
                        mListener.onReplay();
                    }
                } else if (mState == State.PAUSED || mState == State.PLAYING) {
                    mListener.onPlayPause();
                    //set view disabled (play/pause asynchronous processing)
                    setViewEnabled(false);
                }
            } else if (view == mLoopView) {
                mListener.onChangeLoopMode();
            }*/
            //del by qjz for NewVedioPlayerUI 201305011 begin
        } else {
            mScreenModeExt.onClick(view);
            if (enableRewindAndForward) {
                mControllerRewindAndForwardExt.onClick(view);
            }
        }
    }

  /*
   * set view enable
   * (non-Javadoc)
   * @see com.android.gallery3d.app.ControllerOverlay#setViewEnabled(boolean)
   */
  @Override
  public void setViewEnabled(boolean isEnabled) {
      if(mListener.onIsRTSP()){
          MtkLog.v(TAG, "setViewEnabled is " + isEnabled);
          mOverlayExt.setCanScrubbing(isEnabled);
          //del by qjz for New UI 2013-03-14
          //mPlayPauseReplayView.setEnabled(isEnabled);
          mPlayControlView.setEnabled(isEnabled);

          if(enableRewindAndForward){
              mControllerRewindAndForwardExt.setViewEnabled(isEnabled);
          }
      }
  }
  
  /*
   * set play pause button from disable to normal
   * (non-Javadoc)
   * @see com.android.gallery3d.app.ControllerOverlay#setViewEnabled(boolean)
   */
  @Override
  public void setPlayPauseReplayResume(){
      if (mListener.onIsRTSP()) {
          MtkLog.v(TAG, "setPlayPauseReplayResume is enabled is true");
          //del by qjz for New UI 2013-03-14
          //mPlayPauseReplayView.setEnabled(true);
      }
  }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (hidden) {
            show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (super.onTouchEvent(event)) {
            return true;
        }

        if (hidden) {
            show();
            return true;
        }

        if (null != mAllGesture) {
            mAllGesture.onTouchEvent(event);
        }

/*        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            cancelHiding();
            // you can click play or pause when view is resumed
            // play/pause asynchronous processing
        if ((mState == State.PLAYING || mState == State.PAUSED) && mOverlayExt.mEnableScrubbing 
                && event.getY() < (mHeight - mTimeBar.getPreferredHeight())) {
            mListener.onPlayPause();
            }
            break;
        case MotionEvent.ACTION_UP:
            maybeStartHiding();
            break;
        }*/
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = ((MovieActivity)mContext).getWindowManager().getDefaultDisplay().getWidth();
        Rect insets = mWindowInsets;
        int pl = insets.left; // the left paddings
        int pr = insets.right;
        int pt = insets.top;
        int pb = insets.bottom;

        int h = bottom - top;
        int w = right - left;
        mWidth = w;
        mHeight = h;
        if (w > h) {//add by qjz for NewUI 2013-03-20
            mIsLand = true;
        } else {
            mIsLand = false;
        }
        boolean error = mErrorView.getVisibility() == View.VISIBLE;

        int y = h - pb;
        //PR597079-Li-Zhao begin
        int background_marginTop = (int)getResources().getDimension(R.dimen.background_detamargintop);
        int timerbar_marginTop = (int)getResources().getDimension(R.dimen.timebar_detamargintop);
        /*
        * PR 620961 gangchen@tcl.com change screenmode_marginTop.
        * the former is (int)getResources().getDimension(R.dimen.screenmode_margintop)
        */
        int screenmode_marginTop = (int)getResources().getDimension(R.dimen.screenmode_margintop)-(int)(10*scale+0.5f);
        // Put both TimeBar and Background just above the bottom system
        // component.
        // But extend the background to the width of the screen, since we don't
        // care if it will be covered by a system component and it looks better.

        // Needed, otherwise the framework will not re-layout in case only the
        // padding is changed
        if(enableRewindAndForward){
            mBackground.layout(0, y - mTimeBar.getPreferredHeight() - 80, w, y);
            mTimeBar.layout(pl, y - mTimeBar.getPreferredHeight() - 80, w - pr, y - mTimeBar.getBarHeight());
            mControllerRewindAndForwardExt.onLayout(0, width, y);
        } else {
            mBackground.layout(0, y - mTimeBar.getBarHeight()-background_marginTop, w, y);
            mTimeBar.layout(pl, y - mTimeBar.getPreferredHeight()-timerbar_marginTop, w - pr /*- mScreenModeExt.getAddedRightPadding()*/, y + 12);//PR680349-taoli-001
        }

        mScreenModeExt.onLayout(w, pr, y+screenmode_marginTop);
        //PR597079-Li-Zhao end
        // Put the play/pause/next/ previous button in the center of the screen
        //del by qjz for New UI 2013-03-14
        //layoutCenteredView(mPlayPauseReplayView, 0, 0, w, h);
        /*int sw =getSourcePicWidth(R.drawable.full_screen);
        setLayout(mLoopView,sw,w,w-sw,y);
        setLayout(mPlayControlView,sw,w,(w-sw)/2,y);*/
        if (mMainView != null) {
            layoutCenteredView(mMainView, 0, 0, w, h);
        }
    }
    //add by qjz for NewUI 2013-03-15 begin
    public boolean getIsLand() {
        return mIsLand;
    }

        //add by junliang.liu.hz for CR604778 begin
        public void showGestureDialog(String aWhich) {
           if (null != mFBNotifyView) {
               String text = null;
               if ("Forward".equals(aWhich)) {
                   text = getResources().getString(R.string.text_gesture_forward);
               }
               else {
                   text = getResources().getString(R.string.text_gesture_backward);
               }
               mFBNotifyView.setBackgroundResource(R.drawable.gesture_fb_bg);
               mFBNotifyView.setText(text);
               int pointX = 0;
               int pointY = 0;
               int width = 0;
               int height = 0;
                   width = (int)(GESTURE_FB_DLG_WIDTH * 1.5);
                   height = (int)(GESTURE_FB_DLG_HEIGHT * 1.5);
                   pointX = (int)(mWidth - width)/2;
                   pointY = (int)(mHeight - height)/2;
               mFBNotifyView.layout(pointX, pointY, pointX + width, pointY + height);
               mFBNotifyView.setVisibility(View.VISIBLE);
            }
        }
        public void hideGestureDialog() {
           if (null != mFBNotifyView) {
               mFBNotifyView.setVisibility(View.GONE);
           }
        }
        //add by junliang.liu.hz for CR604778 end

    //modified by qjz for NewVedioPlayerUI 20130405 begin
    public void setShowSeekToBackGround(Bitmap bitmap,int total,int seek,int currentPosition) {
        if (bitmap == null) {
            return;
        }
        //add by qjz for NewVedioPlayerUI 201305011 begin
        if (seek>1200000) {
            seek = seek/1000;
            total = total/1000;
        }
        //add by qjz for NewVedioPlayerUI 201305011 end
      //PR804309 modified by junliang.liu.hz at 2014.10.13 begin
        int position = (int)((mWidth - SEEKTO_GROUND_WIDTH_PX)*seek/total);
        mShowSeekTo.layout(position, mHeight - SEEKTO_GROUND_PADDING_BOTTOM - SEEKTO_GROUND_HEIGHT_PX, 
            		position + SEEKTO_GROUND_WIDTH_PX, mHeight-SEEKTO_GROUND_PADDING_BOTTOM);
      //PR804309 modified by junliang.liu.hz at 2014.10.13 end
        mShowSeekTo.setImageBitmap(bitmap);
        if (mIsShow) {
            mIsShow = false;
            mShowSeekTo.setVisibility(View.VISIBLE);
        }
    }
    /*public void setShowSeekToBackGround(Bitmap []bitmap,int total,int seek,int currentPosition) {
        int position = (int)(mWidth-160)*seek/total;
        if (bitmap == null) {
            return;
        }
        if (mIsLand) {
            if (mPreCurrentPosition != currentPosition) {
                mPreCurrentPosition = currentPosition;
                int x=0;
                for (int i=0; i <9; i++) {
                    if (i!=mPreCurrentPosition) {
                        mSeekToView[i].layout(x, mHeight-190, 100+x, mHeight-110);
                        x+=100;
                    } else {
                        mSeekToView[i].layout(x, mHeight-230, 160+x, mHeight-110);
                        x+=160;
                    }
                }
            }
            for (int i=0;i<9;i++) {
                if (bitmap[i] != null) {
                    mSeekToView[i].setImageBitmap(bitmap[i]);
                }
            }
            if (mIsShow) {
                mIsShow = false;
                for (int i=0;i<9;i++) {
                    mSeekToView[i].setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (mPreCurrentPosition != currentPosition) {
                mPreCurrentPosition = currentPosition;
                int x=0;
                for (int i=2; i<7; i++) {
                    if (i!=mPreCurrentPosition) {
                        mSeekToView[i].layout(x, mHeight-190, 100+x, mHeight-110);
                        x+=100;
                    } else {
                        mSeekToView[i].layout(x, mHeight-230, 140+x, mHeight-110);
                        x+=140;
                    }
                }
            }

            for (int i=2;i<7;i++) {
                if (bitmap[i] != null) {
                    mSeekToView[i].setImageBitmap(bitmap[i]);
                }
            }
            if (mIsShow) {
                mIsShow = false;
                for (int i=2;i<7;i++) {
                    mSeekToView[i].setVisibility(View.VISIBLE);
                }
            }
        }
    }*/
    //modified by qjz for NewVedioPlayerUI 20130405 end
    public int getSourcePicWidth(int id) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        Bitmap screenButton = BitmapFactory.decodeResource(mContext.getResources(), id);
        int mLoopWidth = screenButton.getWidth();
        int mLoopPadding = (int) (metrics.density * MARGIN);
        screenButton.recycle();
        // layout screen view position
        return mLoopPadding * 2 + mLoopWidth;
    }

    public void setLayout(View view,int sw,int width, int paddingRight, int yPosition) {
        view.layout(width - paddingRight - sw, yPosition
                - mTimeBar.getPreferredHeight(), width - paddingRight,
                yPosition);
    }
    //add by qjz for NewUI 2013-03-18 end

    protected void updateViews() {
        if (hidden) {
            return;
        }
        mBackground.setVisibility(View.VISIBLE);
        mTimeBar.setVisibility(View.VISIBLE);
        //modified by qjz for New UI 2013-03-14
        mPlayControlView.setVisibility(View.VISIBLE);
        mLoopView.setVisibility(View.VISIBLE);
        /*mPlayPauseReplayView.setImageResource(
                mState == State.PAUSED ? R.drawable.videoplayer_play :
                    mState == State.PLAYING ? R.drawable.videoplayer_pause : 
                        R.drawable.videoplayer_reload);*/
        mScreenModeExt.onShow();
        if (enableRewindAndForward) {
            mControllerRewindAndForwardExt.onShow();
        }
        if (!mOverlayExt.handleUpdateViews()) {
            //del by qjz for New UI 2013-03-14
            /*mPlayPauseReplayView.setVisibility(
                    (mState != State.LOADING && mState != State.ERROR &&
                    !(mState == State.ENDED && !mCanReplay))
                    ? View.VISIBLE : View.GONE);*/
        }
        requestLayout();
        if (LOG) {
            MtkLog.v(TAG, "updateViews() state=" + mState + ", canReplay="
                    + mCanReplay);
        }
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
        cancelHiding();
        super.onScrubbingStart();
    }

    @Override
    public void onScrubbingMove(int time) {
        cancelHiding();
        super.onScrubbingMove(time);
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        maybeStartHiding();
        super.onScrubbingEnd(time, trimStartTime, trimEndTime);
    }

    @Override 
    public void onShowSeekToView(int seek) {
        mListener.onShowSeekToView(seek);
    }

    @Override 
    public void onSetSeekViewShow(boolean isShow) {
        if (isShow) {
            mIsShow = true;
            mPreCurrentPosition = -1;
        } else {
            mIsShow = false;
            //modified by qjz for NewVedioPlayerUI 20130405 begin
            mShowSeekTo.setVisibility(View.GONE);
            mShowSeekTo.setImageBitmap(null);
            /*for (int i=0;i<9;i++) {
                mSeekToView[i].setVisibility(View.INVISIBLE);
                mSeekToView[i].setImageBitmap(null);
            }*/
            //modified by qjz for NewVedioPlayerUI 20130405 end
        }
    }
    // / M: mtk extension for overlay @{
    private static final String TAG = "Gallery3D/MovieControllerOverlay";
    private static final boolean LOG = true;

    private ScreenModeManager mScreenModeManager;

    public void setScreenModeManager(ScreenModeManager manager) {
        mScreenModeManager = manager;
        if (mScreenModeManager != null) {
            mScreenModeManager.addListener(mScreenModeExt);
        }
        if (LOG) {
            MtkLog.v(TAG, "setScreenModeManager(" + manager + ")");
        }
    }

    public IContrllerOverlayExt getOverlayExt() {
        return mOverlayExt;
    }

    public IControllerRewindAndForward getControllerRewindAndForwardExt() {
        if (enableRewindAndForward) {
            return mControllerRewindAndForwardExt;
        }
        return null;
    }

    private ScreenModeExt mScreenModeExt = new ScreenModeExt();
    private ControllerRewindAndForwardExt mControllerRewindAndForwardExt = new ControllerRewindAndForwardExt();
    private OverlayExtension mOverlayExt = new OverlayExtension();

    private class OverlayExtension implements IContrllerOverlayExt {
        private State mLastState;
        private String mPlayingInfo;

        @Override
        public void showBuffering(boolean fullBuffer, int percent) {
            if (LOG) {
                MtkLog.v(TAG, "showBuffering(" + fullBuffer + ", " + percent
                        + ") " + "lastState=" + mLastState + ", state=" + mState);
            }
            if (fullBuffer) {
                // do not show text and loading
                mTimeBar.setSecondaryProgress(percent);
                return;
            }
            if (mState == State.PAUSED || mState == State.PLAYING) {
                mLastState = mState;
            }
            if (percent >= 0 && percent < 100) { // valid value
                mState = State.BUFFERING;
                int msgId = com.mediatek.R.string.media_controller_buffering;
                String text = String.format(getResources().getString(msgId),
                        percent);
                mInfoText.setText(text);// add by yaping.liu for pr497232
                mTimeBar.setInfo(text);
                showMainView(mLoadingView);
            } else if (percent == 100) {
                mState = mLastState;
                mTimeBar.setInfo(null);
                //modified by qjz for PR461095 20130603
                showMainView(null);// restore play pause state 
            } else { // here to restore old state
                mState = mLastState;
                mTimeBar.setInfo(null);
            }
        }

        // set buffer percent to unknown value

        public void clearBuffering() {
            if (LOG) {
                MtkLog.v(TAG, "clearBuffering()");
            }
            mTimeBar.setSecondaryProgress(TimeBar.UNKNOWN);
            showBuffering(false, TimeBar.UNKNOWN);
        }

        public void showReconnecting(int times) {
            clearBuffering();
            mState = State.RETRY_CONNECTING;
            int msgId = R.string.VideoView_error_text_cannot_connect_retry;
            String text = getResources().getString(msgId, times);
            mTimeBar.setInfo(text);
            mInfoText.setText(text);// add by yaping.liu for pr511439
            showMainView(mLoadingView);
            if (LOG) {
                MtkLog.v(TAG, "showReconnecting(" + times + ")");
            }
        }

        public void showReconnectingError() {
            clearBuffering();
            mState = State.RETRY_CONNECTING_ERROR;
            int msgId = com.mediatek.R.string.VideoView_error_text_cannot_connect_to_server;
            String text = getResources().getString(msgId);
            mTimeBar.setInfo(text);
            //modify start by yaping.liu for pr511439
            mErrorView.setText(text);
            showMainView(mErrorView);
            //modified by qjz for PR461095 20130603
//            showMainView(null);
            // modify end by yaping.liu for pr511439
            if (LOG) {
                MtkLog.v(TAG, "showReconnectingError()");
            }
        }

        public void setPlayingInfo(boolean liveStreaming) {
            int msgId;
            if (liveStreaming) {
                msgId = com.mediatek.R.string.media_controller_live;
            } else {
                msgId = com.mediatek.R.string.media_controller_playing;
            }
            mPlayingInfo = getResources().getString(msgId);
            if (LOG) {
                MtkLog.v(TAG, "setPlayingInfo(" + liveStreaming
                        + ") playingInfo=" + mPlayingInfo);
            }
        }

        // for pause feature
        private boolean mCanPause = true;
        private boolean mEnableScrubbing = false;

        public void setCanPause(boolean canPause) {
            this.mCanPause = canPause;
            if (LOG) {
                MtkLog.v(TAG, "setCanPause(" + canPause + ")");
            }
        }

        public void setCanScrubbing(boolean enable) {
            mEnableScrubbing = enable;
            mTimeBar.setScrubbing(enable);
            if (LOG) {
                MtkLog.v(TAG, "setCanScrubbing(" + enable + ")");
            }
        }

        // for only audio feature
        private boolean mAlwaysShowBottom;

        public void setBottomPanel(boolean alwaysShow, boolean foreShow) {
            mAlwaysShowBottom = alwaysShow;
            if (!alwaysShow) { // clear background
                setBackgroundDrawable(null);
                setBackgroundColor(Color.TRANSPARENT);
            } else {
                setBackgroundResource(R.drawable.media_default_bkg);
                if (foreShow) {
                    setVisibility(View.VISIBLE);
                    // show();//show the panel
                    // hide();//hide it for jelly bean doesn't show control when
                    // enter the video.
                }
            }
            if (LOG) {
                MtkLog.v(TAG, "setBottomPanel(" + alwaysShow + ", " + foreShow
                        + ")");
            }
        }

        public boolean isPlayingEnd() {
            if (LOG) {
                MtkLog.v(TAG, "isPlayingEnd() state=" + mState);
            }
            boolean end = false;
            if (State.ENDED == mState || State.ERROR == mState
                    || State.RETRY_CONNECTING_ERROR == mState) {
                end = true;
            }
            return end;
        }

        public boolean handleShowPlaying() {
            if (mState == State.BUFFERING) {
                mLastState = State.PLAYING;
                return true;
            }
            return false;
        }

        public boolean handleShowPaused() {
            mTimeBar.setInfo(null);
            if (mState == State.BUFFERING) {
                mLastState = State.PAUSED;
                return true;
            }
            return false;
        }

        public void onShowLoading() {
            int msgId = com.mediatek.R.string.media_controller_connecting;
            String text = getResources().getString(msgId);
            mTimeBar.setInfo(text);
        }

        public void onShowEnded() {
            clearBuffering();
            mTimeBar.setInfo(null);
        }

        public void onShowErrorMessage(String message) {
            clearBuffering();
        }

        public boolean handleUpdateViews() {
            //del by qjz for New UI 2013-03-14
            /*mPlayPauseReplayView
                    .setVisibility((mState != State.LOADING
                            && mState != State.ERROR
                            &&
                            // !(state == State.ENDED && !canReplay) && //show
                            // end when user stopped it.
                            mState != State.BUFFERING
                            && mState != State.RETRY_CONNECTING && !(mState != State.ENDED
                            && mState != State.RETRY_CONNECTING_ERROR && !mCanPause))
                    // for live streaming
                    ? View.VISIBLE
                            : View.GONE);*/

            if (mPlayingInfo != null && mState == State.PLAYING) {
                mTimeBar.setInfo(mPlayingInfo);
            }
            return true;
        }

        public boolean handleHide() {
            return mAlwaysShowBottom;
        }

        public void onShowMainView(View view) {
            if (LOG) {
                /*MtkLog.v(TAG, "showMainView(" + view + ") errorView="
                        + mErrorView + ", loadingView=" + mLoadingView
                        + ", playPauseReplayView=" + mPlayPauseReplayView);*/
                MtkLog.v(TAG, "showMainView() enableScrubbing="
                        + mEnableScrubbing + ", state=" + mState);
            }
            if (mEnableScrubbing
                    && (mState == State.PAUSED || mState == State.PLAYING)) {
                mTimeBar.setScrubbing(true);
            } else {
                mTimeBar.setScrubbing(false);
            }
        }

        public boolean canHidePanel() {
            return !mAlwaysShowBottom;
        }
    };

    class ScreenModeExt implements View.OnClickListener, ScreenModeListener {
        // for screen mode feature
        private ImageView mScreenView;
        private int mScreenPadding;
        private int mScreenWidth;

        private static final int MARGIN = 10; // dip
        private ViewGroup mParent;
        private ImageView mSeprator;

        void init(Context context, View myTimeBar) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int padding = (int) (metrics.density * MARGIN);
            myTimeBar.setPadding(padding, 0, padding, 0);
            
            LayoutParams wrapContent =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            Intent carmodeintent = ((Activity) context).getIntent();//PR501083-kuiwang-001
            //add by qjz for NewVedioPlayerUI 201305011 begin
            mLoopView = new ImageView(context);
            mLoopView.setScaleType(ScaleType.CENTER);
            mLoopView.setFocusable(true);
            mLoopView.setClickable(true);
            mLoopView.setOnClickListener(this);
            addView(mLoopView, wrapContent);

            mPlayControlView = new ImageView(context);
            mPlayControlView.setScaleType(ScaleType.CENTER);
            mPlayControlView.setFocusable(true);
            mPlayControlView.setClickable(true);
            mPlayControlView.setOnClickListener(this);
            addView(mPlayControlView, wrapContent);

            mShowSeekTo = new BorderImageView(context);
            mShowSeekTo.setScaleType(ScaleType.CENTER);
            mShowSeekTo.setFocusable(true);
            addView(mShowSeekTo, wrapContent);
            //add by qjz for NewVedioPlayerUI 201305011 end
            
            //add screenView
            mScreenView = new ImageView(context);
            mScreenView.setImageResource(R.drawable.ic_media_bigscreen);//default next screen mode
            mScreenView.setScaleType(ScaleType.CENTER);
            mScreenView.setFocusable(true);
            mScreenView.setClickable(true);
            mScreenView.setOnClickListener(this);
            addView(mScreenView, wrapContent);

            // add by junliang.liu.hz for CR604778 begin
            int width = 0;
            int height = 0;

            width = (int) (GESTURE_FB_DLG_WIDTH * 1.5);
            height = (int) (GESTURE_FB_DLG_HEIGHT * 1.5);

            mFBNotifyView = new TextView(context);
            mFBNotifyView.setTextColor(Color.WHITE);
            mFBNotifyView.setTextSize(18);
            mFBNotifyView.setGravity(Gravity.CENTER);
            mFBNotifyView.setFocusable(true);
            addView(mFBNotifyView, new LayoutParams(width, height));
            // add by junliang.liu.hz for CR604778 end


            //PR501083-kuiwang-001 begin
            //we don't need the two imageview when playing recorder with carmode
            if(carmodeintent.getBooleanExtra(MovieActivity.KEY_CAR_MODE_FLAG, false)){
                removeView(mLoopView);
                removeView(mScreenView);
              }
            //PR501083-kuiwang-001 end
            if(enableRewindAndForward){
                MtkLog.v(TAG, "ScreenModeExt enableRewindAndForward");
                mSeprator = new ImageView(context);
                mSeprator.setImageResource(R.drawable.ic_separator_line);//default next screen mode
                mSeprator.setScaleType(ScaleType.CENTER);
                mSeprator.setFocusable(true);
                mSeprator.setClickable(true);
                mSeprator.setOnClickListener(this);
                addView(mSeprator, wrapContent);
                
            } else {
                MtkLog.v(TAG, "ScreenModeExt unenableRewindAndForward");
            }
            
            //for screen layout
            Bitmap screenButton = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_media_bigscreen);
            mScreenWidth = screenButton.getWidth();
            mScreenPadding = (int) (metrics.density * MARGIN);
            screenButton.recycle();
        }

        private void updateScreenModeDrawable() {
            int screenMode = mScreenModeManager.getNextScreenMode();
            if (screenMode == ScreenModeManager.SCREENMODE_BIGSCREEN) {
                mScreenView.setImageResource(R.drawable.vedio_bigscreen);//modified by qjz for NewVedioPlayerUI 20130405 
            } else if (screenMode == ScreenModeManager.SCREENMODE_FULLSCREEN) {
                mScreenView.setImageResource(R.drawable.vedio_fullscreen);//modified by qjz for NewVedioPlayerUI 20130405 
            } else {
                mScreenView.setImageResource(R.drawable.vedio_cropscreen);//modified by qjz for NewVedioPlayerUI 20130405 
            }
        }

        @Override
        public void onClick(View v) {
            if (v == mScreenView && mScreenModeManager != null) {
                mScreenModeManager.setScreenMode(mScreenModeManager
                        .getNextScreenMode());
                show();// show it?
            //add by qjz for NewVedioPlayerUI 201305011 begin
            } else if (v == mLoopView) {
                mListener.onChangeLoopMode();
                maybeStartHiding();//add by qjz for PR461961 20130603
            } else if (v == mPlayControlView) {
                if (mState == State.ENDED) {
                    // PR466487 ming.zhang modify begin
                    // if (mCanReplay) {
                    mListener.onReplay();
                    // }
                    // PR466487 ming.zhang modify end
                } else if (mState == State.PAUSED || mState == State.PLAYING) {
                    mListener.onPlayPause();
                    setViewEnabled(false);
                }
                maybeStartHiding();//add by qjz for PR461961 20130603
            }
            //add by qjz for NewVedioPlayerUI 201305011 end
        }

        public void onStartHiding() {
            startHideAnimation(mScreenView);
        }

        public void onCancelHiding() {
            mScreenView.setAnimation(null);
        }

        public void onHide() {
            mScreenView.setVisibility(View.INVISIBLE);
            if(enableRewindAndForward){
                mSeprator.setVisibility(View.INVISIBLE);
            }
        }
        public void onShow() {
            mScreenView.setVisibility(View.VISIBLE);
            if(enableRewindAndForward){
                mSeprator.setVisibility(View.VISIBLE);
            }
        }
        public void onLayout(int width, int paddingRight, int yPosition) {
            // layout screen view position
            int sw = getAddedRightPadding();
            //FR450344-yanlong.li@tcl.com-begin
            if (popupVideo) {
                // reset position of mScreenModeExt
                mLoopView.layout(width - paddingRight - sw,
                        yPosition - mTimeBar.getPreferredHeight(), width - paddingRight, yPosition);
                mScreenView.layout(paddingRight, yPosition - mTimeBar.getPreferredHeight(),
                        paddingRight + sw, yPosition);

            }else{
              //FR450344-yanlong.li@tcl.com-end
            mScreenView.layout(width - paddingRight - sw, yPosition
                    - mTimeBar.getPreferredHeight(), width - paddingRight,
                    yPosition);

            //add by qjz for NewVedioPlayerUI 201305011 begin
            mLoopView.layout(paddingRight, yPosition
                    - mTimeBar.getPreferredHeight(), paddingRight + sw,
                    yPosition);
            }
            mPlayControlView.layout((width - sw)/2, yPosition
                    - mTimeBar.getPreferredHeight(), (width + sw)/2,
                    yPosition);
            //add by qjz for NewVedioPlayerUI 201305011 end
            
            if(enableRewindAndForward){
                mSeprator.layout(width - paddingRight - sw - 22 , yPosition
                        - mTimeBar.getPreferredHeight(), width - paddingRight - sw - 20,
                        yPosition);
            }
            // modified by junliang.liu.hz for CR604778 begin
            int pointX = 0;
            int pointY = 0;
            int width2 = 0;
            int height2 = 0;
            width2 = (int) (GESTURE_FB_DLG_WIDTH * 1.5);
            height2 = (int) (GESTURE_FB_DLG_HEIGHT * 1.5);
            pointX = (int) (mWidth - width2) / 2;
            pointY = (int) (mHeight - height2) / 2;
            mFBNotifyView.layout(pointX, pointY, pointX + width2, pointY
                    + height2);
            // modified by junliang.liu.hz for CR604778 end
        }

        public int getAddedRightPadding() {
            return mScreenPadding * 2 + mScreenWidth;
        }

        @Override
        public void onScreenModeChanged(int newMode) {
            updateScreenModeDrawable();
        }
    }

    // / @}

    class ControllerRewindAndForwardExt implements View.OnClickListener, IControllerRewindAndForward{
        //for screen mode feature
        private LinearLayout mContollerButtons;
        private ImageView mStop;
        private ImageView mForward;
        private ImageView mRewind;
        private IRewindAndForwardListener mListenerForRewind;
        private int mButtonWidth;
        private static final int BUTTON_PADDING = 40;
        private int mTimeBarHeight = 0;
        
        void init(Context context) {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt init");
            mTimeBarHeight = mTimeBar.getPreferredHeight();
            Bitmap button = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_menu_forward);
            mButtonWidth = button.getWidth();
            button.recycle();
            
            mContollerButtons = new LinearLayout(context);
            LinearLayout.LayoutParams wrapContent = new LinearLayout.LayoutParams(
                    getAddedRightPadding(), mTimeBarHeight);
            mContollerButtons.setHorizontalGravity(LinearLayout.HORIZONTAL);
            mContollerButtons.setVisibility(View.VISIBLE);
            mContollerButtons.setGravity(Gravity.CENTER);
            
            LinearLayout.LayoutParams buttonParam = new LinearLayout.LayoutParams(
                    mTimeBarHeight, mTimeBarHeight);
            mRewind = new ImageView(context);
            mRewind.setImageResource(R.drawable.icn_media_rewind);
            mRewind.setScaleType(ScaleType.CENTER);
            mRewind.setFocusable(true);
            mRewind.setClickable(true);
            mRewind.setOnClickListener(this);
            mContollerButtons.addView(mRewind, buttonParam);
            
            mStop = new ImageView(context);
            mStop.setImageResource(R.drawable.icn_media_stop);
            mStop.setScaleType(ScaleType.CENTER);
            mStop.setFocusable(true);
            mStop.setClickable(true);
            mStop.setOnClickListener(this);
            LinearLayout.LayoutParams stopLayoutParam = new LinearLayout.LayoutParams(
                    mTimeBarHeight, mTimeBarHeight);
            stopLayoutParam.setMargins(BUTTON_PADDING, 0, BUTTON_PADDING, 0);
            mContollerButtons.addView(mStop, stopLayoutParam);
            
            mForward = new ImageView(context);
            mForward.setImageResource(R.drawable.icn_media_forward);
            mForward.setScaleType(ScaleType.CENTER);
            mForward.setFocusable(true);
            mForward.setClickable(true);
            mForward.setOnClickListener(this);
            mContollerButtons.addView(mForward, buttonParam);
            
            addView(mContollerButtons, wrapContent);
        }

        @Override
        public void onClick(View v) {
            if (v == mStop) {
                MtkLog.v(TAG, "ControllerRewindAndForwardExt onClick mStop");
                mListenerForRewind.onStopVideo();
            } else if (v == mRewind) {
                MtkLog.v(TAG, "ControllerRewindAndForwardExt onClick mRewind");
                mListenerForRewind.onRewind();
            } else if (v == mForward) {
                MtkLog.v(TAG, "ControllerRewindAndForwardExt onClick mForward");
                mListenerForRewind.onForward();
            }
        }

        public void onStartHiding() {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt onStartHiding");
            startHideAnimation(mContollerButtons);
        }

        public void onCancelHiding() {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt onCancelHiding");
            mContollerButtons.setAnimation(null);
        }

        public void onHide() {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt onHide");
            mContollerButtons.setVisibility(View.INVISIBLE);
        }

        public void onShow() {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt onShow");
            mContollerButtons.setVisibility(View.VISIBLE);
        }

        public void onLayout(int l, int r, int b) {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt onLayout");
            int cl = (r - l - getAddedRightPadding()) / 2;
            int cr = cl + getAddedRightPadding();
            mContollerButtons.layout(cl, b - mTimeBar.getPreferredHeight(), cr, b);
        }
        
        public int getAddedRightPadding() {
            return mTimeBarHeight * 3 + BUTTON_PADDING * 2; 
        }
        @Override
        public void setIListener(IRewindAndForwardListener listener){
            MtkLog.v(TAG, "ControllerRewindAndForwardExt setIListener " + listener);
            mListenerForRewind = listener;
        }
        
        @Override
        public void showControllerButtonsView(boolean canStop, boolean canRewind, boolean canForward){
            MtkLog.v(TAG, "ControllerRewindAndForwardExt showControllerButtonsView " + canStop + canRewind + canForward);
            // show ui
            mStop.setEnabled(canStop);
            mRewind.setEnabled(canRewind);
            mForward.setEnabled(canForward);
        }

        @Override
        public void setListener(Listener listener) {
            setListener(listener);
        }
        @Override
        public boolean getPlayPauseEanbled() {
            //del by qjz for New UI 2013-03-14
            //return mPlayPauseReplayView.isEnabled();
            return false;
        }
        
        @Override
        public boolean getTimeBarEanbled() {
            return mTimeBar.getScrubbing();
        }

        @Override
        public void setCanReplay(boolean canReplay) {
            setCanReplay(canReplay);
        }

        @Override
        public View getView() {
            return mContollerButtons;
        }

        @Override
        public void show() {
            show();
        }

        @Override
        public void showPlaying() {
            showPlaying();
        }

        @Override
        public void showPaused() {
            showPaused();
        }

        @Override
        public void showEnded() {
            showEnded();
        }

        @Override
        public void showLoading() {
            showLoading();
        }

        @Override
        public void showErrorMessage(String message) {
            showErrorMessage(message);
        }

        public void setTimes(int currentTime, int totalTime ,int trimStartTime, int trimEndTime) {
            setTimes(currentTime, totalTime,0,0);
        }

        @Override
        public void setPlayPauseReplayResume() {
        }

        @Override
        public void setViewEnabled(boolean isEnabled) {
            // TODO Auto-generated method stub
            MtkLog.v(TAG, "ControllerRewindAndForwardExt setViewEnabled is " + isEnabled);
            mRewind.setEnabled(isEnabled);
            mForward.setEnabled(isEnabled);
        }
    }
    /// @}

        //modified by junliang.liu.hz for CR604778 begin
       @Override
       public boolean onDown(MotionEvent e) {
           cancelHiding();
           return false;
       }
       //modified by bing.wang.hz for FR539439 and 538423 begin
       @Override
       public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
               float velocityY) {
           cancelHiding();
            if (mOverlayExt.mEnableScrubbing && e1.getY() < (mHeight - mTimeBar.getPreferredHeight())
                   && e2.getY() < (mHeight - mTimeBar.getPreferredHeight())) {
               if (e1.getX() - e2.getX() > 30) {
                   mListener.onSeekBackward();
                   maybeStartHiding();
                   return true;
               } else if (e2.getX() - e1.getX() > 30) {
                   mListener.onSeekForward();
                   maybeStartHiding();
                   return true;
               }
           }
           maybeStartHiding();
           return false;
       }
       //modified by bing.wang.hz for FR539439 and 538423 end
       @Override
       public void onLongPress(MotionEvent e) {
           // TODO Auto-generated method stub
       }
       @Override
       public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
               float distanceY) {
           // TODO Auto-generated method stub
           return false;
       }
       @Override
       public void onShowPress(MotionEvent e) {
           // TODO Auto-generated method stub
       }
       @Override
       public boolean onSingleTapUp(MotionEvent e) {
           cancelHiding();
            // you can click play or pause when view is resumed
            // play/pause asynchronous processing
           if ((mState == State.PLAYING || mState == State.PAUSED) && mOverlayExt.mEnableScrubbing
                && e.getY() < (mHeight - mTimeBar.getPreferredHeight())) {
               mListener.onPlayPause();
            }
           maybeStartHiding();
           return true;
       }
       //modified by junliang.liu.hz for CR604778 end
}
