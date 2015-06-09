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

import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources.NotFoundException;//FR450344-yanlong.li@tcl.com
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ActivityChooserView;
import android.widget.ShareActionProvider;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.common.ApiHelper;
import com.jrdcom.android.gallery3d.common.Utils;

import com.jrdcom.android.gallery3d.app.MoviePlayer;
import com.jrdcom.mediatek.gallery3d.ext.IActivityHooker;
import com.jrdcom.mediatek.gallery3d.ext.IMovieItem;
import com.jrdcom.mediatek.gallery3d.ext.MovieItem;
import com.jrdcom.mediatek.gallery3d.ext.MovieUtils;
import com.jrdcom.mediatek.gallery3d.ext.MtkLog;
import com.jrdcom.mediatek.gallery3d.util.MtkUtils;
import com.jrdcom.mediatek.gallery3d.video.ExtensionHelper;
import com.jrdcom.mediatek.gallery3d.video.MovieTitleHelper;
import com.mediatek.drm.OmaDrmClient;

import android.drm.DrmManagerClient;
import android.view.WindowManager;

//PR765879 modify for video player UE design by fengke at 2014.08.28 start
import android.media.MediaMetadataRetriever;
//PR765879 modify for video player UE design by fengke at 2014.08.28 end

/**
 * This activity plays a video from a specified URI.
 * 
 * The client of this activity can pass a logo bitmap in the intent (KEY_LOGO_BITMAP)
 * to set the action bar logo so the playback process looks more seamlessly integrated with
 * the original activity.
 */
public class MovieActivity extends Activity implements CreateBeamUrisCallback {
    @SuppressWarnings("unused")
    private static final String TAG = "MovieActivity";
    public static final String KEY_LOGO_BITMAP = "logo-bitmap";
    public static final String KEY_TREAT_UP_AS_BACK = "treat-up-as-back";

    private MoviePlayer mPlayer;
    private boolean mFinishOnCompletion;
    //private Uri mUri;
    private boolean mTreatUpAsBack;
    ///M: add for NFC
    private boolean mBeamVideoIsPlaying = false;
    private boolean mLogo = false;

    //FR477527-kuiwang-001 begin
    public static final String KEY_CAR_MODE_FLAG = "in_car_mode_flag";
    public static final int FLAG_HOMEKEY_DISPATCHED = WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED;
//    public static final int FLAG_JRD_RECENTAPPKEY_DISPATCHED = WindowManager.LayoutParams.FLAG_JRD_RECENTAPPKEY_DISPATCHED;
    private static final String BLUETOOTH_SETTINGS_ACTION = "android.settings.BLUETOOTH_SETTINGS";
    private Intent mCarModeIntent;

    private String mSDPOriginaUrl;//PR629296, tian.shi

