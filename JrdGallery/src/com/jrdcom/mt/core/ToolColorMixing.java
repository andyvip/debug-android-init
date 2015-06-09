package com.jrdcom.mt.core;

import com.mt.mtxx.image.JNI;

/**
 * 调色功能
 * @author aidy
 *
 */
public class ToolColorMixing extends ToolBase
{
	private int m_nValueTotal = 4;
	private float m_values[] = new float[m_nValueTotal];

    /**
     * 功能初始化
     * @param jni   NDK与JAVA的链接工具
     */
	public void init(JNI jni)
	{
		super.init(jni);
		for (int i=0;i<m_nValueTotal;i++)
		{
			m_values[i] = 0.0f;
		}
	}
	/**
	 * 功能的具体实现
	 * @param pValues	
	 * 						参数为四个，
	 * 						第一个为亮度[-128,128]
	 * 						第二个为对比度[-100,100]
	 * 						第三个为饱和度[-100,100]
	 * 						第四个为曝光度[-100,100]
	 * @param length		如果用到前三个就用3，若有用到第四个，则用4，与pValues数组的大小一致
	 * @param bIsPreview	true表示是UI显示的操作，false表示真实图要操作
	 */
	public void procImage(float pValues[],int length,boolean bIsPreview)
	{
		if (bIsPreview == true)
		{
			for (int i=0;i<length;i++)
			{
				m_values[i] = pValues[i];
			}		
		}
		m_nValueTotal = length;
		m_isProcessed = true;
		m_jni.ToolColorMixing(pValues, length, bIsPreview);
	}

    /**
     * 点击确定按钮
     */
	public void ok()
	{
		procImage(m_values, m_nValueTotal, false);//真实图的操作
		super.ok();
	}
	
	
}
