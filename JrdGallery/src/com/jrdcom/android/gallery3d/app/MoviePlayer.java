/*
 * Copyright (C) 2009 The Android Open Source Project
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
/************************************************************************************************************/
/*                                                                                            Date : 07/2013*/
/*                                   PRESENTATION                                                           */
/*                     Copyright (c) 2012 JRD Communications, Inc.                                          */
/************************************************************************************************************/
/*                                                                                                          */
/*           This material is company confidential, cannot be reproduced in any                             */
/*           form without the written permission of JRD Communications, Inc.                                */
/*                                                                                                          */
/*==========================================================================================================*/
/*   Author :                                                                                               */
/*   Role :                                                                                                 */
/*   Reference documents :                                                                                  */
/*==========================================================================================================*/
/* Comments :                                                                                               */
/*     file    :../packages/apps/Gallery2/src/com/android/gallery3d/app/MoviePlayer.java                    */
/*     Labels  :                                                                                            */
/*==========================================================================================================*/
/* Modifications   (month/day/year)                                                                         */
/*==========================================================================================================*/
/* date    | author       |FeatureID                             |modification                              */
/*=========|==============|======================================|==========================================*/
/*         |              |                                      |                                          */
/*==========================================================================================================*/
/* Problems Report(PR/CR)                                                                                   */
/*==========================================================================================================*/
/* date    | author       | PR #                    |                                                       */
/*=========|==============|=========================|=======================================================*/
/*07/25/13 | Xiaobin Yang |PR493750-Xiaobin-Yang-001|Gallary force closeed when plat as loop                */
/*==========================================================================================================*/
package com.jrdcom.android.gallery3d.app;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.res.Resources.NotFoundException;//FR450344-yanlong.li@tcl.com

import com.jrdcom.android.gallery3d.ui.PopupWindowFrameLayout;//FR450344 yanlong.li@tcl.com
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmManagerClient;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.Metadata;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.common.ApiHelper;
import com.jrdcom.android.gallery3d.common.BlobCache;
import com.jrdcom.android.gallery3d.util.CacheManager;
import com.jrdcom.android.gallery3d.util.GalleryUtils;
import com.jrdcom.mediatek.gallery3d.ext.IContrllerOverlayExt;
import com.jrdcom.mediatek.gallery3d.ext.IMovieDrmExtension;
import com.jrdcom.mediatek.gallery3d.ext.IMovieDrmExtension.IMovieDrmCallback;
import com.jrdcom.mediatek.gallery3d.ext.IMovieItem;
import com.jrdcom.mediatek.gallery3d.ext.IMoviePlayer;
import com.jrdcom.mediatek.gallery3d.ext.MovieUtils;
import com.jrdcom.mediatek.gallery3d.ext.MtkLog;
import com.jrdcom.mediatek.gallery3d.video.BookmarkEnhance;
import com.jrdcom.mediatek.gallery3d.video.DetailDialog;
import com.jrdcom.mediatek.gallery3d.video.ExtensionHelper;
import com.jrdcom.mediatek.gallery3d.video.MTKVideoView;
import com.jrdcom.mediatek.gallery3d.video.ScreenModeManager;
import com.jrdcom.mediatek.gallery3d.video.StopVideoHooker;
import com.jrdcom.mediatek.gallery3d.video.ScreenModeManager.ScreenModeListener;
import com.jrdcom.mediatek.gallery3d.video.IControllerRewindAndForward;
import com.jrdcom.mediatek.gallery3d.video.IControllerRewindAndForward.IRewindAndForwardListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.graphics.BitmapFactory;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.DialogFragment;
import com.mediatek.gallery3d.video.ErrorDialogFragment;

