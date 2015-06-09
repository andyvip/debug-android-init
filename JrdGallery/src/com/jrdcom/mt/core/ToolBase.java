package com.jrdcom.mt.core;

import android.graphics.Bitmap;

import com.mt.mtxx.image.JNI;

/**
 * 子功能的基础类
 * 
 * @author aidy
 * 
 */
public class ToolBase {
	public JNI m_jni;
	public boolean m_isProcessed; // 是否进行处理了

    /**
     * 工具初始化
     * @param jni   NDK与JAVA的链接工具
     */
	public void init(JNI jni) {
		m_jni = jni;
		m_isProcessed = false;
		m_jni.initProcImageData();
	}

	/**
	 * 获取UI显示的处理效果图片
	 * 
	 * @return   UI显示的处理效果图片
	 */
	public Bitmap getShowProcImage() {
        //1.1版优化
        try{
            int val[] = m_jni.getShowProcImageSize();

            if (val[0]*val[1] == 0)
            {
                return null;
            }
            Bitmap pImage = Bitmap.createBitmap(val[0], val[1], Bitmap.Config.ARGB_8888);
            m_jni.getShowProcImage(pImage);
            val = null;
            return pImage;
        }catch(Exception e){
        }
        return null;
//		try {
//			int val[] = new int[2];
//			//获取UI显示的操作图片数据
//			int data[] = m_jni.getShowProcImageData(val);
//			if (data == null) {
//				return null;
//			}
//			Bitmap bmp = ImageProcess.intARGB2Bitmap(data, val[0], val[1]);
//			data = null;
//			val = null;
//			System.gc();
//			return bmp;
//		} catch (Exception e) {
//		}
//		return null;
	}

	/**
	 * 获取进入子功能的初始图片的UI显示图，主要是用来作对比用
	 * 
	 * @return   UI显示的原图效果图片
	 */
	public Bitmap getShowOralImage() {
        //1.1版优化
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

        }catch(Exception e)
        {}
        return null;
//		try {
//			int val[] = new int[2];
//			//获取UI显示的原始图片数据
//			int data[] = m_jni.getCurrentShowImageData(val);
//			if (data == null) {
//				return null;
//			}
//			Bitmap bmp = ImageProcess.intARGB2Bitmap(data, val[0], val[1]);
//			data = null;
//			val = null;
//			System.gc();
//			return bmp;
//		} catch (Exception e) {
//		}
//		return null;
	}

	/**
	 * 获取真实图片的宽和高
	 * @return	返回真实图片的宽和高
	 */
	public int[] getRealImageSize() 
	{
		return m_jni.getCurrentImageSize();
	}
	/**
	 * 获取UI显示图的宽和高
	 * @return	返回UI显示图片的宽和高
	 */
	public int[] getShowImageSize() 
	{
		return m_jni.getCurrentShowImageSize();
	}

    /**
     * 获取真实图与UI显示图的放缩比例
     * @return      返回real/show的比例
     */
	public float getScaleBetweenRealAndShow() 
	{
		int realSize[] = m_jni.getCurrentImageSize();
		int showSize[] = m_jni.getCurrentShowImageSize();
		float scale = realSize[0] / (float) showSize[0];
		return scale;
	}

	/**
	 * 判断是否进行过处理
	 * 
	 * @return true表示处理过，false表示没有处理过
	 */
	public boolean isProcessed() {
		return m_isProcessed;
	}

    /**
     * 点击确定按钮的操作
     */
	public void ok() {
		if (m_isProcessed == true) {
			m_jni.ok(2);
		}
		// 删除临时数据与内存
		clear();
	}

    /**
     * 点击取消按钮
     */
	public void cancel() {
		// 删除临时数据与内存
		clear();
	}

    /**
     * 删除临时数据与内存
     */
	public void clear() {
		m_jni.clearProcImageData();
		m_isProcessed = false;
	}

}
