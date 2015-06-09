package com.jrdcom.mt.core;


import android.util.Log;

import com.mt.mtxx.image.JNI;
/**
 * 裁剪功能
 * @author aidy
 *
 */
public class ToolCut extends ToolBase
{
	private float m_fValues[] = new float[4];   //裁剪的四个区域的坐标

    /**
     * 初始化
     * @param jni   NDK与JAVA的链接工具
     */
	public void init(JNI jni)
	{
		super.init(jni);
		for (int i=0;i<4;i++)
		{
			m_fValues[i] = 0.0f;
		}
	}


	/**
	 * 处理图片函数
	 * @param pValues 需要裁剪区域的4个点位置， left，right，top，bottom的百分比
	 * @param bIsPreview        是否是预览效果
	 */
	public void procImage(float pValues[],boolean bIsPreview)
	{
		if (bIsPreview == true)
		{
			for(int i = 0; i < pValues.length; i++)
			{
				m_fValues[i] = pValues[i];
			}			
		}

		m_jni.ToolCut(pValues,pValues.length,bIsPreview);
		m_isProcessed = true;
	}

    /**
     * 点击确定按钮
     */
	public void ok()
	{
        if (isProcessed() == true)
        {
            //由于保存用的是UI的，这边要真实的也来一次
            procImage(m_fValues, false);
            super.ok();
        }
	}
}