public class MoviePlayer implements
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        ControllerOverlay.Listener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnSeekCompleteListener {
    @SuppressWarnings("unused")
    private static final String TAG = "MoviePlayer";

    private static final String KEY_VIDEO_POSITION = "video-position";
    private static final String KEY_RESUMEABLE_TIME = "resumeable-timeout";
    // These are constants in KeyEvent, appearing on API level 11.
    private static final int KEYCODE_MEDIA_PLAY = 126;
    private static final int KEYCODE_MEDIA_PAUSE = 127;

    // Copied from MediaPlaybackService in the Music Player app.
    private static final String SERVICECMD = "com.android.music.musicservicecommand";
    private static final String CMDNAME = "command";
    private static final String CMDPAUSE = "pause";

    private static final long BLACK_TIMEOUT = 500;
    // add by junliang.liu.hz for CR604778 begin
    private static final int MSG_SHOW_NOTIFY_DLG = 1;
    private static final int MSG_HIDE_NOTIFY_DLG = 2;
    private static final int MSG_DELAY_TIME_MILLIS = 3000;
    // add by junliang.liu.hz for CR604778 end

    // If we resume the acitivty with in RESUMEABLE_TIMEOUT, we will keep playing.
    // Otherwise, we pause the player.
    private static final long RESUMEABLE_TIMEOUT = 3 * 60 * 1000; // 3 mins

    private Context mContext;
    private final MTKVideoView mVideoView;
    private final View mRootView;
    private final Bookmarker mBookmarker;
    //private Uri mUri;
    private final Handler mHandler = new Handler();
    private final AudioBecomingNoisyReceiver mAudioBecomingNoisyReceiver;
    private final MovieControllerOverlay mController;

    private long mResumeableTime = Long.MAX_VALUE;
    private int mVideoPosition = 0;
    private boolean mHasPaused = false;
    private int mLastSystemUiVis = 0;

    // If the time bar is being dragged.
    private boolean mDragging;

    // If the time bar is visible.
    private boolean mShowing;

    //added by sima.chen for PR810665 begin
    private PlayPauseProcessExt mPlayPauseProcessExt = new PlayPauseProcessExt();
    private static final int ERROR_INVALID_OPERATION = -38;
    private static final int ERROR_ALREADY_EXISTS = -35;
    //added by sima.chen for PR810665 end

    //add by qjz 2013-03-15 begin
    private long mSeekTo;
    private final int MSG_UPDATE_SEEK_FRAME = 109;
    private static boolean isSeek = false;
    private Bitmap mBitmap;//modified by qjz for NewVedioPlayerUI 20130405 
    private int mSeekTime;//modified by qjz for NewVedioPlayerUI 20130405 
    //private Bitmap [] mBitmap= new Bitmap[9];//del by qjz for NewVedioPlayerUI 20130405 
    //add by qjz 2013-03-15 end
    //add by qjz for PR487297 begin
    private boolean mTreatUpAsBack = false;
    private boolean mLogo;
    public void setTreatUpAsBack(boolean isTreatUpAsBack, boolean logo) {
        mTreatUpAsBack = isTreatUpAsBack;
        mLogo = logo;
    }
    //add by qjz for PR487297 end

    private boolean mIsAttachment;//add by yaping.liu for pr506689
    private final Runnable mPlayingChecker = new Runnable() {
        @Override
        public void run() {
            boolean isplaying = mVideoView.isPlaying();
            if (LOG) {
                MtkLog.v(TAG, "mPlayingChecker.run() isplaying=" + isplaying);
            }
            if (isplaying) {
                mController.showPlaying();
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };

    // add by junliang.liu.hz for CR604778 begin
    private Handler mShowWardNotifyHD = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case MSG_SHOW_NOTIFY_DLG:
                String which = msg.arg1 == 0 ? "Forward" : "Backward";
                if (null != mController) {
                    mController.showGestureDialog(which);
                }
                mShowWardNotifyHD.removeMessages(MSG_HIDE_NOTIFY_DLG);
                Message backMsg = new Message();
                backMsg.what = MSG_HIDE_NOTIFY_DLG;
                mShowWardNotifyHD.sendMessageDelayed(backMsg,
                        MSG_DELAY_TIME_MILLIS);
                break;
            case MSG_HIDE_NOTIFY_DLG:
                if (null != mController) {
                    mController.hideGestureDialog();
                }
                break;

            default:
                break;
            }
            super.handleMessage(msg);
        }

    };
    // add by junliang.liu.hz for CR604778 end

    //add by qjz for NewUI 2013-03-19
    private Handler myHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch(msg.what) {
             case MSG_UPDATE_SEEK_FRAME:
                 mController.setShowSeekToBackGround(mBitmap,(int)(videoDuration/1000),msg.arg1,0/*getCurrentPosition(msg.arg1)*/);
                 break;
             default:
                 break;
             }
        }
    };
    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
        }
    };

    public MoviePlayer(View rootView, final MovieActivity movieActivity, IMovieItem info,
            Bundle savedInstance, boolean canReplay, boolean isAttachment) {
        mIsAttachment = isAttachment;// add by yaping.liu for pr506689
        mContext = movieActivity.getApplicationContext();
        mRootView = rootView;
        mVideoView = (MTKVideoView) rootView.findViewById(R.id.surface_view);
        mBookmarker = new Bookmarker(movieActivity);
        //mUri = videoUri;
         
        //FR 576703 add by xiangchen begin
        //PR493750-xiaobin-yang-001 begin
        //Invoke the private singleton method to create a singleton DrmManagerClient Object.
//        drmClient = this.createSingletonDrmClient(mContext);
        //PR493750-xiaobin-yang-001 end
        //FR 576703 add by xiangchen end

        mController = new MovieControllerOverlay(movieActivity, mIsAttachment);// modify by yaping.liu for pr506689
        ((ViewGroup)rootView).addView(mController.getView());
        mController.setListener(this);
        mController.setCanReplay(canReplay);
        
        init(movieActivity, info, canReplay);
        
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        //we move this behavior to startVideo()
        //mVideoView.setVideoURI(mUri, null, !mWaitMetaData);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mController.show();
                return true;
            }
        });

        // The SurfaceView is transparent before drawing the first frame.
        // This makes the UI flashing when open a video. (black -> old screen
        // -> video) However, we have no way to know the timing of the first
        // frame. So, we hide the VideoView for a while to make sure the
        // video has been drawn on it.
        mVideoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVideoView.setVisibility(View.VISIBLE);
            }
        }, BLACK_TIMEOUT);
        
        setOnSystemUiVisibilityChangeListener();
        // Hide system UI by default
        showSystemUi(false);

        mAudioBecomingNoisyReceiver = new AudioBecomingNoisyReceiver();
        mAudioBecomingNoisyReceiver.register();

        if (savedInstance != null) { // this is a resumed activity
            mVideoPosition = savedInstance.getInt(KEY_VIDEO_POSITION, 0);
            mResumeableTime = savedInstance.getLong(KEY_RESUMEABLE_TIME, Long.MAX_VALUE);
            onRestoreInstanceState(savedInstance);
            mHasPaused = true;
        } else {
            mTState = TState.PLAYING;
            mFirstBePlayed = true;
            final BookmarkerInfo bookmark = mBookmarker.getBookmark(mMovieItem.getUri());
            if (bookmark != null) {
                showResumeDialog(movieActivity, bookmark);
            } else {
                startVideoCareDrm();
            }
        }
        mScreenModeExt.setScreenMode();
    }
   //FR450344-add by yanlong.li@tcl.com 
    public MoviePlayer(View rootView, final MovieActivity movieActivity, IMovieItem info,
            Bundle savedInstance, boolean canReplay, int position, boolean isAttachment) {
        mIsAttachment = isAttachment;// add by yaping.liu for pr506689
        mContext = movieActivity.getApplicationContext();
        mRootView = rootView;
        mVideoView = (MTKVideoView) rootView.findViewById(R.id.surface_view);
        mBookmarker = new Bookmarker(movieActivity);
        //mUri = videoUri;
        
        //FR 576703 add by xiangchen begin
        //PR493750-xiaobin-yang-001 begin
        //Invoke the private singleton method to create a singleton DrmManagerClient Object.
//        drmClient = this.createSingletonDrmClient(mContext);
        //PR493750-xiaobin.yang-001 end
        //FR 576703 add by xiangchen end


        mController = new MovieControllerOverlay(movieActivity, mIsAttachment);//modify by yaping.liu for pr506689
        ((ViewGroup)rootView).addView(mController.getView());
        mController.setListener(this);
        mController.setCanReplay(canReplay);
        
        init(movieActivity, info, canReplay);
        
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        //we move this behavior to startVideo()
        //mVideoView.setVideoURI(mUri, null, !mWaitMetaData);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mController.show();
                return true;
            }
        });

        // The SurfaceView is transparent before drawing the first frame.
        // This makes the UI flashing when open a video. (black -> old screen
        // -> video) However, we have no way to know the timing of the first
        // frame. So, we hide the VideoView for a while to make sure the
        // video has been drawn on it.
        mVideoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVideoView.setVisibility(View.VISIBLE);
            }
        }, BLACK_TIMEOUT);
        
        setOnSystemUiVisibilityChangeListener();
        // Hide system UI by default
        showSystemUi(false);

        mAudioBecomingNoisyReceiver = new AudioBecomingNoisyReceiver();
        mAudioBecomingNoisyReceiver.register();

        if (savedInstance != null) { // this is a resumed activity
            mVideoPosition = savedInstance.getInt(KEY_VIDEO_POSITION, 0);
            mResumeableTime = savedInstance.getLong(KEY_RESUMEABLE_TIME, Long.MAX_VALUE);
            onRestoreInstanceState(savedInstance);
            mHasPaused = true;
        } else {
            mTState = TState.PLAYING;
            mFirstBePlayed = true;
            //modified by qjz for PR481448 20130704 begin
            /*final BookmarkerInfo bookmark = mBookmarker.getBookmark(mMovieItem.getUri());
            if (bookmark != null) {
                showResumeDialog(movieActivity, bookmark);
            } else {*/
                //startVideoCareDrm();
            	//Log.i("yanlong", " ****************  startplay video ");
                mVideoCanSeek = true;
                doStartVideoCareDrm(false, position, 0);//modified by yanlong
            //}
            //modified by qjz for PR481448 20130704 end
        }
        mScreenModeExt.setScreenMode();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setOnSystemUiVisibilityChangeListener() {
        if (!ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_HIDE_NAVIGATION) return;

        // When the user touches the screen or uses some hard key, the framework
        // will change system ui visibility from invisible to visible. We show
        // the media control and enable system UI (e.g. ActionBar) to be visible at this point
        mVideoView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                boolean finish = (mActivityContext == null ? true : mActivityContext.isFinishing());
                int diff = mLastSystemUiVis ^ visibility;
                mLastSystemUiVis = visibility;
                if ((diff & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0
                        && (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    mController.show();
                    mRootView.setBackgroundColor(Color.BLACK);
                }
                
                if (LOG) {
                    MtkLog.v(TAG, "onSystemUiVisibilityChange(" + visibility + ") finishing()=" + finish);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showSystemUi(boolean visible) {
        if (!ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) return;
        int flag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (!visible) {
            // We used the deprecated "STATUS_BAR_HIDDEN" for unbundling
            flag |= View.STATUS_BAR_HIDDEN | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        mVideoView.setSystemUiVisibility(flag);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_VIDEO_POSITION, mVideoPosition);
        outState.putLong(KEY_RESUMEABLE_TIME, mResumeableTime);
        onSaveInstanceStateMore(outState);
    }

    private void showResumeDialog(Context context, final BookmarkerInfo bookmark) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.resume_playing_title);
        builder.setMessage(String.format(
                context.getString(R.string.resume_playing_message),
                GalleryUtils.formatDuration(context, bookmark.mBookmark / 1000)));
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onCompletion();
            }
        });
        builder.setPositiveButton(
                R.string.resume_playing_resume, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //here try to seek for bookmark
                //Note: if current video can not be sought, it will not has any bookmark.
                mVideoCanSeek = true;
                doStartVideoCareDrm(true, bookmark.mBookmark, bookmark.mDuration);
            }
        });
        builder.setNegativeButton(
                R.string.resume_playing_restart, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doStartVideoCareDrm(true, 0, bookmark.mDuration);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                mIsShowResumingDialog = true;
            }
        });
        dialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                mIsShowResumingDialog = false;
            }
        });
        dialog.show();
    }

    public boolean onPause() {
        if (LOG) {
            MtkLog.v(TAG, "onPause() isLiveStreaming()=" + isLiveStreaming());
        }
        boolean pause = false;
        if (isLiveStreaming()) {
            pause = false;
        } else {
            doOnPause();
            pause = true;
        }
        if (LOG) {
            MtkLog.v(TAG, "onPause() , return " + pause);
        }
        return pause;
    }
    
    //we should stop video anyway after this function called.
    public void onStop() {
        if (LOG) {
            MtkLog.v(TAG, "onStop() mHasPaused=" + mHasPaused);
        }
        if (!mHasPaused) {
            doOnPause();
        }
    }
    
    private void doOnPause() {
        long start = System.currentTimeMillis();
        addBackground();
        mHasPaused = true;
        mHandler.removeCallbacksAndMessages(null);
        int position = mVideoView.getCurrentPosition();
        mVideoPosition = position >= 0 ? position : mVideoPosition;
        MtkLog.v(TAG, "mVideoPosition is " + mVideoPosition);
        int duration = mVideoView.getDuration();
        mVideoLastDuration = duration > 0 ? duration : mVideoLastDuration;
        mBookmarker.setBookmark(mMovieItem.getUri(), mVideoPosition, mVideoLastDuration);
        long end1 = System.currentTimeMillis();
        mVideoView.stopPlayback();//change suspend to release for sync paused and killed case
        mResumeableTime = System.currentTimeMillis() + RESUMEABLE_TIMEOUT;
        mVideoView.setResumed(false);//avoid start after surface created
        mVideoView.setVisibility(View.INVISIBLE);//Workaround for last-seek frame difference
        
        long end2 = System.currentTimeMillis();
        mOverlayExt.clearBuffering();//to end buffer state
        mServerTimeoutExt.recordDisconnectTime();
        if (LOG) {
            MtkLog.v(TAG, "doOnPause() save video info consume:" + (end1 - start));
            MtkLog.v(TAG, "doOnPause() suspend video consume:" + (end2 - end1));
            MtkLog.v(TAG, "doOnPause() mVideoPosition=" + mVideoPosition + ", mResumeableTime=" + mResumeableTime
                + ", mVideoLastDuration=" + mVideoLastDuration + ", mIsShowResumingDialog="
                + mIsShowResumingDialog);
        }
    }

    public void onResume() {
        dump();
        mDragging = false;//clear drag info
        if (mHasPaused) {
            /// M: same as launch case to delay transparent. @{
            mVideoView.removeCallbacks(mDelayVideoRunnable);
            mVideoView.postDelayed(mDelayVideoRunnable, BLACK_TIMEOUT);
            /// @}
            
            //added by sima.chen for PR810665 begin
            mPlayPauseProcessExt.mNeedCheckPauseSuccess = false;
            mPlayPauseProcessExt.mPauseSuccess = false;
            mPlayPauseProcessExt.mPlayVideoWhenPaused = false;
            //added by sima.chen for PR810665 end
            
            if (mServerTimeoutExt.handleOnResume() || mIsShowResumingDialog) {
                mHasPaused = false;
                return;
            }
            switch(mTState) {
            case RETRY_ERROR:
                mRetryExt.showRetry();
                break;
            case STOPED:
                mPlayerExt.stopVideo();
                break;
            case COMPELTED:
                mController.showEnded();
                if (mVideoCanSeek || mVideoView.canSeekForward()) { 
                    mVideoView.seekTo(mVideoPosition);
                }
                mVideoView.setDuration(mVideoLastDuration);
                break;
            case PAUSED:
                //if video was paused, so it should be started.
                doStartVideo(true, mVideoPosition, mVideoLastDuration, false);
                pauseVideo();
                break;
            default:
                if (mConsumedDrmRight) {
                    doStartVideo(true, mVideoPosition, mVideoLastDuration);
                } else {
                    doStartVideoCareDrm(true, mVideoPosition, mVideoLastDuration);
                }
                pauseVideoMoreThanThreeMinutes();
                break;
            }
            mVideoView.dump();
            mHasPaused = false;
        }
        mHandler.post(mProgressChecker);
    }

    private void pauseVideoMoreThanThreeMinutes() {
        // If we have slept for too long, pause the play
        // If is live streaming, do not pause it too
        long now = System.currentTimeMillis();
        if (now > mResumeableTime && !isLiveStreaming()
                && ExtensionHelper.getMovieStrategy(mActivityContext).shouldEnableCheckLongSleep()) {
            if (mVideoCanPause || mVideoView.canPause()) {
                pauseVideo();
            }
        }
        if (LOG) {
            MtkLog.v(TAG, "pauseVideoMoreThanThreeMinutes() now=" + now);
        }
    }

    public void onDestroy() {
        //PR690291-limingzhu begin-001
        if (retriever != null) {
            retriever.release();
            retriever = null;
        }
        //PR690291-limingzhu end-001
        mVideoView.stopPlayback();
        mAudioBecomingNoisyReceiver.unregister();
        mServerTimeoutExt.clearTimeoutDialog();
    }

    // This updates the time bar display (if necessary). It is called every
    // second by mProgressChecker and also from places where the time bar needs
    // to be updated immediately.
    private int setProgress() {
        if (LOG) {
            MtkLog.v(TAG, "setProgress() mDragging=" + mDragging + ", mShowing=" + mShowing
                + ", mIsOnlyAudio=" + mIsOnlyAudio);
        }
        if (mDragging || (!mShowing && !mIsOnlyAudio)) {
            return 0;
        }
        int position = mVideoView.getCurrentPosition();
        int duration = mVideoView.getDuration();
        mController.setTimes(position, duration, 0, 0);
        if (mControllerRewindAndForwardExt != null && mControllerRewindAndForwardExt.getPlayPauseEanbled()) {
            updateRewindAndForwardUI();
        }
        return position;
    }
    //add by qjz for NewUI 2013-03-16
    private MediaMetadataRetriever retriever;
    private long videoDuration;
    private void setMediaMetadataSource(Uri uri,Map<String, String> headers) {
        mSeekTo = -1;
        try {
            if (retriever != null) {
                retriever.release();
                retriever = null;
            }
            retriever = new MediaMetadataRetriever();
            if (uri != null) {
                retriever.setDataSource(mContext, uri);
            }
            String duration=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            videoDuration = Long.valueOf(duration)*1000;
        } catch (Exception ex) {
            Log.d(TAG,"setMediaMetadataSource Error:"+ex.toString());
        }
    }
    //add by qjz for NewUI 2013-03-20
    private int getCurrentPosition(long atTime) {
        int period = -1;
        if (mController.getIsLand()) {
            period = (int) (videoDuration/7);
            if (atTime<=50) {
                return 0;
            } else if (Math.abs(atTime*1000 -videoDuration)<=50) {
                return 8;
            }
            return (int) (atTime*1000/period + 1);
        } else {
            period = (int) (videoDuration/3);
            if (atTime<=50) {
                return 2;
            } else if (Math.abs(atTime*1000 -videoDuration)<=50) {
                return 6;
            }
            return (int) (atTime*1000/period + 3);
        }
    }
    private int getRetrieveFramePeriod() {
        if  (videoDuration/1000000 <= 1) {
            return 5000;
        } else if (videoDuration/1000000 <= 10) {
            return 50000;
        } else if (videoDuration/1000000 <= 60) {
            return 500000;
        } else if (videoDuration/1000000 <= 600) {
            return 1000000;
        } else if (videoDuration/1000000 <= 3600) {
            return 2000000;
        } else  {
            return 4000000;
        }
    }

    private Bitmap retrieveFrameFromVideo(long seekToTime) {
        Bitmap temp;
        try {
            temp = Bitmap.createScaledBitmap(retriever.getFrameAtTime(seekToTime*1000), 320, 200, true);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
            return null;
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
            return null;
        } finally {
        }
        return temp;
    }

    /*private Bitmap[] retrieveFrameFromVideo(long atTime){
        int currentPosition = getCurrentPosition(atTime);
        if (mSeekTo/1000 == atTime/1000) {
            Log.i("QQ", "mSeekTo/1000 == atTime/1000 atTime = "+atTime);
            return null;
        }
        mSeekTo = atTime;
        try {
            long videoCurrent = atTime*1000;
            if(videoCurrent > videoDuration) videoCurrent=videoDuration;
            int period = getRetrieveFramePeriod();
            Log.d("QQ","videoCurrent - "+videoCurrent+" period - "+period);
            if (mController.getIsLand()) {
                for (int i=-4;i<5;i++) {
                    int index = i+4;
                    long temp = videoCurrent+i*period;
                    if (currentPosition != index) {
                        mBitmap[i+4] = Bitmap.createScaledBitmap(retriever.getFrameAtTime((temp >= 0)?((temp<=videoDuration)?temp:videoDuration):0), 100, 80, true);
                    } else {
                        mBitmap[i+4] = Bitmap.createScaledBitmap(retriever.getFrameAtTime((temp >= 0)?((temp<=videoDuration)?temp:videoDuration):0), 160, 120, true);
                    }
                }
            } else {
                for (int i=-2;i<3;i++) {
                    int index = i+4;
                    long temp = videoCurrent+i*period;
                    if (currentPosition != index) {
                        mBitmap[i+4] = Bitmap.createScaledBitmap(retriever.getFrameAtTime((temp >= 0)?((temp<=videoDuration)?temp:videoDuration):0), 100, 80, true);
                    } else {
                        mBitmap[i+4] = Bitmap.createScaledBitmap(retriever.getFrameAtTime((temp >= 0)?((temp<=videoDuration)?temp:videoDuration):0), 140, 120, true);
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
            return null;
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
            return null;
        } finally {
        }
        Message msg = new Message();
        msg.what = MSG_UPDATE_SEEK_FRAME;
        msg.arg1 = (int) atTime;
        myHandler.sendMessage(msg);
        return mBitmap;
    }*/
    //modified by qjz for NewVedioPlayerUI 20130405 end
    private void doStartVideo(final boolean enableFasten, final int position, final int duration, boolean start) {
        if (LOG) {
            MtkLog.v(TAG, "doStartVideo(" + enableFasten + ", " + position + ", " + duration + ", " + start + ")");
        }
        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        String scheme = mMovieItem.getUri().getScheme();
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)
                || "https".equalsIgnoreCase(scheme)) {
            mController.showLoading();
            mOverlayExt.setPlayingInfo(isLiveStreaming());
            mHandler.removeCallbacks(mPlayingChecker);
            mHandler.postDelayed(mPlayingChecker, 250);
        } else {
            mController.showPlaying();
            mController.hide();
        }
        /// M: add play/pause asynchronous processing @{
        if(onIsRTSP()){
            Map<String, String> header = new HashMap<String, String>(1);
            header.put("MTK-ASYNC-RTSP-PAUSE-PLAY", "true");
            mVideoView.setVideoURI(mMovieItem.getUri(), header/*, !mWaitMetaData*/);//PR593460 wanwan.ye (porting by liqiang for PR595899)
            setMediaMetadataSource(mMovieItem.getUri(), header);//add by qjz for NewUI 2013-03-19 no support RTSP
            //mController.setVideoType(true);//add by qjz for PR463745 20130606
        } else {
            mVideoView.setVideoURI(mMovieItem.getUri(), null/*, !mWaitMetaData*/);//PR593460 wanwan.ye (porting by liqiang for PR595899)
            setMediaMetadataSource(mMovieItem.getUri(), null);//add by qjz for NewUI 2013-03-19
            //mController.setVideoType(false);//add by qjz for PR463745 20130606
        }
        if (start) {
            mVideoView.start();
        }
        /// @}
        //we may start video from stopVideo,
        //this case, we should reset canReplay flag according canReplay and loop
        boolean loop = mPlayerExt.getLoop();
        boolean canReplay = loop ? loop : mCanReplay;
        mController.setCanReplay(canReplay);
        mController.setVideoType(mStreamingType != STREAMING_LOCAL);//add by qjz for PR466452
        if (position > 0 && (mVideoCanSeek || mVideoView.canSeekForward())) {
            mVideoView.seekTo(position);
        }
        if (enableFasten) {
            mVideoView.setDuration(duration);
        }
        setProgress();
    }

    private void doStartVideo(boolean enableFasten, int position, int duration) {
        ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(
                null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        doStartVideo(enableFasten, position, duration, true);
    }
    
    private void playVideo() {
        if (LOG) {
            MtkLog.v(TAG, "playVideo()");
        }
        mTState = TState.PLAYING;
        mVideoView.start();
        mController.showPlaying();
        setProgress();
    }

    private void pauseVideo() {
        if (LOG) {
            MtkLog.v(TAG, "pauseVideo()");
        }
        mTState = TState.PAUSED;
        mVideoView.pause();
        mController.showPaused();
        setProgress();
    }

    // Below are notifications from VideoView
    @Override
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        if (LOG) {
            MtkLog.v(TAG, "onError(" + player + ", " + arg1 + ", " + arg2 + ")");
        }
        mMovieItem.setError();
        if (mServerTimeoutExt.onError(player, arg1, arg2)) {
            return true;
        }
        if (mRetryExt.onError(player, arg1, arg2)) {
            return true;
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(mProgressChecker);//always show progress
        // VideoView will show an error dialog if we return false, so no need
        // to show more message.
        //M:resume controller
        mController.setViewEnabled(true);
        mController.showErrorMessage("");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (LOG) {
            MtkLog.v(TAG, "onCompletion() mCanReplay=" + mCanReplay);
        }
        if (mMovieItem.getError()) {
            MtkLog.w(TAG, "error occured, exit the video player!");
            mActivityContext.finish();
            return;
        }
        //PR622918  by minrui.yan BEGIN
        //FR 576703 add by xiangchen begin
        //PR493750-xiaobin-yang-001 begin
        if(mp == null) {//onStop callback method invoked.
            if(drmClient == null) {
                drmClient = this.createSingletonDrmClient(mContext);
            }
            int rightsStatus = DrmStore.RightsStatus.RIGHTS_VALID;
            Uri movieItemUri = mMovieItem.getUri();
            //Modify by Rock.Song for PR678251 @20140520 {
            if(drmClient != null && drmClient.isDrm(movieItemUri)) {
                rightsStatus = drmClient.checkRightsStatus(movieItemUri, DrmStore.Action.PLAY);
            }
            //}Modify by Rock.Song for PR678251 @20140520
            /**
             * When onStop callback method of IMovieDrmCallback invoked.
             * It is means drm video file's rights has expired.
             * We should not invoke the onReplay method again even though the return
             * value of getLoop is true.
             */
            if(mPlayerExt.getLoop() && rightsStatus == DrmStore.RightsStatus.RIGHTS_VALID) {
                onReplay();
            } else {//original logic
                mTState = TState.COMPELTED;
                if(mCanReplay) {
                    mController.showEnded();
                }
                onCompletion();
            }
        } else {//other callback method invoked.
            if(mPlayerExt.getLoop()) {
                onReplay();
            } else {//original logic
                mTState = TState.COMPELTED;
                if(mCanReplay) {
                    mController.showEnded();
                }
                onCompletion();
            }
        }
        //PR493750-xiaobin-yang-001 end
        //FR 576703 add by xiangchen end
        //PR622918  by minrui.yan END
    }

    public void onCompletion() {
    }

    // Below are notifications from ControllerOverlay
    @Override
    public void onPlayPause() {
        if (mVideoView.isPlaying()) {
            if (mVideoView.canPause()) {
                pauseVideo();
                //set view disabled(play/pause asynchronous processing)
                mController.setViewEnabled(false);
            }
        } else {
            playVideo();
            //set view disabled(play/pause asynchronous processing)
            mController.setViewEnabled(false);
        }
    }
    
    public boolean isPlaying(){
        return mVideoView.isPlaying();
    }

    @Override
    public void onSeekStart() {
        if (LOG) {
            MtkLog.v(TAG, "onSeekStart() mDragging=" + mDragging);
        }
        mDragging = true;
    }

    @Override
    public void onSeekMove(int time) {
        if (LOG) {
            MtkLog.v(TAG, "onSeekMove(" + time + ") mDragging=" + mDragging);
        }
        if (!mDragging) {
            mVideoView.seekTo(time);
        }
    }

    @Override
    public void onSeekEnd(int time, int start, int end) {
        if (LOG) {
            MtkLog.v(TAG, "onSeekEnd(" + time + ") mDragging=" + mDragging);
        }
        mDragging = false;
        mVideoView.seekTo(time);
        //setProgress();
    }

    @Override
    public void onShown() {
        if (LOG) {
            MtkLog.v(TAG, "onShown");
        }
        addBackground();
        mShowing = true;
        setProgress();
        showSystemUi(true);
    }

    @Override
    public void onHidden() {
        if (LOG) {
            MtkLog.v(TAG, "onHidden");
        }
        mShowing = false;
        showSystemUi(false);
        removeBackground();
    }
    @Override
    public boolean onIsRTSP() {
        if (MovieUtils.isRtspStreaming(mMovieItem.getUri(), mMovieItem
                .getMimeType())) {
            MtkLog.v(TAG, "onIsRTSP() is RTSP");
            return true;
        }
        MtkLog.v(TAG, "onIsRTSP() is not RTSP");
        return false;
    }
    
    public void updateRewindAndForwardUI(){
        if(mControllerRewindAndForwardExt != null){
            mControllerRewindAndForwardExt.showControllerButtonsView(mPlayerExt
                    .canStop(), mVideoView.canSeekBackward()
                    && mVideoView.getCurrentPosition() > 0 && mControllerRewindAndForwardExt.getTimeBarEanbled(), mVideoView
                    .canSeekForward()
                    && (mVideoView.getCurrentPosition() < mVideoView
                            .getDuration()) && mControllerRewindAndForwardExt.getTimeBarEanbled());
        }
    }

    @Override
    public void onReplay() {
        if (LOG) {
            MtkLog.v(TAG, "onReplay()");
        }
        mFirstBePlayed = true;
        if (mRetryExt.handleOnReplay()) {
            return;
        }
        startVideoCareDrm();
    }
    //add by qjz for NewUI 2013-03-15
    @Override
    public void onChangeLoopMode() {
       //FR450344-yanlong.li@tcl.com-begin
        boolean popupVideo = false;
        try{
            //PR519068,In lanscape mode, it shows black window when play drm video via pop up video,bin.li@tct-nj.com
            if(MovieActivity.isDrm || mIsAttachment) popupVideo=false;//modify by yaping.liu for pr506689
            else popupVideo =  mActivityContext.getResources().getBoolean(R.bool.def_gallery_pop_up_video);
            //end PR519068
            }catch(NotFoundException nfe){
                Log.i(TAG, " not found customize key def_gallery_pop_up_video ");
            }
        if(popupVideo){
        Intent intent =new Intent();
        intent.setAction("com.jrdcom.action.INIT_POPUPWINDOW");
        intent.setDataAndType(mMovieItem.getOriginalUri(), mMovieItem.getMimeType());
        intent.putExtra(MovieActivity.KEY_TREAT_UP_AS_BACK, mTreatUpAsBack);//add by qjz for PR487297
        intent.putExtra(MovieActivity.KEY_LOGO_BITMAP, mLogo);
        Bundle bundle=new Bundle();
        bundle.putInt("position", mVideoView.getCurrentPosition());
        /*Log.i("yanlong", " video size width: "+this.getVideoSurface().getWidth()
                +" video size height "+this.getVideoSurface().getHeight());*/
        //bundle.putString("uri", mMovieItem.getOriginalUri().toString());
        //bundle.putString("mimetype", mMovieItem.getMimeType());
        mContext.startService(intent.putExtras(bundle));
        mActivityContext.finish();
        }else{
            mPlayerExt.setLoop(mPlayerExt.getLoop()?false:true);
        }
		//FR450344-yanlong.li@tcl.com-end
    }
    //modified by qjz for NewVedioPlayerUI 20130405 begin
    /*@Override
    public void onShowSeekToView(int seek) {
        if (!isSeek) {
            final int seekTime = seek;
            new Thread() {
                @Override
                public void run() {
                    isSeek = true;
                    retrieveFrameFromVideo(seekTime);
                    isSeek = false;
                }
            }.start();
        }
    }*/
    @Override
    public void onShowSeekToView(int seek) {
        mSeekTime = seek;
        if (!isSeek) {
            final int seekTime = seek;
            new Thread() {
                @Override
                public void run() {
                    isSeek = true;
                    mBitmap = retrieveFrameFromVideo(seekTime);
                    Message msg = new Message();
                    msg.what = MSG_UPDATE_SEEK_FRAME;
                    msg.arg1 = mSeekTime;
                    myHandler.sendMessage(msg);
                    isSeek = false;
                }
            }.start();
        }
        Message msg = new Message();
        msg.what = MSG_UPDATE_SEEK_FRAME;
        msg.arg1 = mSeekTime;
        myHandler.sendMessage(msg);
    }
    //modified by qjz for NewVedioPlayerUI 20130405 end
    @Override
    public void onSetSeekViewShow(boolean isShow) {
    }

    // Below are key events passed from MovieActivity.
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Some headsets will fire off 7-10 events on a single click
        if (event.getRepeatCount() > 0) {
            return isMediaKey(keyCode);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mVideoView.isPlaying() && mVideoView.canPause()) {
                    pauseVideo();
                } else {
                    playVideo();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (mVideoView.isPlaying() && mVideoView.canPause()) {
                    pauseVideo();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if (!mVideoView.isPlaying()) {
                    playVideo();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                // TODO: Handle next / previous accordingly, for now we're
                // just consuming the events.
                return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return isMediaKey(keyCode);
    }

    private static boolean isMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE;
    }

    // We want to pause when the headset is unplugged.
    private class AudioBecomingNoisyReceiver extends BroadcastReceiver {

        public void register() {
            mContext.registerReceiver(this,
                    new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }

        public void unregister() {
            mContext.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mVideoView.isPlaying() && mVideoView.canPause()) pauseVideo();
        }
    }
    
    /// M: for log flag, if set this false, will improve run speed.
    private static final boolean LOG = true;
    private MovieActivity mActivityContext;//for dialog and toast context
    private boolean mFirstBePlayed = false;//for toast more info

    private void init(final MovieActivity movieActivity, IMovieItem info, boolean canReplay) {
        mActivityContext = movieActivity;
        mCanReplay = canReplay;
        mMovieItem = info;
        judgeStreamingType(info.getUri(), info.getMimeType());

        //for toast more info and live streaming
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnBufferingUpdateListener(this);
        mVideoView.setOnVideoSizeChangedListener(this);
        
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mController.show();
                return true;
            }
        });
        mOverlayExt = mController.getOverlayExt();
        mControllerRewindAndForwardExt = mController.getControllerRewindAndForwardExt();
        if(mControllerRewindAndForwardExt != null){
            mControllerRewindAndForwardExt.setIListener(mRewindAndForwardListener);
        }
    }
    
    public IMoviePlayer getMoviePlayerExt() {
        return mPlayerExt;
    }
    
    public SurfaceView getVideoSurface() {
        return mVideoView;
    }
    
    /// M: for more detail in been killed case @{
    private static final String KEY_CONSUMED_DRM_RIGHT = "consumed_drm_right";
    private static final String KEY_POSITION_WHEN_PAUSED = "video_position_when_paused";
    private static final String KEY_VIDEO_CAN_SEEK = "video_can_seek";
    private static final String KEY_VIDEO_CAN_PAUSE = "video_can_pause";
    private static final String KEY_VIDEO_LAST_DURATION = "video_last_duration";
    private static final String KEY_VIDEO_LAST_DISCONNECT_TIME = "last_disconnect_time";
    private static final String KEY_VIDEO_STATE = "video_state";
    private static final String KEY_VIDEO_STREAMING_TYPE = "video_streaming_type";
    
    private int mVideoLastDuration;//for duration displayed in init state
    private boolean mVideoCanPause = false;
    private boolean mVideoCanSeek = false;
    private void onSaveInstanceStateMore(Bundle outState) {
        //for more details
        mServerTimeoutExt.onSaveInstanceState(outState);
        outState.putInt(KEY_VIDEO_LAST_DURATION, mVideoLastDuration);
        outState.putBoolean(KEY_VIDEO_CAN_PAUSE, mVideoView.canPause());
        outState.putBoolean(KEY_VIDEO_CAN_SEEK, mVideoView.canSeekForward());
        outState.putBoolean(KEY_CONSUMED_DRM_RIGHT, mConsumedDrmRight);
        outState.putInt(KEY_VIDEO_STREAMING_TYPE, mStreamingType);
        outState.putString(KEY_VIDEO_STATE, String.valueOf(mTState));
        mScreenModeExt.onSaveInstanceState(outState);
        mRetryExt.onSaveInstanceState(outState);
        mPlayerExt.onSaveInstanceState(outState);
        if (LOG) {
            MtkLog.v(TAG, "onSaveInstanceState(" + outState + ")");
        }
    }

    private void onRestoreInstanceState(Bundle icicle) {
        mVideoLastDuration = icicle.getInt(KEY_VIDEO_LAST_DURATION);
        mVideoCanPause = icicle.getBoolean(KEY_VIDEO_CAN_PAUSE);
        mVideoCanSeek = icicle.getBoolean(KEY_VIDEO_CAN_SEEK);
        mConsumedDrmRight = icicle.getBoolean(KEY_CONSUMED_DRM_RIGHT);
        mStreamingType = icicle.getInt(KEY_VIDEO_STREAMING_TYPE);
        mTState = TState.valueOf(icicle.getString(KEY_VIDEO_STATE));
        
        mScreenModeExt.onRestoreInstanceState(icicle);
        mServerTimeoutExt.onRestoreInstanceState(icicle);
        mRetryExt.onRestoreInstanceState(icicle);
        mPlayerExt.onRestoreInstanceState(icicle);
        if (LOG) {
            MtkLog.v(TAG, "onRestoreInstanceState(" + icicle + ")");
        }
    }
    /// @}
    
    private void clearVideoInfo() {
        mVideoPosition = 0;
        mVideoLastDuration = 0;
        mIsOnlyAudio = false;
        mConsumedDrmRight = false;
        if (mServerTimeoutExt != null) {
            mServerTimeoutExt.clearServerInfo();
        }
    }

    private void getVideoInfo(MediaPlayer mp) {
        if (!MovieUtils.isLocalFile(mMovieItem.getUri(), mMovieItem.getMimeType())) {
            Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
                    MediaPlayer.BYPASS_METADATA_FILTER);
            if (data != null) {
                mServerTimeoutExt.setVideoInfo(data);
                mPlayerExt.setVideoInfo(data);
            } else {
                MtkLog.w(TAG, "Metadata is null!");
            }
            int duration = mp.getDuration();
            if (duration <= 0) {
                mStreamingType = STREAMING_SDP;//correct it
            } else {
                //correct sdp to rtsp
                if (mStreamingType == STREAMING_SDP) {
                    mStreamingType = STREAMING_RTSP;
                }
            }
            if (LOG) {
                MtkLog.v(TAG, "getVideoInfo() duration=" + duration + ", mStreamingType=" + mStreamingType);
            }
        }
    }
    
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (LOG) {
            MtkLog.v(TAG, "onPrepared(" + mp + ")");
        }
        getVideoInfo(mp);
        if (!isLocalFile()) { //hear we get the correct streaming type.
            mOverlayExt.setPlayingInfo(isLiveStreaming());
        }
        boolean canPause = mVideoView.canPause();
        boolean canSeek = mVideoView.canSeekBackward() && mVideoView.canSeekForward();
        mOverlayExt.setCanPause(canPause);
        mOverlayExt.setCanScrubbing(canSeek);
        //resume play pause button (play/pause asynchronous processing)
        mController.setPlayPauseReplayResume();
        if (!canPause && !mVideoView.isTargetPlaying()) {
            mVideoView.start();
        }
        updateRewindAndForwardUI();
        if (LOG) {
            MtkLog.v(TAG, "onPrepared() canPause=" + canPause + ", canSeek=" + canSeek);
        }
    }
    
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (LOG) {
            MtkLog.v(TAG, "onInfo() what:" + what + " extra:" + extra);
        }
        if (mRetryExt.onInfo(mp, what, extra)) {
            return true;
        }

        //added by sima.chen for 810665 begin
        if(mPlayPauseProcessExt.onInfo(mp, what, extra)){
            return true;
        }
        //added by sima.chen for 810665 end
        
        if (mFirstBePlayed && what == MediaPlayer.MEDIA_INFO_VIDEO_NOT_SUPPORTED) {
            Toast.makeText(mActivityContext, R.string.VideoView_info_text_video_not_supported, Toast.LENGTH_SHORT).show();
            mFirstBePlayed = false;
            return true;
        }
        return false;
    }
    
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (!mPlayerExt.pauseBuffering()) {
            boolean fullBuffer = isFullBuffer();
            mOverlayExt.showBuffering(fullBuffer, percent);
        }
        if (LOG) {
            MtkLog.v(TAG, "onBufferingUpdate(" + percent + ") pauseBuffering=" + mPlayerExt.pauseBuffering());
        }
    }

    /// M: for streaming feature @{
    //judge and support sepcial streaming type
    public static final int STREAMING_LOCAL = 0;
    public static final int STREAMING_HTTP = 1;
    public static final int STREAMING_RTSP = 2;
    public static final int STREAMING_SDP = 3;
    
    private boolean mWaitMetaData;
    private int mStreamingType = STREAMING_LOCAL;
    private boolean mCanReplay;
    
    private void judgeStreamingType(Uri uri, String mimeType) {
        if (LOG) {
            MtkLog.v(TAG, "judgeStreamingType(" + uri + ")");
        }
        if (uri == null) {
            return;
        }
        String scheme = uri.getScheme();
        mWaitMetaData = true;
        if (MovieUtils.isSdpStreaming(uri, mimeType)) {
            mStreamingType = STREAMING_SDP;
        } else if (MovieUtils.isRtspStreaming(uri, mimeType)) {
            mStreamingType = STREAMING_RTSP;
        } else if (MovieUtils.isHttpStreaming(uri, mimeType)) {
            mStreamingType = STREAMING_HTTP;
            mWaitMetaData = false;
        } else {
            mStreamingType = STREAMING_LOCAL;
            mWaitMetaData = false;
        }
        if (LOG) {
            MtkLog.v(TAG, "mStreamingType=" + mStreamingType
                + " mCanGetMetaData=" + mWaitMetaData);
        }
    }
    
    public boolean isFullBuffer() {
        if (mStreamingType == STREAMING_RTSP || mStreamingType == STREAMING_SDP) {
            return false;
        }
        return true;
    }
    
    public boolean isLocalFile() {
        if (mStreamingType == STREAMING_LOCAL) {
            return true;
        }
        return false;
    }
    
    public boolean isLiveStreaming() {
        boolean isLive = false;
        if (mStreamingType == STREAMING_SDP) {
            isLive = true;
        }
        if (LOG) {
            MtkLog.v(TAG, "isLiveStreaming() return " + isLive);
        }
        return isLive;
    }
    /// @}
    
    /// M: for drm feature @{
    private boolean mConsumedDrmRight = false;
    private IMovieDrmExtension mDrmExt = ExtensionHelper.getMovieDrmExtension(mActivityContext);
    private void doStartVideoCareDrm(final boolean enableFasten, final int position, final int duration) {
        if (LOG) {
            MtkLog.v(TAG, "doStartVideoCareDrm(" + enableFasten + ", " + position + ", " + duration + ")");
        }
        mTState = TState.PLAYING;
        if (!mDrmExt.handleDrmFile(mActivityContext, mMovieItem, new IMovieDrmCallback() {
            @Override
            public void onContinue() {
                doStartVideo(enableFasten, position, duration);
                mConsumedDrmRight = true;
            }
            @Override
            public void onStop() {
                onCompletion(null);
            }
        })) {
            doStartVideo(enableFasten, position, duration);
        }
    }
    
    private void startVideoCareDrm() {
        doStartVideoCareDrm(false, 0, 0);
    }
    /// @}
    
    /// M: for dynamic change video size(http live streaming) @{
    private boolean mIsOnlyAudio = false;
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        //reget the audio type
        if (width != 0 && height != 0) {
            mIsOnlyAudio = false;
        } else {
            mIsOnlyAudio = true;
        }
        mOverlayExt.setBottomPanel(mIsOnlyAudio, true);
        if (LOG) {
            MtkLog.v(TAG, "onVideoSizeChanged(" + width + ", " + height + ") mIsOnlyAudio=" + mIsOnlyAudio);
        }
    }
    /// @}
    
    private void dump() {
        if (LOG) {
            MtkLog.v(TAG, "dump() mHasPaused=" + mHasPaused
                + ", mVideoPosition=" + mVideoPosition + ", mResumeableTime=" + mResumeableTime
                + ", mVideoLastDuration=" + mVideoLastDuration + ", mDragging=" + mDragging
                + ", mConsumedDrmRight=" + mConsumedDrmRight + ", mVideoCanSeek=" + mVideoCanSeek
                + ", mVideoCanPause=" + mVideoCanPause + ", mTState=" + mTState
                + ", mIsShowResumingDialog=" + mIsShowResumingDialog);
        }
    }
    
    //for more killed case, same as videoview's state and controller's state.
    //will use it to sync player's state.
    //here doesn't use videoview's state and controller's state for that
    //videoview's state doesn't have reconnecting state and controller's state has temporary state.
    private enum TState {
        PLAYING,
        PAUSED,
        STOPED,
        COMPELTED,
        RETRY_ERROR
    }
    
    private TState mTState = TState.PLAYING;
    private IMovieItem mMovieItem;
    private RetryExtension mRetryExt = new RetryExtension();
    private ScreenModeExt mScreenModeExt = new ScreenModeExt();
    private ServerTimeoutExtension mServerTimeoutExt = new ServerTimeoutExtension();
    private MoviePlayerExtension mPlayerExt = new MoviePlayerExtension();
    private IContrllerOverlayExt mOverlayExt;
    private IControllerRewindAndForward mControllerRewindAndForwardExt;
    private IRewindAndForwardListener mRewindAndForwardListener = new ControllerRewindAndForwardExt();;
    
    interface Restorable {
        void onRestoreInstanceState(Bundle icicle);
        void onSaveInstanceState(Bundle outState);
    }

    private class RetryExtension implements Restorable, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {
        private static final String KEY_VIDEO_RETRY_COUNT = "video_retry_count";
        private int mRetryDuration;
        private int mRetryPosition;
        private int mRetryCount;
        public void retry() {
            doStartVideoCareDrm(true, mRetryPosition, mRetryDuration);
            if (LOG) {
                MtkLog.v(TAG, "retry() mRetryCount=" + mRetryCount + ", mRetryPosition=" + mRetryPosition);
            }
        }
        
        public void clearRetry() {
            if (LOG) {
                MtkLog.v(TAG, "clearRetry() mRetryCount=" + mRetryCount);
            }
            mRetryCount = 0;
        }
        
        public boolean reachRetryCount() {
            if (LOG) {
                MtkLog.v(TAG, "reachRetryCount() mRetryCount=" + mRetryCount);
            }
            if (mRetryCount > 3) {
                return true;
            }
            return false;
        }
        
        public int getRetryCount() {
            if (LOG) {
                MtkLog.v(TAG, "getRetryCount() return " + mRetryCount);
            }
            return mRetryCount;
        }
        
        public boolean isRetrying() {
            boolean retry = false;
            if (mRetryCount > 0) {
                retry = true;
            }
            if (LOG) {
                MtkLog.v(TAG, "isRetrying() mRetryCount=" + mRetryCount);
            }
            return retry;
        }
        
        @Override
        public void onRestoreInstanceState(Bundle icicle) {
            mRetryCount = icicle.getInt(KEY_VIDEO_RETRY_COUNT);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putInt(KEY_VIDEO_RETRY_COUNT, mRetryCount);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_ERROR_CANNOT_CONNECT_TO_SERVER) {
                //get the last position for retry
                mRetryPosition = mVideoView.getCurrentPosition();
                mRetryDuration = mVideoView.getDuration();
                mRetryCount++;
                if (reachRetryCount()) {
                    mTState = TState.RETRY_ERROR;
                    mOverlayExt.showReconnectingError();
                } else {
                    mOverlayExt.showReconnecting(mRetryCount);
                    retry();
                }
                return true;
            }
            return false;
        }
        
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_INFO_GET_BUFFER_DATA) {
                //this means streaming player has got the display data
                //so we can retry connect server if it has connection error.
                clearRetry();
                return true;
            /// M: receive PAUSE PLAY COMPLETED info 
            //play/pause asynchronous processing  @{
            } else if (what == MediaPlayer.MEDIA_INFO_PAUSE_COMPLETED
                    || what == MediaPlayer.MEDIA_INFO_PLAY_COMPLETED) {
                MtkLog.v(TAG, "onInfo is PAUSE PLAY COMPLETED");
                mController.setViewEnabled(true);
                updateRewindAndForwardUI();
            }
            /// @}
            return false;
        }
        
        public boolean handleOnReplay() {
            if (isRetrying()) { //from connecting error
                clearRetry();
                int errorPosition = mVideoView.getCurrentPosition();
                int errorDuration = mVideoView.getDuration();
                doStartVideoCareDrm(errorPosition > 0, errorPosition, errorDuration);
                if (LOG) {
                    MtkLog.v(TAG, "onReplay() errorPosition=" + errorPosition + ", errorDuration=" + errorDuration);
                }
                return true;
            }
            return false;
        }
        
        public void showRetry() {
            mOverlayExt.showReconnectingError();
            if (mVideoCanSeek || mVideoView.canSeekForward()) { 
                mVideoView.seekTo(mVideoPosition);
            }
            mVideoView.setDuration(mVideoLastDuration);
            mRetryPosition = mVideoPosition;
            mRetryDuration = mVideoLastDuration;
        }
    }
    
    private class ScreenModeExt implements Restorable, ScreenModeListener {
        private static final String KEY_VIDEO_SCREEN_MODE = "video_screen_mode";
        private int mScreenMode = ScreenModeManager.SCREENMODE_BIGSCREEN;
        private ScreenModeManager mScreenModeManager = new ScreenModeManager();
        
        public void setScreenMode() {
            mVideoView.setScreenModeManager(mScreenModeManager);
            mController.setScreenModeManager(mScreenModeManager);
            mScreenModeManager.addListener(this);
            mScreenModeManager.setScreenMode(mScreenMode);//notify all listener to change screen mode
            if (LOG) {
                MtkLog.v(TAG, "setScreenMode() mScreenMode=" + mScreenMode);
            }
        }
        
        @Override
        public void onScreenModeChanged(int newMode) {
            mScreenMode = newMode;//changed from controller
            if (LOG) {
                MtkLog.v(TAG, "OnScreenModeClicked(" + newMode + ")");
            }
        }
        
        @Override
        public void onRestoreInstanceState(Bundle icicle) {
            mScreenMode = icicle.getInt(KEY_VIDEO_SCREEN_MODE, ScreenModeManager.SCREENMODE_BIGSCREEN);
        }
        
        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putInt(KEY_VIDEO_SCREEN_MODE, mScreenMode);
        }
    }
    
    ///M: for CU 4.0 add rewind and forward function
    private class ControllerRewindAndForwardExt implements IRewindAndForwardListener {
        @Override
        public void onPlayPause() {
            onPlayPause();
        }
        @Override
        public void onSeekStart() {
            onSeekStart();
        }
        @Override
        public void onSeekMove(int time) {
            onSeekMove(time);
        }
        @Override
        public void onSeekEnd(int time, int trimStartTime, int trimEndTime) {
            onSeekEnd(time,trimStartTime,trimEndTime);
        }
        @Override
        public void onShown() {
            onShown();
        }
        @Override
        public void onHidden() {
            onHidden();
        }
        @Override
        public void onReplay() {
            onReplay();
        }
        //add by qjz for NewUI 2013-03-14
        @Override
        public void onChangeLoopMode() {
            onChangeLoopMode();
        }
        
        //add by qjz for NewUI 2013-03-15
        @Override
        public void onShowSeekToView(int seek) {
            onShowSeekToView(seek);
        }
        @Override
        public void onSetSeekViewShow(boolean isShow) {
            onSetSeekViewShow(isShow);
        }
        @Override
        public boolean onIsRTSP() {
            return false;
        }
        @Override
        public void onStopVideo() {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt onStopVideo()");
            if (mPlayerExt.canStop()) {
                mPlayerExt.stopVideo();
                mControllerRewindAndForwardExt.showControllerButtonsView(false,
                        false, false);
            }
        }
        @Override
        public void onRewind() {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt onRewind()");
            if (mVideoView != null && mVideoView.canSeekBackward()) {
                mControllerRewindAndForwardExt
                        .showControllerButtonsView(
                                mPlayerExt.canStop(),
                                false,
                                mVideoView.canSeekForward()
                                        && (mVideoView.getCurrentPosition() < mVideoView
                                                .getDuration())
                                        && mControllerRewindAndForwardExt
                                                .getTimeBarEanbled());
                int stepValue = getStepOptionValue();
                int targetDuration = mVideoView.getCurrentPosition()
                        - stepValue < 0 ? 0 : mVideoView.getCurrentPosition()
                        - stepValue;
                MtkLog.v(TAG, "onRewind targetDuration " + targetDuration);
                mVideoView.seekTo(targetDuration);
            } else {
                mControllerRewindAndForwardExt
                        .showControllerButtonsView(
                                mPlayerExt.canStop(),
                                false,
                                mVideoView.canSeekForward()
                                        && (mVideoView.getCurrentPosition() < mVideoView
                                                .getDuration())
                                        && mControllerRewindAndForwardExt
                                                .getTimeBarEanbled());
            }
        }
        @Override
        public void onForward() {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt onForward()");
            if (mVideoView != null && mVideoView.canSeekForward()) {
                mControllerRewindAndForwardExt.showControllerButtonsView(
                        mPlayerExt.canStop(), mVideoView.canSeekBackward()
                                && mVideoView.getCurrentPosition() > 0
                                && mControllerRewindAndForwardExt
                                        .getTimeBarEanbled(), false);
                int stepValue = getStepOptionValue();
                int targetDuration = mVideoView.getCurrentPosition()
                        + stepValue > mVideoView.getDuration() ? mVideoView
                        .getDuration() : mVideoView.getCurrentPosition()
                        + stepValue;
                MtkLog.v(TAG, "onForward targetDuration " + targetDuration);
                mVideoView.seekTo(targetDuration);
            } else {
                mControllerRewindAndForwardExt.showControllerButtonsView(
                        mPlayerExt.canStop(), mVideoView.canSeekBackward()
                                && mVideoView.getCurrentPosition() > 0
                                && mControllerRewindAndForwardExt
                                        .getTimeBarEanbled(), false);
            }
        }

        // add by junliang.liu.hz for CR604778 begin
        @Override
        public void onSeekBackward() {
            onSeekBackward();
        }

        @Override
        public void onSeekForward() {
            onSeekForward();
        }
        // add by junliang.liu.hz for CR604778 end
    }

    public int getStepOptionValue(){
        final String slectedStepOption = "selected_step_option";
        final String videoPlayerData = "video_player_data";
        final int stepBase = 3000;
        final String stepOptionThreeSeconds = "0";
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return (Integer.parseInt(mPrefs.getString(slectedStepOption, stepOptionThreeSeconds)) + 1)
                * stepBase;
    }
    
    private class MoviePlayerExtension implements IMoviePlayer, Restorable {
        private static final String KEY_VIDEO_IS_LOOP = "video_is_loop";
        
        private BookmarkEnhance mBookmark;//for bookmark
        private String mAuthor;//for detail
        private String mTitle;//for detail
        private String mCopyRight;//for detail
        private boolean mIsLoop;
        private boolean mLastPlaying;
        private boolean mLastCanPaused;
        private boolean mPauseBuffering;
        
        @Override
        public void stopVideo() {
            if (LOG) {
                MtkLog.v(TAG, "stopVideo()");
            }
            mTState = TState.STOPED;
            mVideoView.clearSeek();
            mVideoView.clearDuration();
            mVideoView.stopPlayback();
            mVideoView.setResumed(false);
            mVideoView.setVisibility(View.INVISIBLE);
            mVideoView.setVisibility(View.VISIBLE);
            clearVideoInfo();
            mFirstBePlayed = false;
            mController.setCanReplay(true);
            mController.showEnded();
            //resume review (play/pause asynchronous processing)
            mController.setViewEnabled(true);
            setProgress();
        }
        
        @Override
        public boolean canStop() {
            boolean stopped = false;
            if (mController != null) {
                stopped = mOverlayExt.isPlayingEnd();
            }
            if (LOG) {
                MtkLog.v(TAG, "canStop() stopped=" + stopped);
            }
            return !stopped;
        }

        @Override
        public void addBookmark() {
            if (mBookmark == null) {
                mBookmark = new BookmarkEnhance(mActivityContext);
            }
            String uri = String.valueOf(mMovieItem.getUri());
            if (mBookmark.exists(uri)) {
                Toast.makeText(mActivityContext, R.string.bookmark_exist, Toast.LENGTH_SHORT).show();
            } else {
                mBookmark.insert(mTitle, uri, mMovieItem.getMimeType(), 0);
                Toast.makeText(mActivityContext, R.string.bookmark_add_success, Toast.LENGTH_SHORT).show();
            }
            if (LOG) {
                MtkLog.v(TAG, "addBookmark() mTitle=" + mTitle + ", mUri=" + mMovieItem.getUri());
            }
        }

        @Override
        public boolean getLoop() {
            if (LOG) {
                MtkLog.v(TAG, "getLoop() return " + mIsLoop);
            }
            return mIsLoop;
        }

        @Override
        public void setLoop(boolean loop) {
            if (LOG) {
                MtkLog.v(TAG, "setLoop(" + loop + ") mIsLoop=" + mIsLoop);
            }
            if (isLocalFile()) {
                mIsLoop = loop;
                mController.setCanReplay(loop);
                mController.setLoopViewBackground(loop);
            }
        }

        @Override
        public void showDetail() {
            DetailDialog detailDialog = new DetailDialog(mActivityContext, mTitle, mAuthor, mCopyRight);
            detailDialog.setTitle(R.string.media_detail);
            detailDialog.setOnShowListener(new OnShowListener() {
                
                @Override
                public void onShow(DialogInterface dialog) {
                    if (LOG) {
                        MtkLog.v(TAG, "showDetail.onShow()");
                    }
                    pauseIfNeed();
                }
            });
            detailDialog.setOnDismissListener(new OnDismissListener() {
                
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (LOG) {
                        MtkLog.v(TAG, "showDetail.onDismiss()");
                    }
                    resumeIfNeed();
                }
            });
            detailDialog.show();
        }

        @Override
        public void startNextVideo(IMovieItem item) {
            IMovieItem next = item;
            if (next != null && next != mMovieItem) {
                int position = mVideoView.getCurrentPosition();
                int duration = mVideoView.getDuration();
                mBookmarker.setBookmark(mMovieItem.getUri(), position, duration);
                mVideoView.stopPlayback();
                mVideoView.setVisibility(View.INVISIBLE);
                clearVideoInfo();
                mMovieItem = next;
                mActivityContext.refreshMovieInfo(mMovieItem);
                startVideoCareDrm();
                mVideoView.setVisibility(View.VISIBLE);
            } else {
                MtkLog.e(TAG, "Cannot play the next video! " + item);
            }
            mActivityContext.closeOptionsMenu();
        }

        @Override
        public void onRestoreInstanceState(Bundle icicle) {
            mIsLoop = icicle.getBoolean(KEY_VIDEO_IS_LOOP, false);
            if (mIsLoop) {
                mController.setCanReplay(true);
            } // else  will get can replay from intent.
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putBoolean(KEY_VIDEO_IS_LOOP, mIsLoop);
        }

        public void onFirstShow(DialogInterface dialog) {
            pauseIfNeed();
            if (LOG) {
                MtkLog.v(TAG, "onFirstShow() mLastPlaying=" + mLastPlaying);
            }
        }
        
        public void onLastDismiss(DialogInterface dialog) {
            resumeIfNeed();
            if (LOG) {
                MtkLog.v(TAG, "onLastDismiss() mLastPlaying=" + mLastPlaying);
            }
        }
        
        private void pauseIfNeed() {
            mLastCanPaused = canStop() && mVideoView.canPause();
            if (mLastCanPaused) {
                mLastPlaying = (mTState == TState.PLAYING);
                mOverlayExt.clearBuffering();
                mPauseBuffering = true;
                //added by sima.chen for PR810665 begin
                mPlayPauseProcessExt.mPlayVideoWhenPaused = false;
                if(mVideoView.isCurrentPlaying()&&onIsRTSP()){
                   mPlayPauseProcessExt.mPauseSuccess = false;
                   mPlayPauseProcessExt.mNeedCheckPauseSuccess = true;
                  }
                //added by sima.chen for PR810665 end
                pauseVideo();
            }
            if (LOG) {
                MtkLog.v(TAG, "pauseIfNeed() mLastPlaying=" + mLastPlaying + ", mLastCanPaused=" + mLastCanPaused
                    + ", mPauseBuffering=" + mPauseBuffering);
            }
        }
        
        private void resumeIfNeed() {
            if (mLastCanPaused) {
                if (mLastPlaying) {
                    mPauseBuffering = false;
                    //modifid by sima.chen for PR810665 begin
                    //playVideo();
                    mPlayPauseProcessExt.CheckPauseSuccess();
                    //added by sima.chen for PR810665 end
                }
            }
            if (LOG) {
                MtkLog.v(TAG, "resumeIfNeed() mLastPlaying=" + mLastPlaying + ", mLastCanPaused=" + mLastCanPaused
                    + ", mPauseBuffering=" + mPauseBuffering);
            }
        }
        
        public boolean pauseBuffering() {
            return mPauseBuffering;
        }
        
        public void setVideoInfo(Metadata data) {
            if (data.has(Metadata.TITLE)) {
                mTitle = data.getString(Metadata.TITLE);
            }
            if (data.has(Metadata.AUTHOR)) {
                mAuthor = data.getString(Metadata.AUTHOR);
            }
            if (data.has(Metadata.COPYRIGHT)) {
                mCopyRight = data.getString(Metadata.COPYRIGHT);
            }
        }
    };
    
    //added by sima.chen for PR810665 begin
    /**
     * Play/pause asynchronous processing.
     */
    private class PlayPauseProcessExt implements MediaPlayer.OnInfoListener {
        public boolean mPauseSuccess = false;
        public boolean mNeedCheckPauseSuccess = false;
        public boolean mPlayVideoWhenPaused = false;

        /**
         * Check Pause is success or not. if success, it will start play, or
         * will start play when success is come in onInfo().
         */
        private void CheckPauseSuccess() {
            MtkLog.v(TAG, "CheckPauseSuccess() mNeedCheckPauseSuccess=" + mNeedCheckPauseSuccess
                    + ", mPauseSuccess=" + mPauseSuccess);
            if (mNeedCheckPauseSuccess == true) {
                if (mPauseSuccess == true) {
                    playVideo();
                    mPauseSuccess = false;
                    mNeedCheckPauseSuccess = false;
                } else {
                    mPlayVideoWhenPaused = true;
                    mController.setViewEnabled(false);
                }
            } else {
                playVideo();
            }
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_INFO_PAUSE_COMPLETED
                    || what == MediaPlayer.MEDIA_INFO_PLAY_COMPLETED) {
                MtkLog.v(TAG, "onInfo is PAUSE PLAY COMPLETED");
                if (extra == MediaPlayer.PAUSE_PLAY_SUCCEED) {
                    if (what == MediaPlayer.MEDIA_INFO_PAUSE_COMPLETED) {
                        handlePauseComplete();
                    }
                } else {
                	MtkLog.v(TAG, "onInfo is PAUSE PLAY COMPLETED extra111=" + extra);
                    if (extra != ERROR_INVALID_OPERATION && extra != ERROR_ALREADY_EXISTS && extra != -17) {
                    	MtkLog.v(TAG, "onInfo is PAUSE PLAY COMPLETED extra222=" + extra);
                        showNetWorkErrorDialog();
                    }
                }
                if (mVideoView.canPause()) {
                    mController.setViewEnabled(true);
                    updateRewindAndForwardUI();
                }
                return true;
            }
            return false;
        }

        /**
         * Judge if need play video in onInfo.
         */
        private void handlePauseComplete() {
            Log.v(TAG, "handlePauseComplete() mNeedCheckPauseSuccess=" + mNeedCheckPauseSuccess
                    + ", mPlayVideoWhenPaused=" + mPlayVideoWhenPaused);
            if (mNeedCheckPauseSuccess == true) {
                mPauseSuccess = true;
            }
            if (mPlayVideoWhenPaused == true) {
                mVideoView.start();
                mController.showPlaying();
                mPauseSuccess = false;
                mNeedCheckPauseSuccess = false;
                mPlayVideoWhenPaused = false;
            }
        }

        /**
         * Show dialog to user if play/pause is failed.Notify that only socket
         * error(except invalid operation and already exists error) will cause
         * network connection failed and should show the dialog.
         */
        private void showNetWorkErrorDialog() {
            final String errorDialogTag = "ERROR_DIALOG_TAG";
            FragmentManager fragmentManager = ((Activity) mActivityContext).getFragmentManager();
            DialogFragment fragment =
                    ErrorDialogFragment
                            .newInstance(R.string.VideoView_error_text_connection_failed);
            fragment.show(fragmentManager, errorDialogTag);
            fragmentManager.executePendingTransactions();
        }
    }
    //added by sima.chen for PR810665 end
    
    
    private class ServerTimeoutExtension implements Restorable, MediaPlayer.OnErrorListener {
        //for cmcc server timeout case 
        //please remember to clear this value when changed video.
        private int mServerTimeout = -1;
        private long mLastDisconnectTime;
        private boolean mIsShowDialog = false;
        private AlertDialog mServerTimeoutDialog;
        
        //check whether disconnect from server timeout or not.
        //if timeout, return false. otherwise, return true.
        private boolean passDisconnectCheck() {
            if (ExtensionHelper.getMovieStrategy(mActivityContext).shouldEnableServerTimeout()
                    && !isFullBuffer()) {
                //record the time disconnect from server
                long now = System.currentTimeMillis();
                if (LOG) {
                    MtkLog.v(TAG, "passDisconnectCheck() now=" + now + ", mLastDisconnectTime=" + mLastDisconnectTime
                            + ", mServerTimeout=" + mServerTimeout);
                }
                if (mServerTimeout > 0 && (now - mLastDisconnectTime) > mServerTimeout) {
                    //disconnect time more than server timeout, notify user
                    notifyServerTimeout();
                    return false;
                }
            }
            return true;
        }
        
        private void recordDisconnectTime() {
            if (ExtensionHelper.getMovieStrategy(mActivityContext).shouldEnableServerTimeout()
                    && !isFullBuffer()) {
                //record the time disconnect from server
                mLastDisconnectTime = System.currentTimeMillis();
            }
            if (LOG) {
                MtkLog.v(TAG, "recordDisconnectTime() mLastDisconnectTime=" + mLastDisconnectTime);
            }
        }
        
        private void clearServerInfo() {
            mServerTimeout = -1;
        }
        
        private void notifyServerTimeout() {
            if (mServerTimeoutDialog == null) {
                //for updating last position and duration.
                if (mVideoCanSeek || mVideoView.canSeekForward()) { 
                    mVideoView.seekTo(mVideoPosition);
                }
                mVideoView.setDuration(mVideoLastDuration);
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivityContext);
                mServerTimeoutDialog = builder.setTitle(R.string.server_timeout_title)
                    .setMessage(R.string.server_timeout_message)
                    .setNegativeButton(android.R.string.cancel, new OnClickListener() {
    
                        public void onClick(DialogInterface dialog, int which) {
                            if (LOG) {
                                MtkLog.v(TAG, "NegativeButton.onClick() mIsShowDialog=" + mIsShowDialog);
                            }
                            mController.showEnded();
                            onCompletion();
                        }
                        
                    })
                    .setPositiveButton(R.string.resume_playing_resume, new OnClickListener() {
        
                        public void onClick(DialogInterface dialog, int which) {
                            if (LOG) {
                                MtkLog.v(TAG, "PositiveButton.onClick() mIsShowDialog=" + mIsShowDialog);
                            }
                            doStartVideoCareDrm(true, mVideoPosition, mVideoLastDuration);
                        }
                        
                    })
                    .create();
                mServerTimeoutDialog.setOnDismissListener(new OnDismissListener() {
                        
                        public void onDismiss(DialogInterface dialog) {
                            if (LOG) {
                                MtkLog.v(TAG, "mServerTimeoutDialog.onDismiss()");
                            }
                            mIsShowDialog = false;
                        }
                        
                    });
                mServerTimeoutDialog.setOnShowListener(new OnShowListener() {
    
                        public void onShow(DialogInterface dialog) {
                            if (LOG) {
                                MtkLog.v(TAG, "mServerTimeoutDialog.onShow()");
                            }
                            mIsShowDialog = true;
                        }
                        
                    });
            }
            mServerTimeoutDialog.show();
        }
        
        private void clearTimeoutDialog() {
            if (mServerTimeoutDialog != null && mServerTimeoutDialog.isShowing()) {
                mServerTimeoutDialog.dismiss();
            }
            mServerTimeoutDialog = null;
        }

        @Override
        public void onRestoreInstanceState(Bundle icicle) {
            mLastDisconnectTime = icicle.getLong(KEY_VIDEO_LAST_DISCONNECT_TIME);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putLong(KEY_VIDEO_LAST_DISCONNECT_TIME, mLastDisconnectTime);
        }

        public boolean handleOnResume() {
            if (mIsShowDialog && !isLiveStreaming()) {
                //wait for user's operation
                return true;
            }
            if (!passDisconnectCheck()) {
                return true;
            }
            return false;
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            //if we are showing a dialog, cancel the error dialog
            if (mIsShowDialog) {
                return true;
            }
            return false;
        }
        
        public void setVideoInfo(Metadata data) {
            if (data.has(Metadata.SERVER_TIMEOUT)) {
                mServerTimeout = data.getInt(Metadata.SERVER_TIMEOUT);
                if (LOG) {
                    MtkLog.v(TAG, "get server timeout from metadata. mServerTimeout=" + mServerTimeout);
                }
            }
        }
    }
    
    /// M: fix hardware accelerated issue @{
    private static final int DELAY_REMOVE_MS = 10000;
    // Wait for any animation, ten seconds should be enough
    private final Runnable mRemoveBackground = new Runnable() {
        @Override
        public void run() {
            if (LOG) {
                MtkLog.v(TAG, "mRemoveBackground.run()");
            }
            mRootView.setBackground(null);
        }
    };
    private void removeBackground() {
        if (LOG) {
            MtkLog.v(TAG, "removeBackground()");
        }
        mHandler.removeCallbacks(mRemoveBackground);
        mHandler.postDelayed(mRemoveBackground, DELAY_REMOVE_MS);
    }
    
    // add background for removing ghost image.
    private void addBackground() {
        if (LOG) {
            MtkLog.v(TAG, "addBackground()");
        }
        mHandler.removeCallbacks(mRemoveBackground);
        mRootView.setBackgroundColor(Color.BLACK);
    }
    /// @}
    
    /// M: same as launch case to delay transparent. @{
    private Runnable mDelayVideoRunnable = new Runnable() {
        @Override
        public void run() {
            if (LOG) {
                MtkLog.v(TAG, "mDelayVideoRunnable.run()");
            }
            mVideoView.setVisibility(View.VISIBLE);
        }
    };
    /// @}
    
    /// M: when show resming dialog, suspend->wakeup, will play video. @{
    private boolean mIsShowResumingDialog;
    /// @}

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        setProgress();
    }
    //PR622918  by minrui.yan BEGIN
    //FR 576703 add by xiangchen BEGIN
    //PR493750-xiaobin-yang-001 begin
    private DrmManagerClient drmClient = null;
    /**
     * Create a DrmManagerClient Object in Singleton.
     * @author lei.liu@tct-nj.com
     * @param context
     * @return DrmManagerClient
     */
    private DrmManagerClient createSingletonDrmClient(Context context) {
        if(drmClient == null) {
            drmClient = DrmManagerClient.getInstance(context);
        }
        return drmClient;
    }
    //PR493750-xiaobin-yang-001 end
   //FR 576703 add by xiangchen END
    //PR622918  by minrui.yan END

    //add by junliang.liu.hz for CR604778 begin
    @Override
    public void onSeekBackward() {
        if (mVideoView.isPlaying() && (mVideoCanSeek || mVideoView.canSeekBackward())) {
            int curPosition = mVideoView.getCurrentPosition();
            int targetPosition = curPosition - 30000;//seekBackward 30s
        //modified by bing.wang.hz for FR540473 begin
        if (targetPosition >= 0) {
                mVideoView.seekTo(targetPosition);
                mShowWardNotifyHD.removeMessages(MSG_SHOW_NOTIFY_DLG);
                Message msg = new Message();
                msg.what = MSG_SHOW_NOTIFY_DLG;
                msg.arg1 = 1;
                mShowWardNotifyHD.sendMessage(msg);
            }
            /*else {
                mVideoView.seekTo(1000);
            }*/
            //modified by bing.wang.hz for FR540473 end
         }
    }

    @Override
    public void onSeekForward() {
        if (mVideoView.isPlaying() && (mVideoCanSeek || mVideoView.canSeekForward())) {
            int curPosition = mVideoView.getCurrentPosition();
            int videoDuration = mVideoView.getDuration();
            int targetPosition = curPosition + 30000;//seekForward 30s
        //modified by bing.wang.hz for FR540473 begin
        if (targetPosition <= videoDuration) {
                mVideoView.seekTo(targetPosition);
            mShowWardNotifyHD.removeMessages(MSG_SHOW_NOTIFY_DLG);
                Message msg = new Message();
                msg.what = MSG_SHOW_NOTIFY_DLG;
                msg.arg1 = 0;
                mShowWardNotifyHD.sendMessage(msg);
            }
            /*else {
                mVideoView.seekTo(videoDuration-1000);
            }*/
            //modified by bing.wang.hz for FR540473 end
         }
    }
    //add by junliang.liu.hz for CR604778 end
}

