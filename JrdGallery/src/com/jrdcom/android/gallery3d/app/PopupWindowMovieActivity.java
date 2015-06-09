/*
 * Copyright (C) 2007 The Android Open Source Project
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
import android.app.ActionBar;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ActivityChooserView;
import android.widget.FrameLayout;
import android.widget.ShareActionProvider;

import android.widget.Toast;
import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.common.ApiHelper;
import com.jrdcom.android.gallery3d.common.Utils;
import com.jrdcom.android.gallery3d.ui.PopupWindowFrameLayout;

import com.jrdcom.android.gallery3d.app.MoviePlayer;
import com.jrdcom.mediatek.gallery3d.ext.IActivityHooker;
import com.jrdcom.mediatek.gallery3d.ext.IMovieItem;
import com.jrdcom.mediatek.gallery3d.ext.MovieItem;
import com.jrdcom.mediatek.gallery3d.ext.MovieUtils;
import com.jrdcom.mediatek.gallery3d.ext.MtkLog;
import com.jrdcom.mediatek.gallery3d.util.MtkUtils;
import com.jrdcom.mediatek.gallery3d.video.ExtensionHelper;
import com.jrdcom.mediatek.gallery3d.video.MovieTitleHelper;

/**
 * This activity plays a video from a specified URI.
 * 
 * The client of this activity can pass a logo bitmap in the intent (KEY_LOGO_BITMAP)
 * to set the action bar logo so the playback process looks more seamlessly integrated with
 * the original activity.
 */
public class PopupWindowMovieActivity extends Service {
    @SuppressWarnings("unused")
    private static final String TAG = "PopupWindowMovieActivity";
    public static final String KEY_LOGO_BITMAP = "logo-bitmap";
    public static final String KEY_TREAT_UP_AS_BACK = "treat-up-as-back";

    private PopupWindowMoviePlayer mPlayer;
    private boolean mFinishOnCompletion;
    //private Uri mUri;
    private boolean mTreatUpAsBack;
    ///M: add for NFC
    private boolean mBeamVideoIsPlaying = false;
    
    WindowManager wm;
    PopupWindowFrameLayout popupFrameLayout;
    View rootView;
    private int displayWidth;
    private int displayHeight;
    private static final String PHONE_STATE_ACTION="android.intent.action.PHONE_STATE";
    private static final String CAMERA_OPEN = "com.jrdcom.CAMERA_OPEN";
    private static final String ALARM_ALERT = "com.android.deskclock.ALARM_ALERT";//add by qjz for PR485930 2013-07-10 
    private static final int MSG_REGISTER_RECEIVER = 1101;
    TelephonyManager tm;
    private int lastCallState = TelephonyManager.CALL_STATE_IDLE;
    
