package com.jrdcom.mt.core;

import com.mt.mtxx.image.JNI;


/**
 * 磨皮美白肤色
 * @author aidy
 *
 */
public class ToolCosmesis extends ToolBase 
{
	//为了提高速度，磨皮美白临时存储数据的控制类
	private ImageDiskCache m_imageDiskCache = null;
	//具体的参数个数
	private int m_nValueLen = 4;
	private float m_values[] = new float[4];

    /**
     * 功能的初始化函数
     * @param jni   NDK与JAVA的链接工具
     */
	public void init(JNI jni)
	{
		super.init(jni);
		m_imageDiskCache = new ImageDiskCache();
		m_imageDiskCache.createCacheFolder("CosmesisTool");		
	}

    /**
     * 磨皮美白的操作函数
     * @param pValues    	总共四个参数，
     *                     第一个参数为图片的处理半径,固定为13，第二个参数为美白的index（范围为0-10）
     *                     第三个参数为肤色的index（范围为0-10）,第四个参数为磨皮的百分比（范围为0-10）
     * @param length      pValues的参数个数
     * @param bIsPreview    是否是预览图操作
     */
	public void procImage(float pValues[],int length,boolean bIsPreview)
	{
		if (length != m_nValueLen)
		{
			return ;
		}
		if (bIsPreview == true)
		{
			for (int i=0;i<length;i++)
			{
				m_values[i] = pValues[i];
			}		
		}
		m_isProcessed = true;
		m_jni.ToolCosmesisProcess(m_imageDiskCache.m_strSavePath, m_values, m_nValueLen, bIsPreview);
	}

    /**
     * 点击确定按钮
     */
	public void ok()
	{
		procImage(m_values, m_nValueLen, false);//真实图的操作
		super.ok();
	}

    /**
     * 删除临时数据与内存
     */
	public void clear()
	{
		if ( m_imageDiskCache != null)
		{
			m_imageDiskCache.clear();
			m_imageDiskCache = null;
		}
		super.clear();
	}
}
