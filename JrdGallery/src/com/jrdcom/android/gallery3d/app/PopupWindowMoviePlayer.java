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
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.Metadata;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.common.ApiHelper;
import com.jrdcom.android.gallery3d.common.BlobCache;
import com.jrdcom.android.gallery3d.ui.PopupWindowFrameLayout;
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

public class PopupWindowMoviePlayer implements
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        ControllerOverlay.Listener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnVideoSizeChangedListener{
    @SuppressWarnings("unused")
    private static final String TAG = "PopupWindowMoviePlayerMoviePlayer";

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

    // If we resume the acitivty with in RESUMEABLE_TIMEOUT, we will keep playing.
    // Otherwise, we pause the player.
    private static final long RESUMEABLE_TIMEOUT = 3 * 60 * 1000; // 3 mins

    private Context mContext;
    private final MTKVideoView mVideoView;
    //private final FrameLayout cancelIcon;//del by qjz for some issue 20130730
    private final View mRootView;
    //private final Bookmarker mBookmarker;
    //private Uri mUri;
    private final Handler mHandler = new Handler();
    //private final AudioBecomingNoisyReceiver mAudioBecomingNoisyReceiver;
   // private final MovieControllerOverlay mController;

    private long mResumeableTime = Long.MAX_VALUE;
    private int mVideoPosition = 0;
    private boolean mHasPaused = false;
    private int mLastSystemUiVis = 0;

    // If the time bar is being dragged.
    private boolean mDragging;

    // If the time bar is visible.
    private boolean mShowing;

    //add by qjz 2013-03-15 begin
    private long mSeekTo;
    private final int MSG_UPDATE_SEEK_FRAME = 109;
    private static boolean isSeek = false;
    private Bitmap mBitmap;//modified by qjz for NewVedioPlayerUI 20130405 
    private int mSeekTime;//modified by qjz for NewVedioPlayerUI 20130405 
    //private Bitmap [] mBitmap= new Bitmap[9];//del by qjz for NewVedioPlayerUI 20130405 
    //add by qjz 2013-03-15 end

    private RelativeLayout mLoadingIcon;
    
    private ImageView mAudioBG;// add by yaping.liu for pr500275
    
    private final Runnable mPlayingChecker = new Runnable() {
        @Override
        public void run() {
            boolean isplaying = mVideoView.isPlaying();
            if (LOG) {
                MtkLog.v(TAG, "mPlayingChecker.run() isplaying=" + isplaying);
            }
            if (isplaying) {
                //mController.showPlaying();
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };
    //add by qjz for NewUI 2013-03-19
/*    private Handler myHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch(msg.what) {
             case MSG_UPDATE_SEEK_FRAME:
                 mController.setShowSeekToBackGround(mBitmap,(int)(videoDuration/1000),msg.arg1,getCurrentPosition(msg.arg1));
                 break;
             default:
                 break;
             }
        }
    };*/
/*    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
        }
    };*/

    public PopupWindowMoviePlayer(View rootView, final PopupWindowMovieActivity pMovieActivity, IMovieItem info, boolean canReplay, int position) {
        Log.i(TAG, "PopupWindowMoviePlayer constuct");
        mContext = pMovieActivity.getApplicationContext();
        mRootView = rootView;
        mVideoView = (MTKVideoView) rootView.findViewById(R.id.surface_view_popup);
        //cancelIcon = (FrameLayout) rootView.findViewById(R.id.cancel_icon); //del by qjz for some issue 20130730
        mLoadingIcon = (RelativeLayout) rootView.findViewById(R.id.loading_icon);
        mAudioBG = (ImageView)rootView.findViewById(R.id.audio_bg);// add by yaping.liu for pr500275
        mAudioBG.setVisibility(View.GONE);// add by yaping.liu for pr500275
        init(pMovieActivity, info, canReplay);
        
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //mController.show();//add by yanlong
                Log.i(TAG, "PopupWindowMoviePlayer mVideoView onTouch event type: "+event.getAction());
                mRootView.onTouchEvent(event);
                return true;
            }
        });
        //add by qjz for PR480767 begin
        if (mStreamingType == STREAMING_LOCAL) {
            mLoadingIcon.setBackgroundResource(android.R.color.transparent);// add by yaping.liu for pr507714
            mLoadingIcon.setVisibility(View.GONE);
        }
        //add by qjz for PR480767 end
        //del by qjz for some issue 20130730 begin
        /*cancelIcon.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mActivityContext.finishPopupWindow();
                
            }
        });*/
        //del by qjz for some issue 20130730 end

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
        
            mTState = TState.PLAYING;
            mFirstBePlayed = true;
            mVideoCanSeek = true;
            doStartVideo(false, position, 0);//start to play
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
        mLoadingIcon.setVisibility(View.GONE);// add by yaping.liu for pr499606
        long start = System.currentTimeMillis();
        //addBackground();
        mHasPaused = true;
        mHandler.removeCallbacksAndMessages(null);
        int position = mVideoView.getCurrentPosition();
        mVideoPosition = position >= 0 ? position : mVideoPosition;
        MtkLog.v(TAG, "mVideoPosition is " + mVideoPosition);
        int duration = mVideoView.getDuration();
        mVideoLastDuration = duration > 0 ? duration : mVideoLastDuration;
        long end1 = System.currentTimeMillis();
        mVideoView.stopPlayback();//change suspend to release for sync paused and killed case
        mResumeableTime = System.currentTimeMillis() + RESUMEABLE_TIMEOUT;
        
        long end2 = System.currentTimeMillis();
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
                //mController.showEnded();
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
        //mHandler.post(mProgressChecker);
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
        mVideoView.stopPlayback();
        //mAudioBecomingNoisyReceiver.unregister();
        mServerTimeoutExt.clearTimeoutDialog();
    }

  
    private void doStartVideo(final boolean enableFasten, final int position, final int duration, boolean start) {
        // add start by yaping.liu for pr499606
        if (start) {
            mLoadingIcon.setVisibility(View.VISIBLE);
            if (position > 0 && duration > 0) {
                mLoadingIcon.setBackgroundResource(android.R.color.transparent);
            }
        }
        // add end by yaping.liu for pr499606
        if (LOG) {
            MtkLog.v(TAG, "doStartVideo(" + enableFasten + ", " + position + ", " + duration + ", " + start + ")");
        }
        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        String scheme = mMovieItem.getUri().getScheme();
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)
                || "https".equalsIgnoreCase(scheme)) {
            //mController.showLoading();
            //mOverlayExt.setPlayingInfo(isLiveStreaming());
            mHandler.removeCallbacks(mPlayingChecker);
            mHandler.postDelayed(mPlayingChecker, 250);
        } else {
            //mController.showPlaying();
           // mController.hide();
        }
        /// M: add play/pause asynchronous processing @{
        //modified by qjz for PR480767 begin
        if(onIsRTSP()){// something wrong
            Map<String, String> header = new HashMap<String, String>(1);
            header.put("MTK-ASYNC-RTSP-PAUSE-PLAY", "true");
            mVideoView.setVideoURI(mMovieItem.getUri(), header/*, !mWaitMetaData*/);//PR593460 wanwan.ye(porting by liqiang for PR595899)
            //setMediaMetadataSource(mMovieItem.getUri(), header);//add by qjz for NewUI 2013-03-19 no support RTSP
        } else {
            mVideoView.setVideoURI(mMovieItem.getUri(), null/*, !mWaitMetaData*/);//PR593460 wanwan.ye(porting by liqiang for PR595899)
            //setMediaMetadataSource(mMovieItem.getUri(), null);//add by qjz for NewUI 2013-03-19
        }
        //modified by qjz for PR480767 end
        if (start) {
            mVideoView.start();
        }
        /// @}
        //we may start video from stopVideo,
        //this case, we should reset canReplay flag according canReplay and loop
        //boolean loop = mPlayerExt.getLoop();
        //boolean canReplay = loop ? loop : mCanReplay;
        //mController.setCanReplay(canReplay);
        Log.i(TAG, "position: "+position+" mVideoCanSeek: "+mVideoCanSeek+" mVideoView.canSeekForward() "+mVideoView.canSeekForward());
        if (position > 0 && (mVideoCanSeek || mVideoView.canSeekForward())) {
            mVideoView.seekTo(position);
        }
        if (enableFasten) {
            mVideoView.setDuration(duration);
        }
       // setProgress();
    }
    //PR481516:[Popup video]The popup video and music can play together. 
    //add by qjz 20130709 begin
    OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                if(isPlaying()){
                    onPause();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                if(isPlaying()){
                    onStop();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // Lower the volume
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback or Raise it back to normal
            }
        }
    };
    //PR481516:[Popup video]The popup video and music can play together. 
    //add by qjz 20130709 begin

    private void doStartVideo(boolean enableFasten, int position, int duration) {
        ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(
                mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,//modified by qjz for PR481516 20130709
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        doStartVideo(enableFasten, position, duration, true);
    }
    
    private void playVideo() {
        if (LOG) {
            MtkLog.v(TAG, "playVideo()");
        }
        mTState = TState.PLAYING;
        mVideoView.start();
    }

    private void pauseVideo() {
        if (LOG) {
            MtkLog.v(TAG, "pauseVideo()");
        }
        mTState = TState.PAUSED;
        mVideoView.pause();
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
        //mHandler.post(mProgressChecker);//always show progress
        // VideoView will show an error dialog if we return false, so no need
        // to show more message.
        //M:resume controller
        //mController.setViewEnabled(true);
        //mController.showErrorMessage("");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (LOG) {
            MtkLog.v(TAG, "onCompletion() mCanReplay=" + mCanReplay);
        }
        if (mMovieItem.getError()) {
            MtkLog.w(TAG, "error occured, exit the video player!");
            //mActivityContext.finish();
            mActivityContext.stopSelf();
            return;
        }
            mTState = TState.COMPELTED;
            onCompletion();
        }
    
    public void onCompletion() {
    }
    
    public boolean isPlaying(){
        return mVideoView.isPlaying();
    }

    
    public void updateRewindAndForwardUI(){
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
    private PopupWindowMovieActivity mActivityContext;//for dialog and toast context
    private boolean mFirstBePlayed = false;//for toast more info

    private void init(final PopupWindowMovieActivity movieActivity, IMovieItem info, boolean canReplay) {
        mActivityContext = movieActivity;
        mCanReplay = canReplay;
        mMovieItem = info;
        judgeStreamingType(info.getUri(), info.getMimeType());
        
        //for toast more info and live streaming
        mVideoView.setStreamingType(mStreamingType == STREAMING_LOCAL);//PR490956 set the StreamingType 20130724
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnBufferingUpdateListener(this);
        mVideoView.setOnVideoSizeChangedListener(this);
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

    private void onRestoreInstanceState(Bundle icicle) {
        mVideoLastDuration = icicle.getInt(KEY_VIDEO_LAST_DURATION);
        mVideoCanPause = icicle.getBoolean(KEY_VIDEO_CAN_PAUSE);
        mVideoCanSeek = icicle.getBoolean(KEY_VIDEO_CAN_SEEK);
        mConsumedDrmRight = icicle.getBoolean(KEY_CONSUMED_DRM_RIGHT);
        mStreamingType = icicle.getInt(KEY_VIDEO_STREAMING_TYPE);
        mTState = TState.valueOf(icicle.getString(KEY_VIDEO_STATE));
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
           // mOverlayExt.setPlayingInfo(isLiveStreaming());
        }
        boolean canPause = mVideoView.canPause();
        boolean canSeek = mVideoView.canSeekBackward() && mVideoView.canSeekForward();
        if (!canPause && !mVideoView.isTargetPlaying()) {
            mVideoView.start();
        }

        updateRewindAndForwardUI();
        if (LOG) {
            MtkLog.v(TAG, "onPrepared() canPause=" + canPause + ", canSeek=" + canSeek);
        }

        // add start by yaping.liu for pr512066
        if (mStreamingType == STREAMING_LOCAL && mIsOnlyAudio) {
            mLoadingIcon.setVisibility(View.GONE);
        }
        // add end by yaping.liu for pr512066
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //add start by yaping.liu for PR503960
        if (what == MediaPlayer.MEDIA_INFO_GET_BUFFER_DATA
                || what == MediaPlayer.MEDIA_INFO_BUFFERING_END
                || what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START
                || mStreamingType == STREAMING_LOCAL) {
            mLoadingIcon.setVisibility(View.GONE);
        }
        //add end by yaping.liu for PR503960

        if (LOG) {
            MtkLog.v(TAG, "onInfo() what:" + what + " extra:" + extra);
        }
        if (mRetryExt.onInfo(mp, what, extra)) {
            return true;
        }
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
            //mOverlayExt.showBuffering(fullBuffer, percent);
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
            mAudioBG.setVisibility(View.GONE);// add by yaping.liu for pr500275
            mActivityContext.rescalePopWindowSize(width,height);            
        } else {
            mIsOnlyAudio = true;
            mAudioBG.setVisibility(View.VISIBLE);// add by yaping.liu for pr500275
        }
        //mOverlayExt.setBottomPanel(mIsOnlyAudio, true);
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
    //private ScreenModeExt mScreenModeExt = new ScreenModeExt();
    private ServerTimeoutExtension mServerTimeoutExt = new ServerTimeoutExtension();
    private MoviePlayerExtension mPlayerExt = new MoviePlayerExtension();

    private class RetryExtension implements MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener {
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
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_ERROR_CANNOT_CONNECT_TO_SERVER) {
                //get the last position for retry
                mRetryPosition = mVideoView.getCurrentPosition();
                mRetryDuration = mVideoView.getDuration();
                mRetryCount++;
                if (reachRetryCount()) {
                    mTState = TState.RETRY_ERROR;
                    //mOverlayExt.showReconnectingError();
                } else {
                    //mOverlayExt.showReconnecting(mRetryCount);
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
               // mController.setViewEnabled(true);
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
            //mOverlayExt.showReconnectingError();
            if (mVideoCanSeek || mVideoView.canSeekForward()) { 
                mVideoView.seekTo(mVideoPosition);
            }
            mVideoView.setDuration(mVideoLastDuration);
            mRetryPosition = mVideoPosition;
            mRetryDuration = mVideoLastDuration;
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
                /*mControllerRewindAndForwardExt.showControllerButtonsView(false,
                        false, false);*/
            }
        }
        @Override
        public void onRewind() {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt onRewind()");
            if (mVideoView != null && mVideoView.canSeekBackward()) {
                int stepValue = getStepOptionValue();
                int targetDuration = mVideoView.getCurrentPosition()
                        - stepValue < 0 ? 0 : mVideoView.getCurrentPosition()
                        - stepValue;
                MtkLog.v(TAG, "onRewind targetDuration " + targetDuration);
                mVideoView.seekTo(targetDuration);
            } else {
            }
        }
        @Override
        public void onForward() {
            MtkLog.v(TAG, "ControllerRewindAndForwardExt onForward()");
            if (mVideoView != null && mVideoView.canSeekForward()) {
                int stepValue = getStepOptionValue();
                int targetDuration = mVideoView.getCurrentPosition()
                        + stepValue > mVideoView.getDuration() ? mVideoView
                        .getDuration() : mVideoView.getCurrentPosition()
                        + stepValue;
                MtkLog.v(TAG, "onForward targetDuration " + targetDuration);
                mVideoView.seekTo(targetDuration);
            } else {
            }
        }
        // add by junliang.liu.hz for CR604778 begin
        @Override
        public void onSeekBackward() {
            // TODO Auto-generated method stub
        }
        @Override
        public void onSeekForward() {
            // TODO Auto-generated method stub
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
    
    private class MoviePlayerExtension implements IMoviePlayer {
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
        }
        
        @Override
        public boolean canStop() {
            boolean stopped = false;
       /*     if (mController != null) {
                //stopped = mOverlayExt.isPlayingEnd();
            }*/
            if (LOG) {
                MtkLog.v(TAG, "canStop() stopped=" + stopped);
            }
            return !stopped;
        }
        
        @Override
        public void addBookmark() {
            
        }
        
        @Override
        public boolean getLoop() {
            return false;
        }

        @Override
        public void setLoop(boolean loop) {
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
                //mBookmarker.setBookmark(mMovieItem.getUri(), position, duration);
                mVideoView.stopPlayback();
                mVideoView.setVisibility(View.INVISIBLE);
                clearVideoInfo();
                mMovieItem = next;
               // mActivityContext.refreshMovieInfo(mMovieItem);
                startVideoCareDrm();
                mVideoView.setVisibility(View.VISIBLE);
            } else {
                MtkLog.e(TAG, "Cannot play the next video! " + item);
            }
            //mActivityContext.closeOptionsMenu();
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
               // mOverlayExt.clearBuffering();
                mPauseBuffering = true;
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
                    playVideo();
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
    
    private class ServerTimeoutExtension implements MediaPlayer.OnErrorListener {
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
                          //  mController.showEnded();
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
    //modified by qjz for PR487297 begin
    public void returnToNormalWindow(boolean isTreatUpAsBack, boolean logo) {
        // TODO Auto-generated method stub
         Intent intent =new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(mMovieItem.getOriginalUri(), mMovieItem.getMimeType());
        intent.putExtra(MovieActivity.KEY_TREAT_UP_AS_BACK, isTreatUpAsBack);
        if (logo) {
            intent.putExtra(MovieActivity.KEY_LOGO_BITMAP, BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.video_app));
        }
        Bundle bundle=new Bundle();
        mVideoView.clearSeek();//add by qjz for PR481448 20130704
        bundle.putInt("position", mVideoView.getCurrentPosition());
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivityContext.startActivity(intent);
        mActivityContext.finishPopupWindow();
    }
    //modified by qjz for PR487297 end

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

    @Override
    public void onPlayPause() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onSeekStart() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onSeekMove(int time) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onSeekEnd(int time, int trimStartTime, int trimEndTime) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onShown() {
        // TODO Auto-generated method stub
        addBackground();
        showSystemUi(true);
    }


    @Override
    public void onHidden() {
        // TODO Auto-generated method stub
        showSystemUi(false);
        removeBackground();
    }


    @Override
    public void onReplay() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onChangeLoopMode() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onShowSeekToView(int seek) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onSetSeekViewShow(boolean isShow) {
        // TODO Auto-generated method stub

    }

    //add by junliang.liu.hz for CR604778 begin
    @Override
    public void onSeekBackward() {
        // TODO Auto-generated method stub
    }
    @Override
    public void onSeekForward() {
        // TODO Auto-generated method stub
    }
    //add by junliang.liu.hz for CR604778 end
}