class Bookmarker {
    private static final String TAG = "Bookmarker";

    private static final String BOOKMARK_CACHE_FILE = "bookmark";
    private static final int BOOKMARK_CACHE_MAX_ENTRIES = 100;
    private static final int BOOKMARK_CACHE_MAX_BYTES = 10 * 1024;
    private static final int BOOKMARK_CACHE_VERSION = 1;

    private static final int HALF_MINUTE = 30 * 1000;
    private static final int TWO_MINUTES = 4 * HALF_MINUTE;

    private final Context mContext;

    public Bookmarker(Context context) {
        mContext = context;
    }

    public void setBookmark(Uri uri, int bookmark, int duration) {
        if (LOG) {
            MtkLog.v(TAG, "setBookmark(" + uri + ", " + bookmark + ", " + duration + ")");
        }
        try {
            //do not record or override bookmark if duration is not valid.
            if (duration <= 0) {
                return;
            }
            BlobCache cache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeUTF(uri.toString());
            dos.writeInt(bookmark);
            dos.writeInt(Math.abs(duration));
            dos.flush();
            cache.insert(uri.hashCode(), bos.toByteArray());
        } catch (Throwable t) {
            Log.w(TAG, "setBookmark failed", t);
        }
    }

    public BookmarkerInfo getBookmark(Uri uri) {
        try {
            BlobCache cache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE, BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES, BOOKMARK_CACHE_VERSION);

            byte[] data = cache.lookup(uri.hashCode());
            if (data == null) {
                if (LOG) {
                    MtkLog.v(TAG, "getBookmark(" + uri + ") data=null. uri.hashCode()=" + uri.hashCode());
                }
                return null;
            }

            DataInputStream dis = new DataInputStream(
                    new ByteArrayInputStream(data));

            String uriString = DataInputStream.readUTF(dis);
            int bookmark = dis.readInt();
            int duration = dis.readInt();
            if (LOG) {
                MtkLog.v(TAG, "getBookmark(" + uri + ") uriString=" + uriString + ", bookmark=" + bookmark
                        + ", duration=" + duration);
            }
            if (!uriString.equals(uri.toString())) {
                return null;
            }

            if ((bookmark < HALF_MINUTE) || (duration < TWO_MINUTES)
                    || (bookmark > (duration - HALF_MINUTE))) {
                return null;
            }
            return new BookmarkerInfo(bookmark, duration);
        } catch (Throwable t) {
            Log.w(TAG, "getBookmark failed", t);
        }
        return null;
    }
    
    private static final boolean LOG = true;
}

class BookmarkerInfo {
    public final int mBookmark;
    public final int mDuration;
    
    public BookmarkerInfo(int bookmark, int duration) {
        this.mBookmark = bookmark;
        this.mDuration = duration;
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
        .append("BookmarkInfo(bookmark=")
        .append(mBookmark)
        .append(", duration=")
        .append(mDuration)
        .append(")")
        .toString();
    }
}
