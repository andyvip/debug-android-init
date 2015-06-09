package com.jrdcom.mt.core;

import com.mt.mtxx.image.JNI;

/**
 * 锐化功能
 * @author aidy
 *
 */
public class ToolSharp extends ToolBase
{
	private float m_fValue;//锐化的参数

    /**
     * 初始化
     * @param jni   NDK与JAVA的链接工具
     */
	public void init(JNI jni)
	{
		super.init(jni);
		m_fValue = 0.0f;
	}

	/**
	 * 处理锐化函数。
	 * @param fValue  锐化的程度值
	 * @param bIsPreview   true 处理预览图，false  处理真实图片
	 */
	public void procImage(float fValue,boolean bIsPreview)
	{
		if (bIsPreview == true)
		{
			m_fValue = fValue;
		}
		if (m_fValue != 0.0)
		{
			m_isProcessed = true;
		}
		else
		{
			m_isProcessed = false;
		}
		m_jni.ToolSharp(fValue,bIsPreview);
		
		
	}

    /**
     * 点击确定按钮
     */
	public void ok()
	{
		procImage(m_fValue, false);
		super.ok();
	}

    /**
     * 删除临时数据与内存
     */
	public void clear()
	{
		//释放内存
		m_jni.ToolTempDataClear();
		super.clear();
	}
	
	
}
