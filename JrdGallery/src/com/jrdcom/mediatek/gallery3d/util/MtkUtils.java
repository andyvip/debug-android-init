package com.jrdcom.mediatek.gallery3d.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Debug.MemoryInfo;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.mediatek.storage.StorageManagerEx;

public class MtkUtils {
    private static final String TAG = "Gallery3D/MtkUtils";
    private static final boolean LOG = true;

    public static final String URI_FOR_SAVING = "UriForSaving";
    
    private MtkUtils() {}
    
    public static void logMemory(String title) {
        MemoryInfo mi = new MemoryInfo();
        android.os.Debug.getMemoryInfo(mi);
        String tagtitle = "logMemory() " + title;
        MtkLog.v(TAG, tagtitle + "         PrivateDirty    Pss     SharedDirty");
        MtkLog.v(TAG, tagtitle + " dalvik: " + mi.dalvikPrivateDirty + ", " + mi.dalvikPss
                + ", " + mi.dalvikSharedDirty + ".");
        MtkLog.v(TAG, tagtitle + " native: " + mi.nativePrivateDirty + ", " + mi.nativePss
                + ", " + mi.nativeSharedDirty + ".");
        MtkLog.v(TAG, tagtitle + " other: " + mi.otherPrivateDirty + ", " + mi.otherPss
                + ", " + mi.otherSharedDirty + ".");
        MtkLog.v(TAG, tagtitle + " total: " + mi.getTotalPrivateDirty() + ", " + mi.getTotalPss()
                + ", " + mi.getTotalSharedDirty() + ".");
    }
    
    private static final String EXTRA_CAN_SHARE = "CanShare";
    public static boolean canShare(Bundle extra) {
        boolean canshare = true;
        if (extra != null) {
            canshare = extra.getBoolean(EXTRA_CAN_SHARE, true);
        }
        if (LOG) {
            MtkLog.v(TAG, "canShare(" + extra + ") return " + canshare);
        }
        return canshare;
    }

    private static StorageManager sStorageManager = null;
    public static File getMTKExternalCacheDir(Context context) {
        if (context == null) {
            return null;
        }
        if (sStorageManager == null) {
            sStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        
      //FR 576703 add by xiangchen begin
        File ret = StorageManagerEx.getExternalCacheDir(context.getPackageName());
      //FR 576703 add by xiangchen end
        if (ret == null)
            return null;
      //FR 576703 add by xiangchen begin
        String internalStoragePath = StorageManagerEx.getInternalStoragePath();
      //FR 576703 add by xiangchen end
        if (internalStoragePath == null)
            return null;
        String cachePath = ret.getAbsolutePath();
        cachePath = internalStoragePath
                + cachePath.substring(internalStoragePath.length(), cachePath.length());
        ret = new File(cachePath);
        if (ret.exists())
            return ret;
        if (ret.mkdirs())
            return ret;
        MtkLog.v(TAG, "<getMTKExternalCacheDir> can not create external cache dir");
        return null;
    }

    public static String getMtkDefaultPath() {
        String path = StorageManagerEx.getDefaultPath();//fengke change
        if (LOG) {
            MtkLog.v(TAG, "getMtkDefaultPath() return " + path);
        }
        return path;
    }

    //PR681652-taoli-begin 001
    public static String getInternalStoragePath(){
        String path = StorageManagerEx.getInternalStoragePath();
        if (LOG) {
            Log.e(TAG, "Path: " + path);
        }
        return path;
    }
    //PR681652-taoli-end 001

    public static String getMtkDefaultStorageState(Context context) {
        if (sStorageManager == null && context == null) {
            return null;
        }
        if (sStorageManager == null) {
            sStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
        String path = StorageManagerEx.getDefaultPath();//fengke change
        if (path == null) {
            return null;
        }
        String volumeState = sStorageManager.getVolumeState(path);
        if (LOG) {
            MtkLog.v(TAG, "getMtkDefaultStorageState: default path=" + path + ", state=" + volumeState);
        }
        return volumeState;
    }
    
    public static boolean isSupport3d() {
        boolean support = false; // com.mediatek.common.featureoption.FeatureOption.MTK_S3D_SUPPORT; // for baseline upgrade leiming
        MtkLog.w(TAG, "isSupport3d() return " + support);
        return support;
    }    
    public static final int UNKNOWN = -1;

    public static final String BITMAP_DUMP_PATH = Environment.getExternalStorageDirectory().toString();
    public static void dumpBitmap(Bitmap bitmap, String string) {
        File file = new File(BITMAP_DUMP_PATH, string);
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "cannot create fos");
        }
        bitmap.compress(CompressFormat.PNG, 100, fos);
    }
}
