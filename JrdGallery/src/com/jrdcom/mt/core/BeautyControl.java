package com.jrdcom.mt.core;

import android.graphics.Bitmap;
import android.util.Log;

import com.jrdcom.mt.util.MyData;
import com.mt.mtxx.image.JNI;
/**
 * 主界面功能的控制类
 * @author aidy
 *
 */
public class BeautyControl 
{
	JNI m_jni = null;			//so库
	public boolean m_bIsLoadPic = false;		//是否加载完图片
        //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
        public String m_cacheDirction = null; // 编辑图片的缓冲路径
        //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
	//初始化的时候先加载so库
	public void init(JNI jni)
	{
		m_jni = jni;
		m_bIsLoadPic = false;
	}

	public static int cacheRefCount = -1;//add by biao.luo
    /**
     * 从绝对路径中加载图片
     * @param path               图片文件在硬盘的绝对路径
     * @param nMaxShowWidth     为“UI图”显示的最大宽
     * @param nMaxShowHeight    为“UI图”显示的最大高
     * @param nMaxWidth          为“保存图”的最大宽
     * @param nMaxHeight         为“保存图”的最大高
     * @return          返回1表示加载成功，返回0表示加载失败
     */
	public int initWithImagePath(String path,int nMaxShowWidth,int nMaxShowHeight,int nMaxWidth,int nMaxHeight)
	{
		//已经加载,则返回0
		if (m_bIsLoadPic == true)
		{
			return 0;
		}
		
		//清理内存
		clearMemory();

		// 获取加载图片的EXIF信息里的方向值
		String strOrival = BitmapUtil.getOrientation(path);
		int ori = Integer.parseInt(strOrival);
		//NDK加载图片
		int bRetn = m_jni.initImageWithPath(path, ori, nMaxShowWidth, nMaxShowHeight, nMaxWidth, nMaxHeight);
		if (bRetn == 0)
		{
			return bRetn;
		}
		
		m_bIsLoadPic = true;
		return bRetn;
	}
	/**
	 * 每个小功能点确定按钮后调用该函数
	 */
	public void pushImage()
	{
                //PR936992 Undo and Redo icon are useless,while SD card is full modified by limin.zhuo at 2015.03.06 begin 
                String str = m_cacheDirction+"/.temp"+(++cacheRefCount);
                //PR936992 Undo and Redo icon are useless,while SD card is full modified by limin.zhuo at 2015.03.06 end
		MyData.addCachePath(str);
		int nType = 0;
		int nOrgType = 1;
		
		//保存主功能里的数据到SD卡上的临时路径上
		//str存放的路径；nType为0表示UI显示的图片，1表示UI真实的图片
		MyData.mCurr_index++;
		m_jni.saveImageDataToDisk(str, nType);
		m_jni.saveImageDataToDisk(str, nOrgType);
		
		//将SD卡上的临时路径上的数据导入给主功能的数据
		//str存放的路径；nType为0表示UI显示的图片，1表示UI真实的图片
//		m_jni.loadImageDataFromDisk(str, nType);
		
	}
	//将SD卡上的临时路径上的数据导入给主功能的数据
	public void pullImage()
	{
	    String strpath = MyData.getCachePath();
	    if(strpath == null || strpath.trim().equals(""))return;
	    m_jni.loadImageDataFromDisk(strpath,0); 
	    m_jni.loadImageDataFromDisk(strpath,1); 
	}
	public void getPreCacheImage()
	{
	    MyData.mCurr_index--;
	    if( MyData.mCurr_index<0) {
	        MyData.mCurr_index = 0;
	    }else {
	        MyData.isOral = false;
        }
	    pullImage();
	}
	public void getNextCacheImage()
	{
	    MyData.mCurr_index++;
	    if(MyData.mCurr_index>MyData.cacheBitmaps.size()-1)
	        MyData.mCurr_index  = MyData.cacheBitmaps.size()-1;
	    pullImage();
	}

