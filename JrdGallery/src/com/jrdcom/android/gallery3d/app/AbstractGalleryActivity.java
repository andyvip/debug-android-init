/*
 * Copyright (C) 2010 The Android Open Source Project
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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
//add by qjz for Gallery NewUI begin
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ImageView;
//add by qjz for Gallery NewUI end

import com.jrdcom.android.gallery3d.app.GalleryActionBar;
import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.common.ApiHelper;
import com.jrdcom.android.gallery3d.data.BitmapPool;
import com.jrdcom.android.gallery3d.data.DataManager;
import com.jrdcom.android.gallery3d.data.MediaItem;
import com.jrdcom.android.gallery3d.ui.GLRoot;
import com.jrdcom.android.gallery3d.ui.GLRootView;
import com.jrdcom.android.gallery3d.util.ThreadPool;
import com.jrdcom.android.gallery3d.util.LightCycleHelper.PanoramaViewHelper;
import android.os.Environment;

import com.jrdcom.mediatek.gallery3d.drm.DrmHelper;
import com.jrdcom.mediatek.gallery3d.util.MtkUtils;

//PR929582 when default storage has been changed, we should refresh bucked id by fengke at 2015.02.12 start
import com.jrdcom.android.gallery3d.util.MediaSetUtils;
//PR929582 when default storage has been changed, we should refresh bucked id by fengke at 2015.02.12 end

public class AbstractGalleryActivity extends Activity implements GalleryContext {
    @SuppressWarnings("unused")
    private static final String TAG = "AbstractGalleryActivity";
    private GLRootView mGLRootView;
    private StateManager mStateManager;
    private GalleryActionBar mActionBar;
    private OrientationManager mOrientationManager;
    private TransitionStore mTransitionStore = new TransitionStore();
    private boolean mDisableToggleStatusBar;
    private PanoramaViewHelper mPanoramaViewHelper;

    //add by qjz for Gallery NewUI 20130427 begin
    private ExpandableListView mExList;
    private LinearLayout mListTopView;
    //private ImageView mPositionImage;
    //add by qjz for Gallery NewUI 20130427 end
    private AlertDialog mAlertDialog = null;
    /// M: sign gallery status.
    private volatile boolean hasPausedActivity;
    /// M: if shouldHideToast is true, should hide empty_album Toast. for multi delete operation.
    private volatile boolean shouldHideToast;
    //PR504333-Wentao-Wan-001 begin
    //define for WFD(wifydiaplay) connected about DRM file play.
    public static final String WFD_CONNECTED_ACTION = "com.mediatek.wfd.connection";
    public static final int WFD_CONNECTED_FLAG = 1;
    public static final int WFD_DISCONNECTED_FLAG = 0;
    private WfdReceiver WfdReceiver = new WfdReceiver();
    private WifiDisplayStatus mWifiDisplayStatus;
    private DisplayManager mDisplayManager;
    private AlertDialog mWfdDialog = null;
    //PR504333-Wentao-Wan-001 end
    private BroadcastReceiver mMountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if (getExternalCacheDir() != null) onStorageReady();
            // we don't care about SD card content;
            // As long as the card is mounted, dismiss the dialog
            onStorageReady();
        }
    };
    private IntentFilter mMountFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrientationManager = new OrientationManager(this);
        toggleStatusBarByOrientation();
        getWindow().setBackgroundDrawable(null);
        mPanoramaViewHelper = new PanoramaViewHelper(this);
        mPanoramaViewHelper.onCreate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mGLRootView.lockRenderThread();
        try {
            super.onSaveInstanceState(outState);
            getStateManager().saveState(outState);
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        mStateManager.onConfigurationChange(config);
        getGalleryActionBar().onConfigurationChanged();
        invalidateOptionsMenu();
        toggleStatusBarByOrientation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return getStateManager().createOptionsMenu(menu);
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }

    @Override
    public DataManager getDataManager() {
        return ((GalleryApp) getApplication()).getDataManager();
    }

    @Override
    public ThreadPool getThreadPool() {
        return ((GalleryApp) getApplication()).getThreadPool();
    }

    public synchronized StateManager getStateManager() {
        if (mStateManager == null) {
            mStateManager = new StateManager(this);
        }
        return mStateManager;
    }

    public GLRoot getGLRoot() {
        return mGLRootView;
    }

    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }

    @Override
    public void setContentView(int resId) {
        super.setContentView(resId);
        mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
      //add by qjz for Gallery NewUI 20130427 begin
        /*mExList = (ExpandableListView) findViewById(R.id.exlist);
        mListTopView = (LinearLayout) findViewById(R.id.list_top_group);*/
        //mPositionImage = (ImageView) findViewById(R.id.pop_window_position);
        //add by qjz for Gallery NewUI 20130427 end
    }
    //add by qjz for Gallery NewUI 20130427 begin
    public ExpandableListView getExpandableListView() {
        return mExList;
    }

    public LinearLayout getListTopView() {
        return mListTopView;
    }

    /*public ImageView getPositionImage() {
        return mPositionImage;
    }*/
    //add by qjz for Gallery NewUI 20130427 end
    protected void onStorageReady() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
            //huanglin 20130913 for PR524101
            try{
                unregisterReceiver(mMountReceiver);
            }catch(IllegalArgumentException e){
                Log.d(TAG,"onStorageReady: = " + e);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //PR504333-Wentao-Wan-001 begin
        //register WFD broadcast Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(WFD_CONNECTED_ACTION);
        getApplication().registerReceiver(WfdReceiver, filter);
        Log.d(TAG, "register wfdReceiver");
        //PR504333-Wentao-Wan-001 end

        // M: if we're viewing a non-local file/uri, do NOT check storage 
        // or pop up "No storage" dialog
        Log.d(TAG, "onStart: should check storage=" + mShouldCheckStorageState);
        if (!mShouldCheckStorageState) {
            return;
        }
        
        // M: we only care about not mounted condition,
        // SD card full/error state does not affect normal usage of Gallery2
        if ((MtkUtils.getMTKExternalCacheDir(this) == null) && (!isDefaultStorageMounted())) {
            OnCancelListener onCancel = new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            };
            OnClickListener onClick = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.no_external_storage_title)
                    .setMessage(R.string.no_external_storage)
                    .setNegativeButton(android.R.string.cancel, onClick)
                    .setOnCancelListener(onCancel);
            if (ApiHelper.HAS_SET_ICON_ATTRIBUTE) {
                setAlertDialogIconAttribute(builder);
            } else {
                builder.setIcon(android.R.drawable.ic_dialog_alert);
            }
            mAlertDialog = builder.show();
            registerReceiver(mMountReceiver, mMountFilter);
        }
        mPanoramaViewHelper.onStart();
    }

    @TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
    private static void setAlertDialogIconAttribute(
            AlertDialog.Builder builder) {
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //PR504333-Wentao-Wan-001 begin
        //unregister the wifydiaplayReceiver when leave Gallery app.
        if(WfdReceiver != null){
           getApplication().unregisterReceiver(WfdReceiver);
        }
        //PR504333-Wentao-Wan-001 end
        if (mAlertDialog != null) {
            //huanglin 20130913 for PR524101
            try{
                unregisterReceiver(mMountReceiver);
            }catch(IllegalArgumentException e){
                Log.d(TAG,"onStop: = " + e);
            }
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
        mPanoramaViewHelper.onStop();
    }

    //PR504333-Wentao-Wan-001 begin
    //WifyDisPlay BroadCastReveiver
    private class WfdReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action == WFD_CONNECTED_ACTION) {
                int ExtraResult = intent.getIntExtra("connected", 0);
                Log.d(TAG, "WfdReceiver action:" + action + "connected = " + ExtraResult);
                if (ExtraResult == WFD_CONNECTED_FLAG) {
                        Log.d(TAG, "camera set drm");
                        DrmHelper.setDrmFlag(false);
                } else if (ExtraResult == WFD_DISCONNECTED_FLAG) {
                    Log.d(TAG, "gallery set drm");
                    DrmHelper.setDrmFlag(true);
                }
            }
        }
    };
   //PR504333-Wentao-Wan-001 end

    @Override
    protected void onResume() {
        super.onResume();
        //PR929582 when default storage has been changed, we should refresh bucked id by fengke at 2015.02.12 start
        MediaSetUtils.refreshBucketId();
        //PR929582 when default storage has been changed, we should refresh bucked id by fengke at 2015.02.12 end
        mGLRootView.lockRenderThread();
        try {
            getStateManager().resume();
            getDataManager().resume();
        } finally {
            mGLRootView.unlockRenderThread();
        }
        mGLRootView.onResume();
        mOrientationManager.resume();
        /// M: save activity status.
        hasPausedActivity = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOrientationManager.pause();
        mGLRootView.onPause();
        mGLRootView.lockRenderThread();
        try {
            getStateManager().pause();
            getDataManager().pause();
        } finally {
            mGLRootView.unlockRenderThread();
        }
        clearBitmapPool(MediaItem.getMicroThumbPool());
        clearBitmapPool(MediaItem.getThumbPool());

        MediaItem.getBytesBufferPool().clear();
        /// M: save activity status.
        hasPausedActivity = true;
    }

    private static void clearBitmapPool(BitmapPool pool) {
        if (pool != null) pool.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGLRootView.lockRenderThread();
        try {
            getStateManager().destroy();
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGLRootView.lockRenderThread();
        try {
            getStateManager().notifyActivityResult(
                    requestCode, resultCode, data);
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onBackPressed() {
        // send the back event to the top sub-state
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            getStateManager().onBackPressed();
        } finally {
            root.unlockRenderThread();
        }
    }

    public GalleryActionBar getGalleryActionBar() {
        if (mActionBar == null) {
            mActionBar = new GalleryActionBar(this);
        }
        return mActionBar;
    }

    public boolean hasPausedActivity () {
        return hasPausedActivity;
    }

    public void setHideToast (boolean hideToast) {
        shouldHideToast = hideToast;
    }

    public boolean isHidedToast () {
        return shouldHideToast ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            return getStateManager().itemSelected(item);
        } finally {
            root.unlockRenderThread();
        }
    }

    protected void disableToggleStatusBar() {
        mDisableToggleStatusBar = true;
    }

    // Shows status bar in portrait view, hide in landscape view
    private void toggleStatusBarByOrientation() {
        if (mDisableToggleStatusBar) return;

        Window win = getWindow();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            win.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public TransitionStore getTransitionStore() {
        return mTransitionStore;
    }

    public PanoramaViewHelper getPanoramaViewHelper() {
        return mPanoramaViewHelper;
    }

    protected boolean isFullscreen() {
        return (getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
    }
    
    // M: added for multi-storage support
    private boolean isDefaultStorageMounted() {
        String defaultStorageState = MtkUtils.getMtkDefaultStorageState(this);
        if (defaultStorageState == null) {
            defaultStorageState = Environment.getExternalStorageState();
        }
        return Environment.MEDIA_MOUNTED.equalsIgnoreCase(defaultStorageState);
    }
    
    // M: added for SD hot-plug
    public boolean mShouldCheckStorageState = true;
}