    //FR477527-kuiwang-001 end
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setSystemUiVisibility(View rootView) {
        if (ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }
    
    /// M: NFC feature
    NfcAdapter mNfcAdapter;
    private final Handler mHandler = new Handler();
    private final Runnable mPlayVideoRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null && mBeamVideoIsPlaying) {
                MtkLog.i(TAG, "NFC call play video");
                mPlayer.onPlayPause();
            }
        }
    };
    
    private final Runnable mPauseVideoRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null && mPlayer.isPlaying()) {
                MtkLog.i(TAG, "NFC call pause video");
                mBeamVideoIsPlaying = true;
                mPlayer.onPlayPause();
            } else {
                mBeamVideoIsPlaying = false;
            }
        }
    };
    //add by qjz for PR495700:It will occur the force close prompt when play popup video 20130729
    public boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);// modify by yaping.liu for pr503258
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
    public static boolean isDrm;//PR519068,bin.li@tct-nj.com

    //PR765879 modify for video player UE design by fengke at 2014.08.28 start
    public Activity mActivity;
    //PR765879 modify for video player UE design by fengke at 2014.08.28 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //PR481505:The popup video and video can play together.
        //add by qjz 20130708 begin
        Intent intent = getIntent();
        //PR765879 modify for video player UE design by fengke at 2014.08.28 start
        mActivity = this;
        //PR765879 modify for video player UE design by fengke at 2014.08.28 end
        initMovieInfo(intent);
        mTreatUpAsBack = intent.getBooleanExtra(KEY_TREAT_UP_AS_BACK, false);// add by yaping.liu for pr499786

        //PR765879 modify for video player UE design by fengke at 2014.08.28 start
        new Thread() {
            @Override
            public void run() {
                Log.v(TAG,"fengke getVideoRotate start");
                int videoRotate = getVideoRotate(mActivity,mMovieItem.getUri());
                Log.v(TAG,"fengke getVideoRotate videoRotate = " + videoRotate);
                if (videoRotate >= 0 ){
                    mActivity.setRequestedOrientation(videoRotate);
                }
            }
        }.start();
        //PR765879 modify for video player UE design by fengke at 2014.08.28 end

        //modified by qjz for PR495700:It will occur the force close prompt when play popup video 20130729 begin
        //SharedPreferences preferences = this.getSharedPreferences("com.jrdcom.android.gallery3d_preferences", 0);
        //if (preferences!=null && preferences.getBoolean("isStartPopupVideo", false)) {
        if (isServiceRunning(this, "com.jrdcom.android.gallery3d.app.PopupWindowMovieActivity")) {
        //modified by qjz for PR495700:It will occur the force close prompt when play popup video 20130729 end
            Intent popupVideo =new Intent();
            // add start by yaping.liu for pr499786
            popupVideo.putExtra(KEY_TREAT_UP_AS_BACK, mTreatUpAsBack);
            Bitmap logo = intent.getParcelableExtra(KEY_LOGO_BITMAP);
            if (logo != null) {
                popupVideo.putExtra(PopupWindowMovieActivity.KEY_LOGO_BITMAP, true);
            }
            // add end by yaping.liu for pr499786
            popupVideo.setAction("com.jrdcom.action.INIT_POPUPWINDOW");
            popupVideo.setDataAndType(mMovieItem.getUri(), mMovieItem.getMimeType());
            Bundle bundle=new Bundle();
            bundle.putInt("position", 0);
            startService(popupVideo.putExtras(bundle));
            finish();
            return;
        }
        //PR510332-kuiwang-001 begin
        //The requirement changed.don't need shield home key and recent key, and it cause the play view problem
        // so delete the shield code
        //FR477527-kuiwang-001 begin
        //if carmode call the bluetooth settings,set screen full, shield rencent and home key,set screen orientation landscape
        mCarModeIntent = getIntent();
//        if (mCarModeIntent.getBooleanExtra(KEY_CAR_MODE_FLAG, false)) {
 			// shield home key,only use in MTK chip
// 			getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED,FLAG_HOMEKEY_DISPATCHED);
 			// shield rencent key,only use in MTK chip
