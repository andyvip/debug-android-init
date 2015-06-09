package com.jrdcom.mt.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.net.Uri;
import com.jrdcom.mt.core.BeautyControl;
import com.mt.mtxx.image.JNI;

public class MyData 
{
	//NDK内部数据的控制类
	private static BeautyControl m_beautyControl = null;
	//获取调用NDK的定义
	private static JNI mJni;
	/**
	 * 屏幕宽度
	 */
	public static int nScreenW;
	/**
	 * 屏幕高度
	 */
	public static int nScreenH;
	/**
	 * 像素点密度
	 */
	public static float nDensity;
	/**
	 * 屏幕要使用的原始图片宽度
	 */
	public static int nBmpDstW;
	/**
	 * 屏幕要使用的原始图片高度度
	 */
	public static int nBmpDstH;
	/**
	 * 原图宽度
	 */
	public static int nSrcWidth = 0;
	/**
	 * 原图高度
	 */
	public static int nSrcHeight = 0;
	/**
	 * 裁剪缩放的比例总和 裁剪、旋转和边框会引起变化
	 */
	public static float fScaleCut = 1.0f;
	/**
	 * 最小输出图像宽高，这里宽高是相对的
	 */
	public static final int MIN_OUTPUT_WIDTH = 320;
	/**
	 * 最小输出图像宽高，这里宽高是相对的
	 */
	public static final int MIN_OUTPUT_HEIGHT = 480;
	/**
	 * 中等输出图像宽高，这里宽高是相对的
	 */
	public static final int MID_OUTPUT_WIDTH = 480;
	/**
	 * 中等输出图像宽高，这里宽高是相对的
	 */
	public static final int MID_OUTPUT_HEIGHT = 640;
	/**
	 * 最大输出图像宽高，这里宽高是相对的
	 */
	public static final int MAX_OUTPUT_WIDTH = 1500;
	/**
	 * 最大输出图像宽高，这里宽高是相对的
	 */
	public static final int MAX_OUTPUT_HEIGHT = 1500;
	/**
	 * 当前输出图片宽度
	 */
	public static int nOutPutWidth = MAX_OUTPUT_HEIGHT;
	/**
	 * 当前输出图片
	 */
	public static int nOutPutHeight = MAX_OUTPUT_HEIGHT;
	/**
	 * 打开的图片路径
	 */
	public static String strPicPath;
	
	 /** 图片保存路径
	  * 
	  */
	public static String mSavePicPath = "/sdcard/MTXX/";
	/**
	 * text orginal image
	 */
	public static Bitmap bmpDst = null;
	/**
	 * 获取美化的控制类
	 * 
	 * @return
	 */
	
	public static int nSDKVersion;
	public static String apkFilePath = "/custpack/JRD_custres/app/JrdGallery-res.apk";
	
	/**  add by biao.luo begin
	 * cache sub-function effect images
	 */
	public static int mCurr_index = -1;
	public static boolean isOral = false;
	public static List<String> cacheBitmaps =  new ArrayList<String>();
	
	public static Config mConfig = Config.ARGB_8888;
	
    // yaogang.hao for PR 544189
    public static boolean isImagevalidated = false;
    //yaogang.hao compare change
    public static Uri oldurl;
	
	/********** Cache Effect Images**************/
	public  static void clearCacheFiles()
	{
	    removeCacheImages(0);
	    cacheBitmaps.clear();
	    mCurr_index = -1;
	}
	
	public static void removeCahcheFiles(int offset)
    {
        removeCacheImages(offset);
        for(int i=cacheBitmaps.size()-1;i>=offset;i--)
        {
            cacheBitmaps.remove(offset);
        }
    }
	
	public static void removeCahcheFiles()
	{
	    int offset = mCurr_index+1;
	    removeCacheImages(offset);
	    for(int i=cacheBitmaps.size()-1;i>=offset;i--)
	    {
	        cacheBitmaps.remove(offset);
	    }
	}
	public static void addCachePath(String cachePath)
	{
	    cacheBitmaps.add(cachePath);
	}
	public static String getCachePath()
	{
	    if(cacheBitmaps.size()>0)
	    {
	        return cacheBitmaps.get(mCurr_index);
	    }
	    return null;
	    
	}
	public static void removeCacheImages(int offset)
	{
	    for(;offset<cacheBitmaps.size();offset++)
	    {
	        String tempPath = cacheBitmaps.get(offset);
	        File file = new File(tempPath);
	        if(file.exists())
	        {
	           file.delete(); 
	        }
	    }
	}
	
	/**********************************meitu xiuxiu**********************************/
	public static BeautyControl getBeautyControl() {
		if (mJni == null)
		{
			mJni = new JNI();
		}
		if (m_beautyControl == null) {
			m_beautyControl = new BeautyControl();
			m_beautyControl.init(mJni);
		}
		return m_beautyControl;
	}

	public static float getDensity(Activity context) {
		if (nDensity <= 0) {
			DisplayMetrics metric = new DisplayMetrics();
			context.getWindowManager().getDefaultDisplay().getMetrics(metric);
			nDensity = metric.density;// 屏幕密度（0.75 / 1.0 / 1.5）
		}
		return nDensity <= 0 ? 1 : nDensity;
	}

	
	



	/**
	 * 获取APK在磁盘里的路径
	 * @return	APK在磁盘里的路径
	 */
	public static String setAPKPathToNDK(Context mContext) {

		if (mJni == null)
		{
			mJni = new JNI();
		}
		ApplicationInfo appInfo = null;

		PackageManager packMgmr = mContext.getPackageManager();
		try {


			//美图必备
			appInfo = packMgmr.getApplicationInfo("com.jrdcom.android.gallery3d", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to locate assets, aborting...");
		}
//		apkFilePath = appInfo.sourceDir;
        final File f = new File(apkFilePath);
        if (!f.isFile()){
            apkFilePath = appInfo.sourceDir;
        }
		mJni.SetAPKPath(apkFilePath);
		mJni.SetAPKPathForPuzzle(apkFilePath);
		return apkFilePath;
		// jni.init(apkFilePath);// pass it to NDK
	}
    public static int NDKCheckColorARGB8888Index(InputStream io)
    {
        int nRetn = 0;
        if (mJni == null)
        {
            mJni = new JNI();
        }
        //校验NDK里ARGB的顺序
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = 1;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;

        // add by Javan.Eu 2013-1-12 解决内存问题
        // 2. inPurgeable 设定为 true，可以让java系统, 在内存不足时先行回收部分的内存
        opts.inPurgeable = true;
        // 与inPurgeable 一起使用
        opts.inInputShareable = true;
        try {
            Bitmap pTempBitmap = BitmapFactory.decodeStream(io, null, opts);
            nRetn = mJni.NDKCheckColorARGB8888Index(pTempBitmap);
            pTempBitmap.recycle();
            pTempBitmap = null;
        } catch (OutOfMemoryError e) {
            Log.w("MyData"," getCurrentShowImage :out of memory when decoding:"+e);
        }

        return nRetn;
    }
	/**
	 * 获取调用NDK的定义
	 * @return
	 */
	public static JNI getJNI() {
        if (mJni == null)
        {
            mJni = new JNI();
        }
		return mJni;
	}
}
