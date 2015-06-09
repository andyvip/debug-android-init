
package com.jrdcom.mt.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.*;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * 文件操作工具类
 * 
 * @author Javan.Eu
 * @since 下午5:40:18 2012-9-10
 */
public class FileUtils {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    private static String porotion = null;
    private static String filePathString = null;

    public static boolean isFileExistingOnExternalStorage(final String pFilePath) {
        if (FileUtils.isExternalStorageReadable()) {
            final String absoluteFilePath = FileUtils.getAbsolutePathOnExternalStorage(pFilePath);
            final File file = new File(absoluteFilePath);
            return file.exists() && file.isFile();
        } else {
            throw new IllegalStateException("External Storage is not readable.");
        }
    }

    public static boolean isFileExistingOnExternalStorage(final Context pContext,
            final String pFilePath) {
        if (FileUtils.isExternalStorageReadable()) {
            final String absoluteFilePath = FileUtils.getAbsolutePathOnExternalStorage(pContext,
                    pFilePath);
            final File file = new File(absoluteFilePath);
            return file.exists() && file.isFile();
        } else {
            throw new IllegalStateException("External Storage is not readable.");
        }
    }

    public static boolean isDirectoryExistingOnExternalStorage(final Context pContext,
            final String pDirectory) {
        if (FileUtils.isExternalStorageReadable()) {
            final String absoluteFilePath = FileUtils.getAbsolutePathOnExternalStorage(pContext,
                    pDirectory);
            final File file = new File(absoluteFilePath);
            return file.exists() && file.isDirectory();
        } else {
            throw new IllegalStateException("External Storage is not readable.");
        }
    }

    public static boolean ensureDirectoriesExistOnExternalStorage(final Context pContext,
            final String pDirectory) {
        if (FileUtils.isDirectoryExistingOnExternalStorage(pContext, pDirectory)) {
            return true;
        }

        if (FileUtils.isExternalStorageWriteable()) {
            final String absoluteDirectoryPath = FileUtils.getAbsolutePathOnExternalStorage(
                    pContext, pDirectory);
            return new File(absoluteDirectoryPath).mkdirs();
        } else {
            throw new IllegalStateException("External Storage is not writeable.");
        }
    }

    public static InputStream openOnExternalStorage(final String pFilePath)
            throws FileNotFoundException {
        final String absoluteFilePath = FileUtils.getAbsolutePathOnExternalStorage(pFilePath);
        return new FileInputStream(absoluteFilePath);
    }

    public static InputStream openOnExternalStorage(final Context pContext, final String pFilePath)
            throws FileNotFoundException {
        final String absoluteFilePath = FileUtils.getAbsolutePathOnExternalStorage(pContext,
                pFilePath);
        return new FileInputStream(absoluteFilePath);
    }

    public static String[] getDirectoryListOnExternalStorage(final Context pContext,
            final String pFilePath) throws FileNotFoundException {
        final String absoluteFilePath = FileUtils.getAbsolutePathOnExternalStorage(pContext,
                pFilePath);
        return new File(absoluteFilePath).list();
    }

    public static String[] getDirectoryListOnExternalStorage(final Context pContext,
            final String pFilePath, final FilenameFilter pFilenameFilter)
            throws FileNotFoundException {
        final String absoluteFilePath = FileUtils.getAbsolutePathOnExternalStorage(pContext,
                pFilePath);
        return new File(absoluteFilePath).list(pFilenameFilter);
    }

    public static String getAbsolutePathOnInternalStorage(final Context pContext,
            final String pFilePath) {
        return pContext.getFilesDir().getAbsolutePath() + pFilePath;
    }

    public static String getAbsolutePathOnExternalStorage(final String pFilePath) {
        return Environment.getExternalStorageDirectory() + "/" + pFilePath;
    }

    public static String getAbsolutePathOnExternalStorage(final Context pContext,
            final String pFilePath) {
        return Environment.getExternalStorageDirectory() + "/Android/data/"
                + pContext.getApplicationInfo().packageName + "/.files/" + pFilePath;
    }

    public static boolean isExternalStorageWriteable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean isExternalStorageReadable() {
        final String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED)
                || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    /**
     * Deletes all files and sub-directories under <code>dir</code>. Returns
     * true if all deletions were successful. If a deletion fails, the method
     * stops attempting to delete and returns false.
     * 
     * @param pFileOrDirectory
     * @return
     */
    public static boolean deleteDirectory(final File pFileOrDirectory) {
        if (pFileOrDirectory.isDirectory()) {
            final String[] children = pFileOrDirectory.list();
            final int childrenCount = children.length;
            for (int i = 0; i < childrenCount; i++) {
                final boolean success = FileUtils.deleteDirectory(new File(pFileOrDirectory,
                        children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return pFileOrDirectory.delete();
    }

    public static boolean deleteDirectory(final String pFilePath) {
        final File fileOrDirectory = new File(pFilePath);
        if (fileOrDirectory.isDirectory()) {
            final String[] children = fileOrDirectory.list();
            final int childrenCount = children.length;
            for (int i = 0; i < childrenCount; i++) {
                final boolean success = FileUtils.deleteDirectory(new File(fileOrDirectory,
                        children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return fileOrDirectory.delete();
    }

    /**
     * 
     * @param prefix if null will generate automiclly ,if not null append the parameter to the head of name
     * @return full path that describes the path of saving pictrues
     */
    public static String generateToSaveFileName(String prefix)
    {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMDD_hhmmss");
        porotion = sdf.format(date);
        //add by biao.luo begin
        //Modify the file save path
//        String filenameString = "";
        String fileString = "";
        
        StringBuffer sBuffer = new StringBuffer();
        prefix = null == prefix ? "mnt/sdcard/Pictures" : prefix;
        File tempFile = new File(prefix);
        if(tempFile != null)
        {
//            filenameString = tempFile.getName();
//            filenameString = filenameString.substring(0, filenameString.lastIndexOf("."));
            fileString = tempFile.getName().substring(tempFile.getName().lastIndexOf("."), tempFile.getName().length());
            prefix = tempFile.getParent()+"/";
        }
        
        sBuffer.append(prefix);
        sBuffer.append("IMG"+porotion);
        sBuffer.append(fileString);
        filePathString = sBuffer.toString();
        //add by biao.luo end
        return sBuffer.toString();
    }
    //add by biao.luo begin
    //Updated gallery database
    public static void scanDirAsync(Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePathString);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
    public static Uri getUri() {
        if (filePathString != null) {
            File f = new File(filePathString);
            Uri contentUri = Uri.fromFile(f);
            return contentUri;
        }
        return null;
    }
    //add by biao.luo end
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