    public Bitmap test(Bitmap pImage,int val[])
    {
        //特效缩略图的操作
//        ToolEffect.ThumbnailWithEffectProc(pImage,2);
//        return pImage;
        //简单边框的缩略图操作
//        String strPath = "img_frame/10019000.mtxbk";
//        pImage = ToolFrame.ThumbnailWithSimleFrame(pImage,strPath,false);
//        return pImage;
        //炫彩边框的缩略图操作
        String strPath = "colorful/10029000.jpg";
        ToolFrame.ThumbnailWithColorFrame(pImage,strPath,1,false);
        return pImage;
    }
	/* 
	 * 获取当前步骤的"UI图",主要是UI显示用的
	 */
	public Bitmap getCurrentShowImage()
	{
        //1.1修改
        if (m_bIsLoadPic == false)
        {
            return null;
        }
        try{
            int val[] = m_jni.getCurrentShowImageSize();
            if (val[0]*val[1] == 0)
            {
                return null;
            }
            Bitmap pImage = Bitmap.createBitmap(val[0], val[1], Bitmap.Config.ARGB_8888);
            m_jni.getCurrentShowImage(pImage);
            val = null;
            return pImage;

        }catch(OutOfMemoryError e){
            Log.w("BeautyControl"," getCurrentShowImage :out of memory when decoding:"+e);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;


//		try{
//			int val[] = new int[2];
//			val = m_jni.getCurrentImageSize();
//	    	Bitmap bmp = ImageProcess.intARGB2Bitmap(data, val[0], val[1]);
//	    	data = null;
//	    	val = null;
//	    	System.gc();
//	    	return bmp;
//		}catch(Exception e)
//		{
//		}
//		return null;
	}
	/* 
	 * 获取当前步骤的“保存图”
	 */
	public Bitmap getCurrentRealImage()
	{
        //1.1修改
		if (m_bIsLoadPic == false)
		{
			return null;
		}
		try{
            int val[] = m_jni.getCurrentImageSize();
            if (val[0]*val[1] == 0)
            {
                return null;
            }
            Bitmap pImage = Bitmap.createBitmap(val[0], val[1], Bitmap.Config.ARGB_8888);
            m_jni.getCurrentImage(pImage);
            val = null;
            return pImage;
//			int val[] = new int[2];
//	    	int data[] = m_jni.getCurrentImageData(val);
//	    	if(data == null){
//	    		return null;
//	    	}
//	    	Bitmap bmp = ImageProcess.intARGB2Bitmap(data, val[0], val[1]);
//	    	data = null;
//	    	val = null;
//	    	System.gc();
//	    	return bmp;
		}catch(Exception e){
		}
		return null;
	}
	

	/**
	 * 保存与分享，保存到本地的函数接口
	 * 保存图片
	 * @param strPath 为保存的路径
	 * @return 判断保存是否成功
	 */
	public int saveImage(String strPath)
	{
		int bRetn = 1;
		//第二个参数：0表示代表原图保存，非0表示图片的最大边为该值
		bRetn = m_jni.saveImageWithPath(strPath,0,100);
		return bRetn;
	}
	
	/**
	 * 上传图片的接口，将先要上传的图片保存到临时文件里，再上传
	 * @param strPath	为图片保存的绝对路径
	 * @param nMaxlen	当值为0时则为原始图片数据，当值大于0时，则图片的宽或高最大值就为nMaxlen
	 * @param nQuality	图片保存质量，参数为60-100，最高质量为100,默认为85
	 * @return	是否保存成功
	 */
	public int saveImageWithUpload(String strPath,int nMaxlen,int nQuality)
	{
		int bRetn = 1;
		bRetn = m_jni.saveImageWithPath(strPath,nMaxlen,nQuality);
		return bRetn;
	}


	/*
	 * 释放内存与清除缓存
	 */
	public void clearMemory()
	{
		if (m_bIsLoadPic == true)
		{
			m_bIsLoadPic = false;
			m_jni.ReleaseControlMemory();
		}

	}
}
