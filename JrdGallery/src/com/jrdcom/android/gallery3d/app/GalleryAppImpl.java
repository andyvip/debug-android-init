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

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.view.WindowManager; //FR450344 yanlong.li@tcl.com

import com.jrdcom.android.gallery3d.common.ApiHelper;
import com.jrdcom.android.gallery3d.data.DataManager;
import com.jrdcom.android.gallery3d.data.DownloadCache;
import com.jrdcom.android.gallery3d.data.ImageCacheService;
import com.jrdcom.android.gallery3d.gadget.WidgetUtils;
import com.jrdcom.android.gallery3d.picasasource.PicasaSource;
import com.jrdcom.android.gallery3d.util.CacheManager;
import com.jrdcom.android.gallery3d.util.GalleryUtils;
import com.jrdcom.android.gallery3d.util.LightCycleHelper;
import com.jrdcom.android.gallery3d.util.ThreadPool;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;


import com.jrdcom.mediatek.gallery3d.util.MtkLog;
import com.jrdcom.mediatek.gallery3d.util.MtkUtils;
import com.jrdcom.mt.util.MyData;

public class GalleryAppImpl extends Application implements GalleryApp {

    private static final String DOWNLOAD_FOLDER = "download";
    private static final long DOWNLOAD_CAPACITY = 64 * 1024 * 1024; // 64M

    private ImageCacheService mImageCacheService;
    private Object mLock = new Object();
    private DataManager mDataManager;
    private ThreadPool mThreadPool;
    private DownloadCache mDownloadCache;
    private StitchingProgressManager mStitchingProgressManager;
    
    static {
        System.loadLibrary("_mt_image_jni");
        System.loadLibrary("_mt_image");
        System.loadLibrary("_mt_image_puzzle");
        // PR 524575 jipu.xiong@tcl.com begin
        try {
        //FR 576703 add by xiangchen begin
            System.loadLibrary("jni_filtershow_filters_jrd");
        //FR 576703 add by xiangchen begin
        } catch (UnsatisfiedLinkError ue) {
            Log.v("GalleryAppImpl", "Try to load jni_filtershow_filters, because the fatal exception:" + ue);
            System.loadLibrary("jni_filtershow_filters");
        }
        // PR 524575 jipu.xiong@tcl.com end
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        com.android.jrdcamera.Util.initialize(this);
        initializeAsyncTask();
        GalleryUtils.initialize(this);
        WidgetUtils.initialize(this);
        PicasaSource.initialize(this);

        mStitchingProgressManager = LightCycleHelper.createStitchingManagerInstance(this);
        if (mStitchingProgressManager != null) {
            mStitchingProgressManager.addChangeListener(getDataManager());
        }

        com.jrdcom.mediatek.gallery3d.util.MediatekFeature.initialize(this);
        
        registerStorageReceiver();
        MyData.setAPKPathToNDK(getApplicationContext());
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }

    @Override
    public synchronized DataManager getDataManager() {
        if (mDataManager == null) {
            mDataManager = new DataManager(this);
            mDataManager.initializeSourceMap();
        }
        return mDataManager;
    }

    @Override
    public StitchingProgressManager getStitchingProgressManager() {
        return mStitchingProgressManager;
    }

    @Override
    public ImageCacheService getImageCacheService() {
        // This method may block on file I/O so a dedicated lock is needed here.
        synchronized (mLock) {
            if (mImageCacheService == null) {
                mImageCacheService = new ImageCacheService(getAndroidContext());
            }
            return mImageCacheService;
        }
    }

    @Override
    public synchronized ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            mThreadPool = new ThreadPool();
        }
        return mThreadPool;
    }

    @Override
    public synchronized DownloadCache getDownloadCache() {
        if (mDownloadCache == null) {
            //File cacheDir = new File(getExternalCacheDir(), DOWNLOAD_FOLDER);
            File cacheDir = new File(MtkUtils.getMTKExternalCacheDir(this), DOWNLOAD_FOLDER);

            if (!cacheDir.isDirectory()) cacheDir.mkdirs();

            if (!cacheDir.isDirectory()) {
                throw new RuntimeException(
                        "fail to create: " + cacheDir.getAbsolutePath());
            }
            mDownloadCache = new DownloadCache(this, cacheDir, DOWNLOAD_CAPACITY);
        }
        return mDownloadCache;
    }

    private void initializeAsyncTask() {
        // AsyncTask class needs to be loaded in UI thread.
        // So we load it here to comply the rule.
        try {
            Class.forName(AsyncTask.class.getName());
        } catch (ClassNotFoundException e) {
        }
    }

    private static final String TAG = "GalleryAppImpl";
    // M: for closing cache when SD card got unmounted
    private BroadcastReceiver mStorageReceiver;

    // M: for closing/re-opening cache
    private void registerStorageReceiver() {
        MtkLog.d(TAG, ">> registerStorageReceiver");
        // register BroadcastReceiver for SD card mount/unmount broadcast
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");
        mStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleStorageIntentAsync(intent);
            }
        };
        registerReceiver(mStorageReceiver, filter);
        MtkLog.d(TAG, "<< registerStorageReceiver: receiver registered");
    }
    
    private void handleStorageIntentAsync(final Intent intent) {
        new Thread() {
            public void run() {
                String action = intent.getAction();
                String storagePath = intent.getData().getPath();
                String defaultPath = MtkUtils.getMtkDefaultPath();
                MtkLog.d(TAG, "storage receiver: action=" + action);
                MtkLog.d(TAG, "intent path=" + storagePath + ", default path=" + defaultPath);
                
                if (storagePath == null || !storagePath.equalsIgnoreCase(defaultPath)) {
                    MtkLog.w(TAG, "ejecting storage is not cache storage!!");
                    return;
                }
                if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
                    // close and disable cache
                    MtkLog.i(TAG, "-> closing CacheManager");
                    CacheManager.storageStateChanged(false);
                    MtkLog.i(TAG, "<- closing CacheManager");
                    // clear refs in ImageCacheService
                    if (mImageCacheService != null) {
                        MtkLog.i(TAG, "-> closing cache service");
                        mImageCacheService.closeCache();
                        MtkLog.i(TAG, "<- closing cache service");
                    }
                } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                    // enable cache but not open it explicitly
                    MtkLog.i(TAG, "-> opening CacheManager");
                    CacheManager.storageStateChanged(true);
                    MtkLog.i(TAG, "<- opening CacheManager");
                    // re-open cache in ImageCacheService
                    if (mImageCacheService != null) {
                        MtkLog.i(TAG, "-> opening cache service");
                        mImageCacheService.openCache();
                        MtkLog.i(TAG, "<- opening cache service");
                    }
                } else {
                    MtkLog.w(TAG, "undesired action '" + action + "' for storage receiver!");
                }
            }
        }.start();
    }
    
    //FR450344 add by yanlong for popupwindow
    private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

    public WindowManager.LayoutParams getMywmParams() {
        return wmParams;
    }
    public void setMywmParams(WindowManager.LayoutParams params) {
        wmParams=params;
    }
    //FR450344-yanlong.li@tcl.com -end

}
