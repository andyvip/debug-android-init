package com.jrdcom.mt.core;

import android.graphics.Bitmap;
import com.mt.mtxx.image.JNI;


/**
 * 祛痘祛斑
 * @author aidy
 *
 */
public class ToolRemoveBeverageAndAcne extends ToolBase 
{
//	/**
//	 * 处理函数
//	 * @param maskData	蒙版的数据，用白色来表示
//	 * @param nWidth	蒙版数据的宽
//	 * @param nHeight	蒙版数据的高
//	 */
//	public void procImage(int[] maskData,int nWidth,int nHeight)
//	{
//		m_isProcessed = true;
//		m_jni.ToolRemoveBeverageAndAcne(maskData,nWidth,nHeight,150,20);
//	}

    public void procImage(Bitmap pImage)
    {
        m_isProcessed = true;
        m_jni.ToolRemoveBeverageAndAcneWithImage(pImage,150,20);
    }
}
