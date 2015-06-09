package com.jrdcom.example.joinpic;

import android.content.Context;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.Toast;
// xiaodaijun PR675235 start
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
import com.jrdcom.mt.util.MyData;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
import com.mediatek.storage.StorageManagerEx;
// xiaodaijun PR675235 end

//PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 start
import android.os.storage.StorageVolume;
//PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 end

/**
 *
 * Utils class
 * @author jony
 *
 */
public class Utils {
    public static final String TAG = "Utils";
    public static final long PREPARING = -2L;
    public static final long UNKNOWN_SIZE = -3L;
    public static final long FULL_SDCARD = -4L;
    private static StorageManager mStorageManager;
    // PR932423 Gallery Force close while edit photo add by limin.zhuo at 20150303 begin
    private static final String ICS_STORAGE_PATH_SD1 = "/mnt/sdcard";
    private static final String ICS_STORAGE_PATH_SD2 = "/mnt/sdcard2";
    private static final String STORAGE_PATH_SD1 = "/storage/sdcard0";
    private static final String STORAGE_PATH_SD2 = "/storage/sdcard1";
    // PR932423 Gallery Force close while edit photo add by limin.zhuo at 20150303 end

    private static StorageManager getStorageManager(){
        if (mStorageManager == null) {
            try {
                mStorageManager = new StorageManager(null, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return mStorageManager;
    }

    public static boolean hasAvailableSpace(String path){
        Long mAvaliableSppace = getAvailableMountSpace(path);
        long leftSpace = mAvaliableSppace.longValue();
        Log.i(TAG, "left space: " + leftSpace);
        if (leftSpace <= 1000000) {
            return false;
        }else {
            return true;
        }
    }

    public static long getAvailableMountSpace(String mountPath){
        String state;
        StorageManager storageManager = getStorageManager();
        state = storageManager.getVolumeState(mountPath);
        Log.v(TAG, "Storage state= " + state + ", mount point = " + mountPath);
        if (Environment.MEDIA_CHECKING.equals(state)) {
            return PREPARING;
        }
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return UNKNOWN_SIZE;
        }
        try {
            StatFs statFs = new StatFs(mountPath);
            Log.i(TAG, "Total: " + statFs.getAvailableBlocks() * (long)statFs.getBlockSize() + "    Total2:  " + statFs.getAvailableBlocksLong() + "  Total3:" +   statFs.getBlockSizeLong());
            return statFs.getAvailableBlocks() * (long)statFs.getBlockSize();//unit byte
        } catch (Exception e) {
            Log.i(TAG, "Fail to access external storage",e);
        }
        return UNKNOWN_SIZE;
    }

    public static void showToast(Context context,int resId){
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show();
    }

    // xiaodaijun PR675235 start
    public static String getDefaultPath(){
        return StorageManagerEx.getDefaultPath();
    }
    // xiaodaijun PR675235 end

    //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 start
    public static final String SEPARATOR = "/";
    public static String getDescriptionPath(Context context, String path) {
        try {
            Log.w(TAG, "getDescriptionPath path =" + path);
            StorageManager storageManager = getStorageManager();
            StorageVolume[] storageVolumeList = storageManager.getVolumeList();
            if (storageVolumeList != null) {
                for (StorageVolume volume : storageVolumeList) {
                    String mDescription;
                    String mPath;
                    mDescription = volume.getDescription(context);
                    mPath = volume.getPath();
                    if ((path + SEPARATOR).startsWith(mPath + SEPARATOR)) {
                        return path.length() > mPath.length() + 1 ? mDescription
                            + SEPARATOR + path.substring(mPath.length() + 1) : mDescription;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "getDescriptionPath Exception =",e);
        }
        return path;
    }
    //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 end

    // PR932423 Gallery Force close while edit photo add by limin.zhuo at 20150303 begin
    public static boolean ishasAvailableSpaceFromPath(String fileName) {
        if (fileName == null)
                return false;

        String path = null;
        if (fileName.startsWith(ICS_STORAGE_PATH_SD1)) {
            path = ICS_STORAGE_PATH_SD1;
        } else if (fileName.startsWith(ICS_STORAGE_PATH_SD2)) {
            path = ICS_STORAGE_PATH_SD2;
        } else if (fileName.startsWith(STORAGE_PATH_SD1)) {
            path = STORAGE_PATH_SD1;
        } else if (fileName.startsWith(STORAGE_PATH_SD2)) {
            path = STORAGE_PATH_SD2;
        }

        if (path == null) {
            return false;
        }

        return hasAvailableSpace(path);
    }
    // PR932423 Gallery Force close while edit photo add by limin.zhuo at 20150303 end

    //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
    public static boolean updateCacheDirEditPicture()
    {
        long valuesize = 10000000; // when the storage is 10m space, the edit picture cache is noraml
        String cachePath = null;
        String defaultPath = StorageManagerEx.getExternalStoragePath();
        Long mAvaliableSppace = getAvailableMountSpace(defaultPath);
        long leftSpace = mAvaliableSppace.longValue();
        if (leftSpace >= valuesize ) {
            cachePath = defaultPath;
        }else {
            defaultPath = StorageManagerEx.getInternalStoragePath();
            mAvaliableSppace = getAvailableMountSpace(defaultPath);
            leftSpace = mAvaliableSppace.longValue();
            if (leftSpace >= valuesize) {
                cachePath = defaultPath;
            }
        }
        if (cachePath == null) {
            return false;
        }
        MyData.getBeautyControl().m_cacheDirction = cachePath;
        return true;
    }
    //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
}