    private int mOrientation;
    private boolean mIsCalling = false;
    private boolean mIsReset = false;
    private boolean mLogo;//add by qjz for PR487297

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;
    }

    //PR485896:Play the video which is recording with popip video ,happen force close.
    //add by qjz 20130709 begin
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_REGISTER_RECEIVER:
                IntentFilter filter = new IntentFilter();
                filter.addAction(CAMERA_OPEN);
                filter.addAction(ALARM_ALERT);//add by qjz for PR485930 2013-07-10 
                PopupWindowMovieActivity.this.registerReceiver(phoneStateReceiver, filter);
                break;
            default:
                break;
            }
        }
    };
    //PR485896:Play the video which is recording with popip video ,happen force close.
    //add by qjz 20130709 begin
    @Override
    public void onCreate() {
        MtkLog.v(TAG, "onCreate()");
        popupFrameLayout=(PopupWindowFrameLayout)PopupWindowFrameLayout.inflate(this,R.layout.popup_video_window,null);
        wm=(WindowManager)this.getApplicationContext().getSystemService("window");  
        WindowManager.LayoutParams wmParams =((GalleryAppImpl)this.getApplicationContext()).getMywmParams();
        wmParams.type=LayoutParams.TYPE_PHONE;//2002;
        wmParams.format=PixelFormat.RGBA_8888;//1;
        wmParams.flags|= LayoutParams.FLAG_NOT_FOCUSABLE;       
        //display size
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        displayWidth = dm.widthPixels;//1080
        displayHeight = dm.heightPixels;//1920
        //Log.i("yanlong", " init displaywidth:  "+displayWidth+"  displayHeight: "+displayHeight);
        // modify start by yaping.liu for pr504624
        if (displayHeight > displayWidth) {
            wmParams.width = (int) displayWidth / 2;
            wmParams.height = (int) displayWidth / 2;
        } else {
            wmParams.width = (int) displayHeight / 2;
            wmParams.height = (int) displayHeight / 2;
        }
        // modify end by yaping.liu for pr504624
        wm.addView(popupFrameLayout, wmParams);
        //register intent filter
        //PR485896:Play the video which is recording with popip video ,happen force close.
        //modified by qjz 20130709 begin
        /*IntentFilter filter = new IntentFilter();
        filter.addAction(CAMERA_OPEN);
        this.registerReceiver(phoneStateReceiver, filter);*/
        mHandler.sendEmptyMessageDelayed(MSG_REGISTER_RECEIVER, 500);
        //PR485896:Play the video which is recording with popip video ,happen force close.
        //modified by qjz 20130709 begin

        tm = (TelephonyManager)getSystemService(Service.TELEPHONY_SERVICE); 
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        //add by qjz for some issue 20130730 begin
        rootView = popupFrameLayout.findViewById(R.id.popup_root);
        FrameLayout cancelIcon = (FrameLayout) rootView.findViewById(R.id.cancel_icon);
        cancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finishPopupWindow();
            }
        });
        //add by qjz for some issue 20130730 end
        this.initOrientation();
        //PR481505:The popup video and video can play together.
        //add by qjz 20130708 begin
        //del by qjz for PR495700:It will occur the force close prompt when play popup video 20130729
        /*mIsReset = false;
        SharedPreferences preferences = this.getSharedPreferences("com.jrdcom.android.gallery3d_preferences", 0);
        SharedPreferences.Editor localEditor = preferences.edit();
        localEditor.putBoolean("isStartPopupVideo", true);
        localEditor.commit();*/
        //PR481505:The popup video and video can play together.
        //add by qjz 20130708 end
    }
    private BroadcastReceiver phoneStateReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, final Intent intent) {
        	//PR-549939 added by xiaowei.xu begin
        	if(mPlayer == null){
        		return;
        	}
        	//PR-549939 added by xiaowei.xu end
            if (CAMERA_OPEN.equals(intent.getAction())) {
                if(mPlayer.isPlaying()){
                    mPlayer.onStop();
                }
                finishPopupWindow();
            //modified by qjz for PR485930 2013-07-10 begin
            } else if(ALARM_ALERT.equals(intent.getAction())) {
                if(mPlayer.isPlaying()){
                    mPlayer.onStop();
                }
//                finishPopupWindow();// delete by yaping.liu for pr500209
            }
            //modified by qjz for PR485930 2013-07-10 end
        }
    };
    
    PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                mIsCalling = false;
                // hang up
                // Log.i("yanlong","onCallStateChanged: "+tm.getCallState());
                // modified by qjz for PR481424 20130703 begin
                /*
                 * if(lastCallState!=TelephonyManager.CALL_STATE_IDLE
                 * &&tm.getCallState()==TelephonyManager.CALL_STATE_IDLE){
                 * lastCallState=TelephonyManager.CALL_STATE_IDLE;
                 * popupFrameLayout.singleClick(); }
                 */
                // modified by qjz for PR481424 20130703 end
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                // listen
                // break;
            case TelephonyManager.CALL_STATE_RINGING:
                // ringing
                // Log.i("yanlong","onCallStateChanged: "+tm.getCallState());
                // modified by qjz for PR481424 20130703 begin
                /*
                 * if(lastCallState!=TelephonyManager.CALL_STATE_RINGING
                 * &&tm.getCallState()==TelephonyManager.CALL_STATE_RINGING){
                 * lastCallState=TelephonyManager.CALL_STATE_RINGING;
                 * popupFrameLayout.singleClick(); }
                 */
                mIsCalling = true;
                if (mPlayer.isPlaying()) {
                    mPlayer.onPause();
                }
                // modified by qjz for PR481424 20130703 end
                break;
            }
        }
    };

    @Override
    public void onStart(Intent intent, int startid) {
       // super.onStart(intent,startid);
        if(intent==null) return;
        String action = intent.getAction();
        //modified by qjz for PR481424 20130705 begin
        if("com.jrdcom.action.CHANGE_PLAY_STATUS".equals(action)){
            if (!mIsCalling) {
                if(mPlayer!=null){
                    if(mPlayer.isPlaying()){
                        mPlayer.onPause();
                    }else{
                        mPlayer.onResume();
                    }
                }
            } else {
                Toast.makeText(this, this.getApplicationContext().getResources().getString(R.string.unable_to_play), 
                        Toast.LENGTH_SHORT).show();
            }
        }else if("com.jrdcom.action.BACK_TO_NORMAL_WINDOW".equals(action)){
            //PR481505:The popup video and video can play together.
            //add by qjz 20130715 begin
            //del by qjz for PR495700:It will occur the force close prompt when play popup video 20130729
            /*SharedPreferences preferences = this.getSharedPreferences("com.jrdcom.android.gallery3d_preferences", 0);
            SharedPreferences.Editor localEditor = preferences.edit();
            localEditor.putBoolean("isStartPopupVideo", false);
            localEditor.commit();
            mIsReset = true;*/
            //PR481505:The popup video and video can play together.
            //add by qjz 20130715 end
            if (!mIsCalling) {
                if(mPlayer!=null){
                    mPlayer.returnToNormalWindow(mTreatUpAsBack, mLogo);//modified by qjz for PR487297
                }
            } else {
                Toast.makeText(this, this.getApplicationContext().getResources().getString(R.string.unable_to_play), 
                        Toast.LENGTH_SHORT).show();
                finishPopupWindow();
            }
            //modified by qjz for PR495700:It will occur the force close prompt when play popup video 20130729 begin
            }else if ("com.jrdcom.action.CLOSE_POPUP_WINDOW".equals(action)) {
                finishPopupWindow();
            //modified by qjz for PR495700:It will occur the force close prompt when play popup video 20130729 end
        }else{
        //modified by qjz for PR481424 20130705 end
        initMovieInfo(intent);
        int currentPosition =intent.getExtras().getInt("position");
        //rootView = popupFrameLayout.findViewById(R.id.popup_root);//del by qjz for some issue 20130730 begin
        mFinishOnCompletion = intent.getBooleanExtra(
                MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        mTreatUpAsBack = intent.getBooleanExtra(KEY_TREAT_UP_AS_BACK, false);
        mLogo = intent.getBooleanExtra(KEY_LOGO_BITMAP, false);
        mPlayer = new PopupWindowMoviePlayer(rootView, this, mMovieItem, !mFinishOnCompletion,currentPosition){
            @Override
            public void onCompletion() {
                if (LOG) {
                    MtkLog.v(TAG, "onCompletion() mFinishOnCompletion=" + mFinishOnCompletion);
                }
                if (mFinishOnCompletion) {
                    //finish();
                    popupFrameLayout.setEnabled(false);
                    wm.removeView(popupFrameLayout);
                    stopSelf();
                }
            }
        };

        registerScreenOff();
        if (LOG) {
            MtkLog.v(TAG, "onStart()");
        }
        }
        //PR481505:The popup video and video can play together.
        //add by qjz 20130708 begin
        /*SharedPreferences preferences = this.getSharedPreferences("com.jrdcom.android.gallery3d_preferences", 0);
        SharedPreferences.Editor localEditor = preferences.edit();
        localEditor.putBoolean("isStartPopupVideo", true);
        localEditor.commit();*/
        //PR481505:The popup video and video can play together.
        //add by qjz 20130708 end
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
           super.onConfigurationChanged(newConfig);
           int orientation=Configuration.ORIENTATION_LANDSCAPE;
           if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
               //landscape
               
           }else if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
               //portrait
               orientation=Configuration.ORIENTATION_PORTRAIT;
           }
           mOrientation=orientation;
           
           popupFrameLayout.informOrientationChanged(mOrientation);
    }

    @Override
    public void onDestroy() {
        //add by qjz for some issue 20130730 begin
        if (mPlayer != null) {
            mPlayer.onDestroy();
        }
        //add by qjz for some issue 20130730 end
        //PR481505:The popup video and video can play together.
        //add by qjz 20130708 begin
        this.unregisterReceiver(phoneStateReceiver);
        //del by qjz for PR495700:It will occur the force close prompt when play popup video 20130729
        /*if (!mIsReset) {
	        SharedPreferences preferences = this.getSharedPreferences("com.jrdcom.android.gallery3d_preferences", 0);
	        SharedPreferences.Editor localEditor = preferences.edit();
	        localEditor.putBoolean("isStartPopupVideo", false);
	        localEditor.commit();
        }*/
        //PR481505:The popup video and video can play together.
        //add by qjz 20130708 end
        //PR817483 PhoneStateListener is not unregister which will lead to memory leak 2014.10.22 start
        if (tm != null && listener != null) {
             tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        }
        //PR817483 PhoneStateListener is not unregister which will lead to memory leak 2014.10.22 end
        super.onDestroy();
    }
    private static final boolean LOG = true;
    /// M: resume bug fix @{
    private boolean mResumed = false;
    private boolean mControlResumed = false;
    private KeyguardManager mKeyguardManager;
    private boolean isKeyguardLocked() {
        if (mKeyguardManager == null) {
            mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        }
        // isKeyguardSecure excludes the slide lock case.
        boolean locked = (mKeyguardManager != null) && mKeyguardManager.inKeyguardRestrictedInputMode();
        if (LOG) {
            MtkLog.v(TAG, "isKeyguardLocked() locked=" + locked + ", mKeyguardManager=" + mKeyguardManager);
        }
        return locked;
    }
    /// @}
    
    /// M: for sdp over http @{
    private static final String VIDEO_SDP_MIME_TYPE = "application/sdp";
    private static final String VIDEO_SDP_TITLE = "rtsp://";
    private static final String VIDEO_FILE_SCHEMA = "file";
    private static final String VIDEO_MIME_TYPE = "video/*";
    private IMovieItem mMovieItem;
    
    private void initMovieInfo(Intent intent) {
        Uri original = intent.getData();
        String mimeType = intent.getType();
        if (VIDEO_SDP_MIME_TYPE.equalsIgnoreCase(mimeType)
                && VIDEO_FILE_SCHEMA.equalsIgnoreCase(original.getScheme())) {
            mMovieItem = new MovieItem(VIDEO_SDP_TITLE + original, mimeType, null);
        } else {
            mMovieItem = new MovieItem(original, mimeType, null);
        }
        mMovieItem.setOriginalUri(original);
        if (LOG) {
            MtkLog.v(TAG, "initMovieInfo(" + original + ") mMovieInfo=" + mMovieItem);
        }
    }
    /// @}
    
    /// M:for live streaming. @{
    //we do not stop live streaming when other dialog overlays it.
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (LOG) {
                MtkLog.v(TAG, "onReceive(" + intent.getAction() + ") mControlResumed=" + mControlResumed);
            }
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                //Only stop video.
                //modified by qjz for PR481530 20130703 begin
                /*if (mControlResumed) {
                    mPlayer.onStop();
                    mControlResumed = false;
                }*/
                if(mPlayer.isPlaying()){
                    mPlayer.onPause();
                }
                //modified by qjz for PR481530 20130703 end
            }
        }
        
    };
    
    private void registerScreenOff() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOffReceiver, filter);
    }
    
    private void unregisterScreenOff() {
        unregisterReceiver(mScreenOffReceiver);
    }
    
    
    /// M: share history file name
    private static final String SHARE_HISTORY_FILE = "video_share_history_file";
    
    //private IActivityHooker mMovieHooker;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public void finishPopupWindow() {
        // TODO Auto-generated method stub
        Log.i("yanlong", "PopupWindowMovieActivity onComletion ");
        // modify start by yaping.liu for pr512877
        try {
            wm.removeView(popupFrameLayout);
        } catch (IllegalArgumentException e) {
            MtkLog.e(TAG, "finishPopupWindow error: " + e.getMessage());
        }
        // modify end by yaping.liu for pr512877
        stopSelf();
    }
    
    public boolean getScreenOrientationStatus() {
        int state = 0;
        try {
            state = Settings.System.getInt(this.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION);
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return state == 1;

    }
    private void initOrientation(){
        if(getScreenOrientationStatus()){
            //auto rotation is enabled
            Resources res=this.getResources();
            mOrientation=res.getConfiguration().orientation;
        }else{
            mOrientation=Configuration.ORIENTATION_PORTRAIT;
        }
    }
    
    public void rescalePopWindowSize(int width, int height) {
        // scale screen size add by yanlong
        Log.i("yanlong", " call rescalePopWindowSize @@width " + width + "  height: " + height);
        WindowManager.LayoutParams wmParams = ((GalleryAppImpl) this.getApplicationContext())
                .getMywmParams();
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            wmParams.height = (int) (((int) (wmParams.width) * height) / width);
        } else {
            wmParams.width = (int) (((int) (wmParams.height) * width) / height);
        }
        ((GalleryAppImpl) this.getApplicationContext()).setMywmParams(wmParams);
        double ratio = (double) (width) / (double) (height);
        popupFrameLayout.rescalePopWindowSize(displayWidth, displayHeight, ratio);
    }
}
