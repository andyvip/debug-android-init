package com.jrdcom.mt.core;

import android.graphics.Bitmap;
import com.mt.mtxx.image.JNI;

/**
 * 亮眼功能
 * @author aidy
 *
 */

public class ToolBrightEyes extends ToolBase 
{
//	/**
//	 * 处理函数
//	 * @param maskData	蒙版的数据，用白色来表色
//	 * @param nWidth	蒙版数据的宽
//	 * @param nHeight	蒙版数据的高
//	 */
//	public void procImage(int[] maskData,int nWidth,int nHeight)
//	{
//		m_isProcessed = true;
//		m_jni.ToolBrightEyesMixing(maskData,nWidth,nHeight,0.6f);
//	}

    /**
     *  处理函数
     * @param pImage    蒙版的数据，用白色来表示
     */
    public void procImage(Bitmap pImage)
    {
        m_isProcessed = true;
        m_jni.ToolBrightEyesMixingWithImage(pImage,0.6f);
    }
	//删除临时数据与内存
	public void clear()
	{
		//释放临时数据
		m_jni.ToolTempDataClear();
		super.clear();
	}
}