// 			getWindow().setFlags(FLAG_JRD_RECENTAPPKEY_DISPATCHED, FLAG_JRD_RECENTAPPKEY_DISPATCHED);
// 		 }
 		 //FR477527-kuiwang-001 end
        //PR510332-kuiwang-001 end

        //PR510056-kuiwang-001 begin
        if (mCarModeIntent.getBooleanExtra(KEY_CAR_MODE_FLAG, false)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //PR510056-kuiwang-001 end

        //PR481505:The popup video and video can play together.
        //add by qjz 20130708 end
        MtkLog.v(TAG, "onCreate()");
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.movie_view);
        View rootView = findViewById(R.id.movie_view_root);

        setSystemUiVisibility(rootView);

        //PR629296, tian.shi, begin
        if(intent.hasExtra("SDPOriginaUrl")){
            mSDPOriginaUrl = intent.getStringExtra("SDPOriginaUrl");
        }
        //PR629296, tian.shi, end

        mMovieHooker = ExtensionHelper.getHooker(this);
        initializeActionBar(intent);
        mFinishOnCompletion = intent.getBooleanExtra(
                MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
      //FR450344-yanlong.li@tcl.com-begin
        Bundle bundle = intent.getExtras();
        boolean popupVideo =false;
        try{
            //PR519068,In lanscape mode, it shows black window when play drm video via pop up video,bin.li@tct-nj.com
            if(isDrm || mIsAttachment) popupVideo=false;//modify by yaping.liu for pr506689
            else popupVideo =  this.getResources().getBoolean(R.bool.def_gallery_pop_up_video);
            //end PR519068
        }catch(NotFoundException nfe){
            Log.i(TAG, " not found customize key def_gallery_pop_up_video ");
        }
        if (popupVideo && bundle != null) {
            int position = bundle.getInt("position");
            // Log.i("yanlong", " position: "+position);
            if (position > 0) {
                mPlayer = new MoviePlayer(rootView, this, mMovieItem, savedInstanceState,
                        !mFinishOnCompletion, position, mIsAttachment) {//modify by yaping.liu for pr506689
                    @Override
                    public void onCompletion() {
                        if (LOG) {
                            MtkLog.v(TAG, "onCompletion() mFinishOnCompletion="
                                    + mFinishOnCompletion);
                        }
                        if (mFinishOnCompletion) {
                            finish();
                        }
                    }
                };

            } else {
                // FR450344 add by yanlong end
                mPlayer = new MoviePlayer(rootView, this, mMovieItem, savedInstanceState,
                        !mFinishOnCompletion, mIsAttachment) {//modify by yaping.liu for pr506689
                    @Override
                    public void onCompletion() {
                        if (LOG) {
                            MtkLog.v(TAG, "onCompletion() mFinishOnCompletion="
                                    + mFinishOnCompletion);
                        }
                        if (mFinishOnCompletion) {
                            finish();
                        }
                    }
                };

            }
        }else{
            mPlayer = new MoviePlayer(rootView, this, mMovieItem, savedInstanceState,
                    !mFinishOnCompletion, mIsAttachment) {//modify by yaping.liu for pr506689
                @Override
                public void onCompletion() {
                    if (LOG) {
                        MtkLog.v(TAG, "onCompletion() mFinishOnCompletion=" + mFinishOnCompletion);
                    }
                    if (mFinishOnCompletion) {
                        finish();
                    }
                }
            };
        }
        mPlayer.setTreatUpAsBack(mTreatUpAsBack, mLogo);//add by qjz for PR487297
        if (intent.hasExtra(MediaStore.EXTRA_SCREEN_ORIENTATION)) {
            int orientation = intent.getIntExtra(
                    MediaStore.EXTRA_SCREEN_ORIENTATION,
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (orientation != getRequestedOrientation()) {
                setRequestedOrientation(orientation);
            }
        }
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
        winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        win.setAttributes(winParams);

        // We set the background in the theme to have the launching animation.
        // But for the performance (and battery), we remove the background here.
        win.setBackgroundDrawable(null);
        mMovieHooker.init(this, intent);
        mMovieHooker.setParameter(null, mPlayer.getMoviePlayerExt());
        mMovieHooker.setParameter(null, mMovieItem);
        mMovieHooker.setParameter(null, mPlayer.getVideoSurface());
        mMovieHooker.onCreate(savedInstanceState);
    }

    //PR765879 modify for video player UE design by fengke at 2014.08.28 start
    public int getVideoRotate(Context context, Uri uri) {
        int returnVal = -1;
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            if (uri != null) {
                retriever.setDataSource(context, uri);
                Bitmap temp = retriever.getFrameAtTime();
                if (temp != null) {
                    Log.w(TAG,"fengke getVideoRotate temp.getWidth() = " + temp.getWidth() + ", getHeight() = " + temp.getHeight());
                    if (temp.getWidth() > temp.getHeight()){
                        returnVal = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    } else {
                        returnVal = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG,"fengke getVideoRotate Error:"+ex.toString());
        } finally {
            if (retriever != null) {
                retriever.release();
                retriever = null;
            }
        }
        return returnVal;
    }
    //PR765879 modify for video player UE design by fengke at 2014.08.28 send

    private void setActionBarLogoFromIntent(Intent intent) {
        Bitmap logo = intent.getParcelableExtra(KEY_LOGO_BITMAP);
        mLogo = false;
        if (logo != null) {
            mLogo = true;
            getActionBar().setLogo(
                    new BitmapDrawable(getResources(), logo));
        }
        /// M: Get Nfc adapter and set callback available. @{
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (mNfcAdapter == null) {
            MtkLog.e(TAG, "NFC not available!");
            return;
        }
        mNfcAdapter.setBeamPushUrisCallback(this, this);
        OnNdefPushCompleteCallback completeCallBack = new OnNdefPushCompleteCallback(){
            @Override
            public void onNdefPushComplete(NfcEvent event) {
                mHandler.removeCallbacks(mPlayVideoRunnable);
                mHandler.post(mPlayVideoRunnable);
            }
        };
        mNfcAdapter.setOnNdefPushCompleteCallback(completeCallBack, this, this);
        /// @}
    }
    
    private void initializeActionBar(Intent intent) {
        //mUri = intent.getData();
        final ActionBar actionBar = getActionBar();
        setActionBarLogoFromIntent(intent);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        /// M: show title for video playback
        actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_TITLE);

//        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
//        if (title != null) {
//            actionBar.setTitle(title);
//        } else {
//            enhanceActionBar();
            /*// Displays the filename as title, reading the filename from the
            // interface: {@link android.provider.OpenableColumns#DISPLAY_NAME}.
            AsyncQueryHandler queryHandler =
                    new AsyncQueryHandler(getContentResolver()) {
                @Override
                protected void onQueryComplete(int token, Object cookie,
                        Cursor cursor) {
                    try {
                        if ((cursor != null) && cursor.moveToFirst()) {
                            String displayName = cursor.getString(0);

                            // Just show empty title if other apps don't set
                            // DISPLAY_NAME
                            actionBar.setTitle((displayName == null) ? "" :
                                    displayName);
                        }
                    } finally {
                        Utils.closeSilently(cursor);
                    }
                }
            };
            queryHandler.startQuery(0, null, mUri,
                    new String[] {OpenableColumns.DISPLAY_NAME}, null, null,
                    null);*/
//        }
        if (LOG) {
            MtkLog.v(TAG, "initializeActionBar() mMovieInfo=" + mMovieItem);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        boolean local = MovieUtils.isLocalFile(mMovieItem.getOriginalUri(), mMovieItem.getMimeType());
        if (!MtkUtils.canShare(getIntent().getExtras()) || (local && 
                !ExtensionHelper.getMovieDrmExtension(this).canShare(this, mMovieItem))) {
            //do not show share
        } else {
            if (!mCarModeIntent.getBooleanExtra(KEY_CAR_MODE_FLAG, false)) {//PR501083-kuiwang-001
            getMenuInflater().inflate(R.menu.movie, menu);
            mShareMenu = menu.findItem(R.id.action_share);
            ShareActionProvider provider = (ShareActionProvider) mShareMenu.getActionProvider();
            mShareProvider = provider;
            if (mShareProvider != null) {
                /// M: share provider is singleton, we should refresh our history file.
//                mShareProvider.setShareHistoryFileName(SHARE_HISTORY_FILE);
            }
            refreshShareProvider(mMovieItem);
            }//PR501083-kuiwang-001
        }

        return mMovieHooker.onCreateOptionsMenu(menu);
        /*getMenuInflater().inflate(R.menu.movie, menu);
        ShareActionProvider provider = GalleryActionBar.initializeShareActionProvider(menu);

        // Document says EXTRA_STREAM should be a content: Uri
        // So, we only share the video if it's "content:".
        if (provider != null && ContentResolver.SCHEME_CONTENT
                .equals(mUri.getScheme())) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("video/*");
            intent.putExtra(Intent.EXTRA_STREAM, mUri);
            provider.setShareIntent(intent);
        }

        return true;*/
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return mMovieHooker.onPrepareOptionsMenu(menu);
    }
    
    private Intent createShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, mMovieItem.getUri());
        return intent;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
    	//FR477527-kuiwang-001 begin
		if (mCarModeIntent.getBooleanExtra(KEY_CAR_MODE_FLAG, false)) {
			return mMovieHooker.onOptionsItemSelected(item);
		}
		//FR477527-kuiwang-001 end
        if (id == android.R.id.home) {
            if (mTreatUpAsBack) {
                finish();
            } else {
                startActivity(new Intent(this, Gallery.class));
                finish();
            }
            return true;
        } else if (id == R.id.action_share) {
            startActivity(Intent.createChooser(createShareIntent(),
                    getString(R.string.share)));
            return true;
        }
        return mMovieHooker.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMovieHooker.onStart();
        registerScreenOff();
        if (LOG) {
            MtkLog.v(TAG, "onStart()");
        }
    }

    @Override
    protected void onStop() {
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .abandonAudioFocus(null);
        super.onStop();
        if (mControlResumed && mPlayer != null) {
            mPlayer.onStop();
            mControlResumed = false;
        }
        mMovieHooker.onStop();
        unregisterScreenOff();
        if (LOG) {
            MtkLog.v(TAG, "onStop() isKeyguardLocked=" + isKeyguardLocked()
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
        }
    }

    @Override
    public void onPause() {
        if (LOG) {
            MtkLog.v(TAG, "onPause() isKeyguardLocked=" + isKeyguardLocked()
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
        }
        mResumed = false;
        if (mControlResumed && mPlayer != null) {
            mControlResumed = !mPlayer.onPause();
        }
        super.onPause();
        collapseShareMenu();
        mMovieHooker.onPause();
    }

    @Override
    public void onResume() {
        if (LOG) {
            MtkLog.v(TAG, "onResume() isKeyguardLocked=" + isKeyguardLocked()
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
        }
        mResumed = true;
        if (!isKeyguardLocked() && mResumed && !mControlResumed && mPlayer != null) {
            mPlayer.onResume();
            mControlResumed = true;
        }

        enhanceActionBar();
        super.onResume();
        mMovieHooker.onResume();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (LOG) {
            MtkLog.v(TAG, "onWindowFocusChanged(" + hasFocus + ") isKeyguardLocked=" + isKeyguardLocked()
                + ", mResumed=" + mResumed + ", mControlResumed=" + mControlResumed);
        }
        if (hasFocus && !isKeyguardLocked() && mResumed && !mControlResumed && mPlayer != null) {
            mPlayer.onResume();
            mControlResumed = true;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPlayer.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        //PR481505:The popup video and video can play together.
        //modified by qjz 20130708 begin
        if (mPlayer != null ) {
            mPlayer.onDestroy();
        }
        super.onDestroy();
        if (mMovieHooker != null) {
            mMovieHooker.onDestroy();
        }
        //PR481505:The popup video and video can play together.
        //modified by qjz 20130708 begin
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mPlayer.onKeyDown(keyCode, event)
                || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mPlayer.onKeyUp(keyCode, event)
                || super.onKeyUp(keyCode, event);
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
    
    private boolean mIsAttachment = false;//add by yaping.liu for pr506689
    
    private void initMovieInfo(Intent intent) {
        Uri original = intent.getData();
        //PR519068,In lanscape mode, it shows black window when play drm video via pop up video,bin.li@tct-nj.com
        //FR 576703 add by xiangchen begin
//        DrmManagerClient.getInstance(this);
//        if(original.toString().startsWith("content://")&&!original.toString().startsWith("content://media/")){ //530183 by xiangchen
//        	
//        }else{
//        	 isDrm = DrmManagerClient.isDrm(original.toString());
//        }
        //FR 576703 add by xiangchen
        //end PR519068
        String mimeType = intent.getType();
        if (VIDEO_SDP_MIME_TYPE.equalsIgnoreCase(mimeType)
                && VIDEO_FILE_SCHEMA.equalsIgnoreCase(original.getScheme())) {
             mMovieItem = new MovieItem(VIDEO_SDP_TITLE + original,mimeType, null);
        } else {
            mMovieItem = new MovieItem(original, mimeType, null);
        }
        mMovieItem.setOriginalUri(original);
        // add start by yaping.liu for pr506689
        mIsAttachment = judgeIsAttachment(original, mimeType);
        // add end by yaping.liu for pr506689
        if (LOG) {
            MtkLog.v(TAG, "initMovieInfo(" + original + ") mMovieInfo=" + mMovieItem);
        }
    }
    /// @}

    // add start by yaping.liu for pr506689
    private boolean judgeIsAttachment(Uri uri, String mimeType) {
        if (LOG) {
            MtkLog.v(TAG, "judgeIsAttachment(" + uri + ")");
        }
        boolean isAttachment = false;
        if (uri == null) {
            return false;
        }
        if (MovieUtils.isSdpStreaming(uri, mimeType)) {
        } else if (MovieUtils.isRtspStreaming(uri, mimeType)) {
        } else if (MovieUtils.isHttpStreaming(uri, mimeType)) {
        } else {
            if (uri.toString().startsWith("content://mms/part/")) {
                isAttachment = true;
            }
        }
        if (LOG) {
            MtkLog.v(TAG, "isAttachment: " + isAttachment);
        }
        return isAttachment;
    }
    // add end by yaping.liu for pr506689

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
                if (mControlResumed) {
                    mPlayer.onStop();
                    mControlResumed = false;
                }
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
    /// @}
    
    /// M: enhance the title feature @{
    private void enhanceActionBar() {
        final IMovieItem movieItem = mMovieItem;//remember original item
        final Uri uri = mMovieItem.getUri();
        final String scheme = mMovieItem.getUri().getScheme();
        final String authority = mMovieItem.getUri().getAuthority();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String title = null;
                if (ContentResolver.SCHEME_FILE.equals(scheme)) { //from file manager
                    title = MovieTitleHelper.getTitleFromMediaData(MovieActivity.this, uri);
                } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    title = MovieTitleHelper.getTitleFromDisplayName(MovieActivity.this, uri);
                    if (title == null) {
                        title = MovieTitleHelper.getTitleFromData(MovieActivity.this, uri);
                    }
                }
                if (title == null) {
                    title = MovieTitleHelper.getTitleFromUri(uri);
                }
                if (LOG) {
                    MtkLog.v(TAG, "enhanceActionBar() task return " + title);
                }
                return title;
            }
            @Override
            protected void onPostExecute(String result) {
                if (LOG) {
                    MtkLog.v(TAG, "onPostExecute(" + result + ") movieItem=" + movieItem + ", mMovieItem=" + mMovieItem);
                }
                movieItem.setTitle(result);
                if (movieItem == mMovieItem) {
                    setActionBarTitle(result);
                }
            };
        }.execute();
        if (LOG) {
            MtkLog.v(TAG, "enhanceActionBar() " + mMovieItem);
        }
    }
    
    public void setActionBarTitle(String title) {
        if (LOG) {
            MtkLog.v(TAG, "setActionBarTitle(" + title + ")");
        }
        ActionBar actionBar = getActionBar();
        if (title != null) {
            actionBar.setTitle(title);
        }
    }
    /// @}

    public void refreshMovieInfo(IMovieItem info) {
        mMovieItem = info;
        setActionBarTitle(info.getTitle());
        refreshShareProvider(info);
        mMovieHooker.setParameter(null, mMovieItem);
        if (LOG) {
            MtkLog.v(TAG, "refreshMovieInfo(" + info + ")");
        }
    }

    private ShareActionProvider mShareProvider;
    private void refreshShareProvider(IMovieItem info) {
        // Document says EXTRA_STREAM should be a content: Uri
        // So, we only share the video if it's "content:".
        /// M: the upper is JellyBean's comment, here we enhance the share action.
        if (mShareProvider != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            if (MovieUtils.isLocalFile(info.getUri(), info.getMimeType())) {
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_STREAM, info.getUri());
            } else {
                intent.setType("text/plain");
                //PR629296, tian.shi, begin
                if(mSDPOriginaUrl != null && "application/sdp".equals(info.getMimeType())){
                    intent.putExtra(Intent.EXTRA_TEXT, mSDPOriginaUrl);
                }else{
                    intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(info.getUri()));
                }
                //PR629296, tian.shi, end
            }
            mShareProvider.setShareIntent(intent);
        }
        if (LOG) {
            MtkLog.v(TAG, "refreshShareProvider() mShareProvider=" + mShareProvider);
        }
    }
    
    /* M: ActivityChooseView's popup window will not dismiss
     * when user press power key off and on quickly.
     * Here dismiss the popup window if need.
     * Note: dismissPopup() will check isShowingPopup().
     * @{
     */
    private MenuItem mShareMenu;
    private void collapseShareMenu() {
        if (mShareMenu != null &&  mShareMenu.getActionView() instanceof ActivityChooserView) {
            ActivityChooserView chooserView = (ActivityChooserView)mShareMenu.getActionView();
            if (LOG) {
                MtkLog.v(TAG, "collapseShareMenu() chooserView.isShowingPopup()=" + chooserView.isShowingPopup());
            }
            chooserView.dismissPopup();
        }
    }
    /* @} */
    
    /// M: share history file name
    private static final String SHARE_HISTORY_FILE = "video_share_history_file";
    
    private IActivityHooker mMovieHooker;
    /**
     * M: Add NFC callback to provide the uri.
     */
    @Override
    public Uri[] createBeamUris(NfcEvent event) {
        mHandler.removeCallbacks(mPauseVideoRunnable);
        mHandler.post(mPauseVideoRunnable);
        Uri currentUri = mMovieItem.getOriginalUri();
        MtkLog.i(TAG, "NFC call for uri " + currentUri);
        return new Uri[]{currentUri};
    }
}
